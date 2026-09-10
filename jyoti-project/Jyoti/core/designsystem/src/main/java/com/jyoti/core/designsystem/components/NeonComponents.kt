package com.jyoti.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jyoti.core.designsystem.NeonCyan
import com.jyoti.core.designsystem.NeonMagenta
import com.jyoti.core.designsystem.NeonViolet

/**
 * The big pulsing mic orb on the home screen. Glow color communicates state
 * (idle / listening / speaking) without needing extra text.
 */
@Composable
fun NeonMicOrb(
    isActive: Boolean,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val pulse by transition.animateFloat(
        initialValue = if (isActive) 0.94f else 1f,
        targetValue = if (isActive) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 650 else 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic-pulse-value"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .scale(pulse)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.55f), Color.Transparent),
                    radius = 220f
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(2.dp, Brush.linearGradient(listOf(NeonCyan, NeonMagenta, NeonViolet)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Tap to talk to Jyoti",
                tint = glowColor,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/** Full-bleed neon gradient backdrop reused by every screen for visual consistency. */
@Composable
fun NeonBackground(content: @Composable Box.() -> Unit) {
    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF160B2E),
                        MaterialTheme.colorScheme.background
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 1600f)
                )
            )
    ) {
        content()
    }
}

