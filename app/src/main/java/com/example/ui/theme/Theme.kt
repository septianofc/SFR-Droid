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
    primary = BlueDarkPrimary,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = OrangeDarkAccent,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF693C00),
    onSecondaryContainer = Color(0xFFFFDCC1),
    tertiary = Color(0xFF81C784),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCardSurface,
    error = Color(0xFFEF9A9A)
)

private val LightColorScheme = lightColorScheme(
    primary = SfrBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = SfrOrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC1),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = SfrGreenSuccess,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFE6ECF5),
    error = SfrRedError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false by default to showcase branded SFR Droid colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
