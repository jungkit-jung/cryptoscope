package com.cryptoscope.app.core.llm

import android.content.Context
import android.content.SharedPreferences

data class LlmConfig(
    val endpoint: String = "http://10.0.2.2:11434/v1",
    val apiKey: String = "",
    val model: String = "llama3",
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT
) {
    companion object {
        const val DEFAULT_SYSTEM_PROMPT = 
            "Anda adalah AI Analis Kripto & On-Chain Security Expert di CryptoScope. " +
            "Tugas Anda adalah menganalisis pergerakan smart wallet, keamanan likuiditas, " +
            "mendeteksi jebakan/honeypot, serta memberikan evaluasi teknikal (RSI, EMA, Trend) " +
            "secara objektif, tajam, ringkas, dan profesional dalam Bahasa Indonesia."
    }
}

object LlmConfigManager {
    private const val PREF_NAME = "cryptoscope_llm_prefs"
    private const val KEY_ENDPOINT = "llm_endpoint"
    private const val KEY_API_KEY = "llm_api_key"
    private const val KEY_MODEL = "llm_model"
    private const val KEY_SYSTEM_PROMPT = "llm_system_prompt"

    fun getConfig(context: Context): LlmConfig {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return LlmConfig(
            endpoint = prefs.getString(KEY_ENDPOINT, "http://10.0.2.2:11434/v1") ?: "http://10.0.2.2:11434/v1",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            model = prefs.getString(KEY_MODEL, "llama3") ?: "llama3",
            systemPrompt = prefs.getString(KEY_SYSTEM_PROMPT, LlmConfig.DEFAULT_SYSTEM_PROMPT) ?: LlmConfig.DEFAULT_SYSTEM_PROMPT
        )
    }

    fun saveConfig(context: Context, config: LlmConfig) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ENDPOINT, config.endpoint.trim())
            .putString(KEY_API_KEY, config.apiKey.trim())
            .putString(KEY_MODEL, config.model.trim())
            .putString(KEY_SYSTEM_PROMPT, config.systemPrompt.trim())
            .apply()
    }
}
