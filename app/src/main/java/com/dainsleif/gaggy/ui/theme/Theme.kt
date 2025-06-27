package com.dainsleif.gaggy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),         // Medium green
    secondary = Color(0xFF8BC34A),       // Light green
    tertiary = Color(0xFF009688),        // Teal
    background = Color(0xFF0A1F0A),      // Very dark green
    surface = Color(0xFF132713),         // Dark green
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE1F5E1),    // Very light green
    onSurface = Color(0xFFE1F5E1)        // Very light green
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),         // Forest green
    secondary = Color(0xFF66BB6A),       // Medium green
    tertiary = Color(0xFF00796B),        // Dark teal
    background = Color(0xFFECF7EC),      // Very light green
    surface = Color(0xFFFFFFFF),         // White
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1B5E20),    // Dark green
    onSurface = Color(0xFF1B5E20)        // Dark green
)

@Composable
fun GardenAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to use our custom green colors
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 