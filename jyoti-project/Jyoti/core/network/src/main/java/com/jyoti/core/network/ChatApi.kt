package com.jyoti.core.network

import com.jyoti.core.network.dto.ChatRequestDto
import com.jyoti.core.network.dto.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Talks ONLY to our backend proxy (Jyoti server), never to an AI provider directly.
 * The proxy authenticates the request, injects its own securely-stored provider key,
 * calls the LLM, and returns just the reply text. This is the boundary that keeps
 * secrets out of the APK — see README "Security model" for the full explanation.
 */
interface ChatApi {
    @POST("v1/chat")
    suspend fun sendMessage(@Body request: ChatRequestDto): ChatResponseDto
}
