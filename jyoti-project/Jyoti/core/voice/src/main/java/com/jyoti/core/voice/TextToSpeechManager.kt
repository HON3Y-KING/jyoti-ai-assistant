package com.jyoti.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import java.util.UUID

sealed class SpeakEvent {
    data object Started : SpeakEvent()
    data object Finished : SpeakEvent()
    data class Error(val message: String) : SpeakEvent()
}

/**
 * Wraps Android's built-in TextToSpeech engine. Personality (Breezy / Firm) is applied
 * as pitch + rate adjustments on top of whatever system voice is selected for the locale.
 */
class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val appContext = context.applicationContext

    fun init(onReady: (Boolean) -> Unit) {
        tts = TextToSpeech(appContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            onReady(ready)
        }
    }

    fun speak(text: String, localeTag: String, personality: Personality): Flow<SpeakEvent> = callbackFlow {
        val engine = tts
        if (engine == null || !ready) {
            trySend(SpeakEvent.Error("Text-to-speech isn't ready yet."))
            close()
            return@callbackFlow
        }

        engine.language = Locale.forLanguageTag(if (localeTag == "hi-en") "hi-IN" else localeTag)
        engine.setPitch(personality.pitch)
        engine.setSpeechRate(personality.speechRate)

        val utteranceId = UUID.randomUUID().toString()
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { trySend(SpeakEvent.Started) }
            override fun onDone(utteranceId: String?) { trySend(SpeakEvent.Finished); close() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { trySend(SpeakEvent.Error("Playback error")); close() }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        awaitClose { engine.stop() }
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
