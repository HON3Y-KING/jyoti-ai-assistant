package com.jyoti.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyoti.core.common.AppLanguage
import com.jyoti.core.voice.Personality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    fun setPersonality(personality: Personality) {
        viewModelScope.launch { repository.setPersonality(personality) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    companion object {
        val availableLanguages = listOf(
            AppLanguage.HINDI to "हिंदी (Hindi)",
            AppLanguage.HINGLISH to "Hinglish",
            AppLanguage.ENGLISH to "English"
        )
    }
}
