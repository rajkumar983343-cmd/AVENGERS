package com.jarvis.assistant.data

import android.content.Context

/**
 * Simple on-device settings. The app token is only a shared password for YOUR
 * backend. It is not your Gemini API key (that never touches the phone).
 */
class Settings(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    var backendUrl: String
        get() = prefs.getString("backend_url", "") ?: ""
        set(value) {
            prefs.edit().putString("backend_url", value.trim()).apply()
        }

    var appToken: String
        get() = prefs.getString("app_token", "") ?: ""
        set(value) {
            prefs.edit().putString("app_token", value.trim()).apply()
        }

    var speakReplies: Boolean
        get() = prefs.getBoolean("speak_replies", true)
        set(value) {
            prefs.edit().putBoolean("speak_replies", value).apply()
        }

    var speechRate: Float
        get() = prefs.getFloat("speech_rate", 1.0f)
        set(value) {
            prefs.edit().putFloat("speech_rate", value).apply()
        }
}
