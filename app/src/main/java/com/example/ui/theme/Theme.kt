package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TechHorizonColors.Dark.primary,
    secondary = TechHorizonColors.Dark.secondary,
    tertiary = TechHorizonColors.Dark.accent,
    background = TechHorizonColors.Dark.background,
    surface = TechHorizonColors.Dark.surface,
    outline = TechHorizonColors.Dark.border,
    error = TechHorizonColors.Dark.error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TechHorizonColors.Dark.textPrimary,
    onSurface = TechHorizonColors.Dark.textPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = TechHorizonColors.Light.primary,
    secondary = TechHorizonColors.Light.secondary,
    tertiary = TechHorizonColors.Light.accent,
    background = TechHorizonColors.Light.background,
    surface = TechHorizonColors.Light.surface,
    outline = TechHorizonColors.Light.border,
    error = TechHorizonColors.Light.error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TechHorizonColors.Light.textPrimary,
    onSurface = TechHorizonColors.Light.textPrimary
)

@Composable
fun NexGenLmsTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Use brand theme for consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
