package com.example.tolamol.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            var result = engine.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = engine.setLanguage(Locale.getDefault())
            }
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = engine.setLanguage(Locale.US)
            }
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
