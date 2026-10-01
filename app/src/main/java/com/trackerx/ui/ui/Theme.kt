package com.trackerx.ui.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFF3DD6C6)
val AccentDeep = Color(0xFF0E8F86)
val Warn = Color(0xFFFFB454)
val Danger = Color(0xFFFF6B6B)

private val Dark = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF00201D),
    background = Color(0xFF06090D),
    onBackground = Color(0xFFE8EEF4),
    surface = Color(0xFF111821),
    onSurface = Color(0xFFE8EEF4),
    surfaceVariant = Color(0xFF1A232E),
    onSurfaceVariant = Color(0xFF8A97A6),
    outline = Color(0xFF334150)
)

private val Light = lightColorScheme(
    primary = AccentDeep,
    onPrimary = Color.White,
    background = Color(0xFFEDF1F5),
    onBackground = Color(0xFF10161D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10161D),
    surfaceVariant = Color(0xFFE1E7EE),
    onSurfaceVariant = Color(0xFF5A6775),
    outline = Color(0xFFB8C3CF)
)

/** Preferências visuais vindas das configurações. */
data class UiPrefs(val dark: Boolean = true, val cardAlpha: Float = 0.72f, val scale: Float = 1f, val animations: Boolean = true)

val LocalUiPrefs = compositionLocalOf { UiPrefs() }

@Composable
fun TrackerTheme(prefs: UiPrefs, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalUiPrefs provides prefs) {
        MaterialTheme(
            colorScheme = if (prefs.dark) Dark else Light,
            typography = Typography(),
            content = content
        )
    }
}
