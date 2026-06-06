package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ElegantColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimaryDark,
    background = ElegantBackground,
    onBackground = ElegantPrimaryText,
    surface = ElegantSurface,
    onSurface = ElegantPrimaryText,
    surfaceVariant = ElegantContainer,
    onSurfaceVariant = ElegantOnSurfaceVariant,
    outline = ElegantOutline,
    secondary = ElegantActiveOverlay,
    onSecondary = ElegantPrimary,
    tertiary = ElegantEmerald,
    onTertiary = ElegantBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = ElegantColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
