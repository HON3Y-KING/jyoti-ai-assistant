package com.jyoti.feature.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jyoti.core.common.AppLanguage
import com.jyoti.core.voice.Personality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "jyoti_settings")

data class UserPreferences(
    val personality: Personality = Personality.BREEZY,
    val language: String = AppLanguage.HINGLISH
)

/**
 * Local-only preferences (no account/server round trip needed for basic settings).
 * Any future module (e.g. WhatsApp workflows) that needs its own settings should add
 * its own keys here, or its own small DataStore, rather than growing HomeViewModel.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val personalityKey = stringPreferencesKey("personality")
    private val languageKey = stringPreferencesKey("language")

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            personality = Personality.fromId(prefs[personalityKey] ?: Personality.BREEZY.id),
            language = prefs[languageKey] ?: AppLanguage.HINGLISH
        )
    }

    suspend fun setPersonality(personality: Personality) {
        context.dataStore.edit { it[personalityKey] = personality.id }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[languageKey] = language }
    }
}
