package com.cryptoscope.app.core.llm

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

object CustomLlmClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    fun normalizeEndpoint(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }
        return when {
            url.endsWith("/chat/completions") -> url
            url.endsWith("/v1") -> "$url/chat/completions"
            url.contains(":11434") && !url.contains("/v1") -> "$url/v1/chat/completions"
            else -> {
                if (!url.endsWith("/v1/chat/completions")) {
                    "$url/v1/chat/completions"
                } else {
                    url
                }
            }
        }
    }

    suspend fun testConnection(config: LlmConfig): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = normalizeEndpoint(config.endpoint)
            val jsonBody = JsonObject().apply {
                addProperty("model", config.model)
                val messages = JsonArray().apply {
                    val msg = JsonObject().apply {
                        addProperty("role", "user")
                        addProperty("content", "Ping. Reply with exactly 'PONG'.")
                    }
                    add(msg)
                }
                add("messages", messages)
                addProperty("max_tokens", 10)
                addProperty("temperature", 0.1)
            }

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA))

            if (config.apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val code = response.code
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP $code: ${if (bodyString.isNotBlank()) bodyString.take(150) else response.message}")
                )
            }

            val jsonResponse = gson.fromJson(bodyString, JsonObject::class.java)
            val choices = jsonResponse.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                val text = message?.get("content")?.asString ?: "Koneksi berhasil."
                Result.success("Terhubung! Respon LLM: ${text.trim()}")
            } else {
                Result.success("Terhubung ke server (HTTP $code)!")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi Gagal: ${e.localizedMessage ?: e.message}"))
        }
    }

    suspend fun sendChat(
        messages: List<ChatMessage>,
        config: LlmConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = normalizeEndpoint(config.endpoint)
            val jsonBody = JsonObject().apply {
                addProperty("model", config.model)
                val messagesArray = JsonArray()

                // Always add system prompt if configured
                if (config.systemPrompt.isNotBlank()) {
                    val sysMsg = JsonObject().apply {
                        addProperty("role", "system")
                        addProperty("content", config.systemPrompt)
                    }
                    messagesArray.add(sysMsg)
                }

                messages.forEach { msg ->
                    val obj = JsonObject().apply {
                        addProperty("role", msg.role)
                        addProperty("content", msg.content)
                    }
                    messagesArray.add(obj)
                }

                add("messages", messagesArray)
                addProperty("temperature", 0.7)
            }

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA))

            if (config.apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val code = response.code
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Gagal dari LLM (HTTP $code): ${if (bodyString.isNotBlank()) bodyString.take(200) else response.message}")
                )
            }

            val jsonResponse = gson.fromJson(bodyString, JsonObject::class.java)
            val choices = jsonResponse.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                val reply = message?.get("content")?.asString.orEmpty()
                Result.success(reply.trim())
            } else {
                Result.failure(Exception("Format balasan LLM tidak dikenali: $bodyString"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error komunikasi LLM: ${e.localizedMessage ?: e.message}"))
        }
    }
}
