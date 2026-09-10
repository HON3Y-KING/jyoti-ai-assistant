package com.jyoti.core.common

object AppLanguage {
    const val HINDI = "hi"
    const val HINGLISH = "hi-en"
    const val ENGLISH = "en"
}

enum class VoicePersonality(val id: String, val displayName: String) {
    BREEZY("breezy", "Breezy"),
    FIRM("firm", "Firm")
}
