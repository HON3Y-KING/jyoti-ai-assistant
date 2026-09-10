package com.jyoti.core.voice

/**
 * Maps each personality to concrete TTS knobs. Adding a third personality later
 * is just adding another entry + a value class here — nothing else in the app changes.
 */
enum class Personality(val id: String, val label: String, val pitch: Float, val speechRate: Float) {
    BREEZY(id = "breezy", label = "Breezy", pitch = 1.12f, speechRate = 1.08f),
    FIRM(id = "firm", label = "Firm", pitch = 0.92f, speechRate = 0.98f);

    companion object {
        fun fromId(id: String): Personality = entries.firstOrNull { it.id == id } ?: BREEZY
    }
}
