package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BentoPurpleDark,
    onPrimary = BentoPurpleDeep,
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = BentoPurpleContainer,
    secondary = BentoPurpleLight,
    onSecondary = BentoPurpleDeep,
    secondaryContainer = BentoSurfaceVariantDark,
    onSecondaryContainer = BentoTextPrimaryDark,
    background = BentoBackgroundDark,
    onBackground = BentoTextPrimaryDark,
    surface = BentoSurfaceDark,
    onSurface = BentoTextPrimaryDark,
    surfaceVariant = BentoSurfaceVariantDark,
    onSurfaceVariant = BentoTextSecondaryDark,
    outline = BentoBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = BentoPurpleContainer,
    onPrimaryContainer = BentoPurpleOnContainer,
    secondary = BentoPurpleDeep,
    onSecondary = Color.White,
    secondaryContainer = BentoPurpleLight,
    onSecondaryContainer = BentoPurpleDeep,
    background = BentoBackgroundLight,
    onBackground = BentoTextPrimary,
    surface = BentoSurfaceLight,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoSurfaceVariant,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorderLight,
    outlineVariant = BentoBorderMedium
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
