package com.jyoti.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jyoti.core.common.JyotiResult
import com.jyoti.core.voice.Personality
import com.jyoti.core.voice.SpeechEvent
import com.jyoti.core.voice.SpeechRecognizerManager
import com.jyoti.core.voice.TextToSpeechManager
import com.jyoti.feature.assistant.AssistantRepository
import com.jyoti.feature.assistant.ConversationTurn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ListeningState { IDLE, LISTENING, THINKING, SPEAKING, ERROR }

data class HomeUiState(
    val listeningState: ListeningState = ListeningState.IDLE,
    val transcriptSoFar: String = "",
    val conversation: List<ConversationTurn> = emptyList(),
    val personality: Personality = Personality.BREEZY,
    val languageTag: String = "hi-IN", // Hindi by default; Hinglish reuses hi-IN recognition + mixed replies
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val assistantRepository: AssistantRepository
) : AndroidViewModel(application) {

    private val speechRecognizer = SpeechRecognizerManager(application)
    private val textToSpeech = TextToSpeechManager(application).apply { init { } }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onMicTapped() {
        val current = _uiState.value.listeningState
        if (current == ListeningState.LISTENING) {
            speechRecognizer.cancel()
            return
        }
        if (current == ListeningState.THINKING || current == ListeningState.SPEAKING) return

        _uiState.value = _uiState.value.copy(listeningState = ListeningState.LISTENING, errorMessage = null)

        viewModelScope.launch {
            speechRecognizer.listen(_uiState.value.languageTag).collect { event ->
                when (event) {
                    is SpeechEvent.PartialResult ->
                        _uiState.value = _uiState.value.copy(transcriptSoFar = event.text)

                    is SpeechEvent.FinalResult -> {
                        _uiState.value = _uiState.value.copy(transcriptSoFar = "")
                        if (event.text.isNotBlank()) sendToAssistant(event.text)
                        else _uiState.value = _uiState.value.copy(listeningState = ListeningState.IDLE)
                    }

                    is SpeechEvent.Error -> _uiState.value = _uiState.value.copy(
                        listeningState = ListeningState.ERROR,
                        errorMessage = event.message
                    )

                    else -> Unit
                }
            }
        }
    }

    fun setPersonality(personality: Personality) {
        _uiState.value = _uiState.value.copy(personality = personality)
    }

    fun setLanguageTag(tag: String) {
        _uiState.value = _uiState.value.copy(languageTag = tag)
    }

    private fun sendToAssistant(userText: String) {
        val updatedHistory = _uiState.value.conversation + ConversationTurn(isUser = true, text = userText)
        _uiState.value = _uiState.value.copy(conversation = updatedHistory, listeningState = ListeningState.THINKING)

        viewModelScope.launch {
            when (val result = assistantRepository.sendMessage(
                history = updatedHistory,
                language = _uiState.value.languageTag,
                personalityId = _uiState.value.personality.id
            )) {
                is JyotiResult.Success -> {
                    val withReply = updatedHistory + ConversationTurn(isUser = false, text = result.data)
                    _uiState.value = _uiState.value.copy(conversation = withReply, listeningState = ListeningState.SPEAKING)
                    speakReply(result.data)
                }
                is JyotiResult.Error -> _uiState.value = _uiState.value.copy(
                    listeningState = ListeningState.ERROR,
                    errorMessage = result.message
                )
                JyotiResult.Loading -> Unit
            }
        }
    }

    private fun speakReply(text: String) {
        viewModelScope.launch {
            textToSpeech.speak(text, _uiState.value.languageTag, _uiState.value.personality).collect { event ->
                when (event) {
                    is com.jyoti.core.voice.SpeakEvent.Finished ->
                        _uiState.value = _uiState.value.copy(listeningState = ListeningState.IDLE)
                    is com.jyoti.core.voice.SpeakEvent.Error ->
                        _uiState.value = _uiState.value.copy(listeningState = ListeningState.IDLE, errorMessage = event.message)
                    else -> Unit
                }
            }
        }
    }

    override fun onCleared() {
        textToSpeech.shutdown()
        super.onCleared()
    }
}
