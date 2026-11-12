package com.jarvis.assistant

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.ai.AiProvider
import com.jarvis.assistant.ai.ChatMessage
import com.jarvis.assistant.ai.GeminiBackendProvider
import com.jarvis.assistant.ai.Role
import com.jarvis.assistant.data.Settings
import com.jarvis.assistant.voice.SpeechInput
import com.jarvis.assistant.voice.SpeechOutput
import kotlinx.coroutines.launch

enum class CoreState { IDLE, LISTENING, THINKING, SPEAKING }

class JarvisViewModel(application: Application) :
    AndroidViewModel(application), SpeechInput.Listener {

    private val settings = Settings(application)

    // Swap this line to plug in a different AI model later.
    private val provider: AiProvider = GeminiBackendProvider(
        backendUrl = { settings.backendUrl },
        appToken = { settings.appToken }
    )

    private var listening by mutableStateOf(false)
    private var thinking by mutableStateOf(false)
    private var speaking by mutableStateOf(false)

    val messages = mutableStateListOf<ChatMessage>()

    var partialText by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var backendUrl by mutableStateOf(settings.backendUrl)
        private set
    var appToken by mutableStateOf(settings.appToken)
        private set
    var speakReplies by mutableStateOf(settings.speakReplies)
        private set
    var speechRate by mutableStateOf(settings.speechRate)
        private set

    private val speechInput = SpeechInput(application, this)
    private val speechOutput = SpeechOutput(
        context = application,
        onSpeakingChanged = { speaking = it },
        onProblem = { errorMessage = it }
    )

    val coreState: CoreState
        get() = when {
            thinking -> CoreState.THINKING
            listening -> CoreState.LISTENING
            speaking -> CoreState.SPEAKING
            else -> CoreState.IDLE
        }

    val isSpeaking: Boolean
        get() = speaking

    // ---------- voice input ----------

    fun startListening() {
        if (thinking) return
        stopSpeaking()
        errorMessage = null
        partialText = ""
        listening = true
        speechInput.start()
    }

    fun stopListening() {
        speechInput.finish()
    }

    override fun onListeningStarted() {
        listening = true
    }

    override fun onPartial(text: String) {
        partialText = text
    }

    override fun onFinal(text: String) {
        listening = false
        partialText = ""
        if (text.isNotBlank()) sendText(text)
    }

    override fun onError(message: String) {
        listening = false
        partialText = ""
        errorMessage = message
    }

    // ---------- chat ----------

    fun sendText(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || thinking) return

        if (listening) {
            speechInput.cancel()
            listening = false
            partialText = ""
        }
        stopSpeaking()
        errorMessage = null
        messages.add(ChatMessage(Role.USER, text))
        thinking = true

        val snapshot = messages.toList()
        viewModelScope.launch {
            val result = provider.reply(snapshot)
            thinking = false
            result
                .onSuccess { answer ->
                    messages.add(ChatMessage(Role.ASSISTANT, answer))
                    if (speakReplies) speechOutput.speak(answer, speechRate)
                }
                .onFailure { e ->
                    val reason = e.message ?: "Unknown error."
                    errorMessage = reason
                    messages.add(
                        ChatMessage(
                            Role.ASSISTANT,
                            "I couldn't complete that request. $reason",
                            isError = true
                        )
                    )
                }
        }
    }

    fun stopSpeaking() {
        speechOutput.stop()
    }

    fun clearChat() {
        messages.clear()
    }

    fun reportError(message: String) {
        errorMessage = message
    }

    fun dismissError() {
        errorMessage = null
    }

    fun saveSettings(url: String, token: String, speak: Boolean, rate: Float) {
        settings.backendUrl = url
        settings.appToken = token
        settings.speakReplies = speak
        settings.speechRate = rate
        backendUrl = settings.backendUrl
        appToken = settings.appToken
        speakReplies = speak
        speechRate = rate
        if (!speak) stopSpeaking()
    }

    override fun onCleared() {
        speechInput.cancel()
        speechOutput.shutdown()
        super.onCleared()
    }
}
