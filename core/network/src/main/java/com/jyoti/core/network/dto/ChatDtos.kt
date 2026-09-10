package com.jyoti.core.network.dto

/**
 * Wire format between the Jyoti app and OUR OWN backend proxy — never the AI
 * provider's raw API. The proxy is what actually holds the provider secret key
 * and forwards the request server-side. See /server-example in the repo root.
 */
data class ChatTurnDto(
    val role: String,       // "user" | "assistant"
    val content: String
)

data class ChatRequestDto(
    val messages: List<ChatTurnDto>,
    val language: String,        // "hi", "hi-en" (Hinglish), or "en"
    val personality: String      // "breezy" | "firm"
)

data class ChatResponseDto(
    val reply: String,
    val language: String
)
