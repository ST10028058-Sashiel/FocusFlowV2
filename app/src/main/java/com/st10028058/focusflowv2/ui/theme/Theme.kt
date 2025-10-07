package com.st10028058.focusflowv2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.ColorScheme

// ---------- Light Scheme ----------
private val LightColors = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = BrandPurple.copy(alpha = 0.12f),
    onPrimaryContainer = BrandPurple,

    secondary = BrandPink,
    onSecondary = Color.White,
    secondaryContainer = BrandPink.copy(alpha = 0.14f),
    onSecondaryContainer = BrandPink,

    background = LightBg,
    onBackground = Color(0xFF111111),

    surface = Color.White,
    onSurface = Color(0xFF111111),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF656565),

    outline = LightOutline,
    outlineVariant = LightSurfaceVariant
)

// ---------- Dark Scheme ----------
private val DarkColors = darkColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = BrandPurple.copy(alpha = 0.22f),
    onPrimaryContainer = BrandPurple,

    secondary = BrandPink,
    onSecondary = Color.White,
    secondaryContainer = BrandPink.copy(alpha = 0.25f),
    onSecondaryContainer = BrandPink,

    background = DarkBg,
    onBackground = Color(0xFFECECEC),

    surface = DarkSurface,
    onSurface = Color(0xFFECECEC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBBB3C8),

    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant
)

/**
 * App theme root.
 * MainActivity: FocusFlowV2Theme(darkTheme = isDarkMode) { ... }
 */
@Composable
fun FocusFlowV2Theme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

/**
 * Convenience flag for screens that need to branch on light/dark
 * without checking system UI directly.
 *
 * Usage:
 *   val colors = MaterialTheme.colorScheme
 *   if (colors.isLight) { ... } else { ... }
 */
val ColorScheme.isLight: Boolean
    get() = surface.luminance() > 0.5f
