package com.trackerx.ui.voice

import android.content.Intent
import android.speech.RecognizerIntent
import com.trackerx.ui.navigation.Screen
import java.text.Normalizer

/** Interpreta frases como "abrir mapa". Ponto de extensão para um assistente de IA. */
interface VoiceInterpreter {
    fun interpret(text: String): Screen?
}

object KeywordVoiceInterpreter : VoiceInterpreter {
    private val keywords = listOf(
        Screen.NAV to listOf("mapa", "navegacao", "waze", "maps", "rota"),
        Screen.MEDIA to listOf("musica", "midia", "spotify", "som"),
        Screen.CAR to listOf("carro", "veiculo", "tracker"),
        Screen.SETTINGS to listOf("configuracoes", "configuracao", "ajustes"),
        Screen.BLUETOOTH to listOf("bluetooth"),
        Screen.APPS to listOf("aplicativos", "apps"),
        Screen.SPLIT to listOf("dividir", "tela dividida"),
        Screen.HOME to listOf("inicio", "home", "painel")
    )

    private fun normalize(s: String): String =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

    override fun interpret(text: String): Screen? {
        val t = normalize(text)
        return keywords.firstOrNull { (_, words) -> words.any { t.contains(it) } }?.first
    }
}

fun voiceRecognitionIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga: abrir mapa, música, carro ou configurações")
    }
