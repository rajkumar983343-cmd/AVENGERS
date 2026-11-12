package com.jarvis.assistant.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** Android TextToSpeech wrapper. */
class SpeechOutput(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit,
    private val onProblem: (String) -> Unit
) {
    private val main = Handler(Looper.getMainLooper())
    private var ready = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            val engine = this.tts
            if (status == TextToSpeech.SUCCESS && engine != null) {
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        main.post { onSpeakingChanged(true) }
                    }

                    override fun onDone(utteranceId: String?) {
                        main.post { onSpeakingChanged(false) }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        main.post { onSpeakingChanged(false) }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        main.post { onSpeakingChanged(false) }
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        main.post { onSpeakingChanged(false) }
                    }
                })
                val result = engine.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    engine.setLanguage(Locale.US)
                }
                ready = true
            } else {
                main.post {
                    onProblem("Text-to-speech isn't available. Check Android Settings > Text-to-speech output.")
                }
            }
        }
    }

    fun speak(text: String, rate: Float) {
        val engine = tts
        if (!ready || engine == null) {
            onProblem("Voice output isn't ready yet. Try again in a moment.")
            return
        }
        val clean = text
            .replace(Regex("[*_`#>]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (clean.isEmpty()) return

        engine.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        val limit = TextToSpeech.getMaxSpeechInputLength() - 1
        engine.speak(clean.take(limit), TextToSpeech.QUEUE_FLUSH, null, "jarvis-utterance")
    }

    fun stop() {
        tts?.stop()
        main.post { onSpeakingChanged(false) }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
