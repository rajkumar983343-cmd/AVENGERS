package com.jarvis.assistant.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object JarvisColors {
    val Black = Color(0xFF020409)
    val Navy = Color(0xFF050B18)
    val NavyLight = Color(0xFF0A1730)
    val Cyan = Color(0xFF00E5FF)
    val Blue = Color(0xFF1E90FF)
    val TextPrimary = Color(0xFFE6F7FF)
    val TextDim = Color(0xFF7FA6BF)
    val Danger = Color(0xFFFF5C7A)
}

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = JarvisColors.Cyan,
            onPrimary = JarvisColors.Black,
            secondary = JarvisColors.Blue,
            background = JarvisColors.Navy,
            onBackground = JarvisColors.TextPrimary,
            surface = JarvisColors.NavyLight,
            onSurface = JarvisColors.TextPrimary,
            error = JarvisColors.Danger
        ),
        content = content
    )
}

@Composable
fun jarvisFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = JarvisColors.TextPrimary,
    unfocusedTextColor = JarvisColors.TextPrimary,
    cursorColor = JarvisColors.Cyan,
    focusedBorderColor = JarvisColors.Cyan,
    unfocusedBorderColor = JarvisColors.Cyan.copy(alpha = 0.35f),
    focusedPlaceholderColor = JarvisColors.TextDim,
    unfocusedPlaceholderColor = JarvisColors.TextDim,
    focusedLabelColor = JarvisColors.Cyan,
    unfocusedLabelColor = JarvisColors.TextDim
)
