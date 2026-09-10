package com.jyoti.feature.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jyoti.core.designsystem.NeonAmber
import com.jyoti.core.designsystem.StatusError
import com.jyoti.core.designsystem.StatusIdle
import com.jyoti.core.designsystem.StatusListening
import com.jyoti.core.designsystem.StatusSpeaking
import com.jyoti.core.designsystem.components.NeonBackground
import com.jyoti.core.designsystem.components.NeonMicOrb
import com.jyoti.core.voice.Personality
import com.jyoti.feature.assistant.ConversationTurn
import com.jyoti.feature.settings.SettingsRoute

const val HomeRoute = "home"

fun NavGraphBuilder.homeScreen(navController: NavController) {
    composable(HomeRoute) {
        HomeScreen(onOpenSettings = { navController.navigate(SettingsRoute) })
    }
}

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
        if (granted) viewModel.onMicTapped()
    }

    NeonBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(horizontal = 20.dp),
        ) {
            HomeTopBar(onOpenSettings = onOpenSettings)

            Spacer(modifier = Modifier.height(8.dp))

            PersonalityRow(
                selected = uiState.personality,
                onSelect = viewModel::setPersonality
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConversationArea(
                conversation = uiState.conversation,
                liveTranscript = uiState.transcriptSoFar,
                errorMessage = uiState.errorMessage,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatusLabel(state = uiState.listeningState)

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                NeonMicOrb(
                    isActive = uiState.listeningState == ListeningState.LISTENING,
                    glowColor = glowColorFor(uiState.listeningState),
                    onClick = {
                        if (hasMicPermission) {
                            viewModel.onMicTapped()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun HomeTopBar(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Jyoti",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "आपकी आवाज़ वाली सहायक",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PersonalityRow(selected: Personality, onSelect: (Personality) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Personality.entries.forEach { personality ->
            FilterChip(
                selected = personality == selected,
                onClick = { onSelect(personality) },
                label = { Text(personality.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ConversationArea(
    conversation: List<ConversationTurn>,
    liveTranscript: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    if (conversation.isEmpty() && liveTranscript.isBlank() && errorMessage == null) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Tap the mic and say \u201cNamaste Jyoti\u201d to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(conversation) { turn -> ConversationBubble(turn) }

        if (liveTranscript.isNotBlank()) {
            item { ConversationBubble(ConversationTurn(isUser = true, text = liveTranscript), isLive = true) }
        }
        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage,
                    color = StatusError,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ConversationBubble(turn: ConversationTurn, isLive: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (turn.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (turn.isUser) 18.dp else 4.dp,
                bottomEnd = if (turn.isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (turn.isUser)
                    MaterialTheme.colorScheme.primary.copy(alpha = if (isLive) 0.12f else 0.20f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = turn.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun StatusLabel(state: ListeningState) {
    val label = when (state) {
        ListeningState.IDLE -> "Tap to speak"
        ListeningState.LISTENING -> "Listening\u2026"
        ListeningState.THINKING -> "Jyoti is thinking\u2026"
        ListeningState.SPEAKING -> "Speaking\u2026"
        ListeningState.ERROR -> "Something went wrong"
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = glowColorFor(state)
        )
    }
}

private fun glowColorFor(state: ListeningState) = when (state) {
    ListeningState.LISTENING -> StatusListening
    ListeningState.SPEAKING -> StatusSpeaking
    ListeningState.THINKING -> NeonAmber
    ListeningState.ERROR -> StatusError
    ListeningState.IDLE -> StatusIdle
}
