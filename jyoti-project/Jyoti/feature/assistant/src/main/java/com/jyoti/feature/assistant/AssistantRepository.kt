package com.jyoti.feature.assistant

import com.jyoti.core.common.JyotiResult
import com.jyoti.core.network.ChatApi
import com.jyoti.core.network.dto.ChatRequestDto
import com.jyoti.core.network.dto.ChatTurnDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ConversationTurn(val isUser: Boolean, val text: String)

/**
 * The only place that talks to the AI. It always goes through ChatApi -> our backend
 * proxy, sending the recent turns + chosen language + personality, and gets back plain
 * reply text. Swapping AI providers, adding streaming, or adding tool-calling for future
 * modules (phone control, WhatsApp, call screening) only touches this class and the
 * backend — feature:home and feature:settings never change.
 */
class AssistantRepository @Inject constructor(
    private val chatApi: ChatApi
) {
    suspend fun sendMessage(
        history: List<ConversationTurn>,
        language: String,
        personalityId: String
    ): JyotiResult<String> = withContext(Dispatchers.IO) {
        try {
            val response = chatApi.sendMessage(
                ChatRequestDto(
                    messages = history.map {
                        ChatTurnDto(role = if (it.isUser) "user" else "assistant", content = it.text)
                    },
                    language = language,
                    personality = personalityId
                )
            )
            JyotiResult.Success(response.reply)
        } catch (t: Throwable) {
            JyotiResult.Error(t.message ?: "Something went wrong talking to Jyoti.", t)
        }
    }
}
