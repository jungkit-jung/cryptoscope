package com.cryptoscope.app.core.telegram

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class TelegramConfig(
    val botToken: String = "",
    val chatId: String = "",
    val isEnabled: Boolean = false,
    val alertOnTraps: Boolean = true,
    val alertOnTrades: Boolean = true
)

object TelegramConfigManager {
    private const val PREF_NAME = "cryptoscope_tg_prefs"
    private const val KEY_TOKEN = "tg_bot_token"
    private const val KEY_CHAT_ID = "tg_chat_id"
    private const val KEY_ENABLED = "tg_is_enabled"
    private const val KEY_ALERT_TRAPS = "tg_alert_traps"
    private const val KEY_ALERT_TRADES = "tg_alert_trades"

    fun getConfig(context: Context): TelegramConfig {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return TelegramConfig(
            botToken = prefs.getString(KEY_TOKEN, "") ?: "",
            chatId = prefs.getString(KEY_CHAT_ID, "") ?: "",
            isEnabled = prefs.getBoolean(KEY_ENABLED, false),
            alertOnTraps = prefs.getBoolean(KEY_ALERT_TRAPS, true),
            alertOnTrades = prefs.getBoolean(KEY_ALERT_TRADES, true)
        )
    }

    fun saveConfig(context: Context, config: TelegramConfig) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, config.botToken.trim())
            .putString(KEY_CHAT_ID, config.chatId.trim())
            .putBoolean(KEY_ENABLED, config.isEnabled)
            .putBoolean(KEY_ALERT_TRAPS, config.alertOnTraps)
            .putBoolean(KEY_ALERT_TRADES, config.alertOnTrades)
            .apply()
    }
}

object TelegramBridge {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    suspend fun sendMessage(
        botToken: String,
        chatId: String,
        text: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (botToken.isBlank() || chatId.isBlank()) {
            return@withContext Result.failure(Exception("Bot Token atau Chat ID belum diisi."))
        }

        try {
            val url = "https://api.telegram.org/bot${botToken.trim()}/sendMessage"
            val bodyJson = JsonObject().apply {
                addProperty("chat_id", chatId.trim())
                addProperty("text", text)
                addProperty("parse_mode", "HTML")
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Telegram API Error (${response.code}): $responseString"))
            }

            Result.success("Pesan Telegram berhasil terkirim.")
        } catch (e: Exception) {
            Result.failure(Exception("Gagal kirim Telegram: ${e.localizedMessage ?: e.message}"))
        }
    }

    suspend fun testConnection(botToken: String, chatId: String): Result<String> {
        val testMessage = "🤖 <b>CryptoScope Agentic AI</b>\n\n" +
                "Koneksi Telegram Remote Bridge berhasil aktif!\n" +
                "Agentic AI siap mengirim notifikasi eksekusi dan peringatan Anti-Trap langsung ke Telegram Anda."
        return sendMessage(botToken, chatId, testMessage)
    }

    suspend fun notifyTradeExecuted(
        config: TelegramConfig,
        tokenSymbol: String,
        price: Double,
        amountUsd: Double,
        strategy: String,
        tp: String,
        sl: String
    ) {
        if (!config.isEnabled || !config.alertOnTrades || config.botToken.isBlank() || config.chatId.isBlank()) return

        val msg = "⚡ <b>AGENTIC TRADE EXECUTED</b>\n\n" +
                "• <b>Token:</b> ${tokenSymbol}/USDT\n" +
                "• <b>Entry:</b> $${String.format("%.4f", price)}\n" +
                "• <b>Alokasi:</b> $${amountUsd.toInt()} USD\n" +
                "• <b>Strategi:</b> ${strategy}\n" +
                "• <b>Target TP:</b> ${tp}\n" +
                "• <b>Stop Loss:</b> ${sl}\n\n" +
                "<i>Eksekusi otonom berhasil dicatat di portofolio CryptoScope.</i>"

        sendMessage(config.botToken, config.chatId, msg)
    }

    suspend fun notifyTrapBlocked(
        config: TelegramConfig,
        tokenSymbol: String,
        trapType: String,
        reason: String,
        capitalSaved: Double
    ) {
        if (!config.isEnabled || !config.alertOnTraps || config.botToken.isBlank() || config.chatId.isBlank()) return

        val msg = "🚨 <b>ANTI-TRAP INTERCEPTED!</b>\n\n" +
                "• <b>Token Scam:</b> ${tokenSymbol}\n" +
                "• <b>Tipe Perangkap:</b> ${trapType}\n" +
                "• <b>Detail:</b> ${reason}\n" +
                "• <b>Modal Terselamatkan:</b> +$${capitalSaved.toInt()} USD\n\n" +
                "<i>Transaksi dicegah otomatis oleh 7-Layer Defense Matrix.</i>"

        sendMessage(config.botToken, config.chatId, msg)
    }
}
