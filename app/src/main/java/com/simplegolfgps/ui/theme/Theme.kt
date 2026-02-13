package com.simplegolfgps.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val GolfGreen = Color(0xFF2E7D32)
private val GolfGreenLight = Color(0xFF4CAF50)
private val GolfGreenDark = Color(0xFF1B5E20)
private val FairwayGreen = Color(0xFF66BB6A)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFF5F5F5)
private val DarkGreen = Color(0xFF1B5E20)

private val LightColorScheme = lightColorScheme(
    primary = GolfGreen,
    onPrimary = White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = DarkGreen,
    secondary = Color(0xFF546E7A),
    onSecondary = White,
    secondaryContainer = Color(0xFFCFD8DC),
    onSecondaryContainer = Color(0xFF263238),
    tertiary = Color(0xFF795548),
    onTertiary = White,
    background = OffWhite,
    onBackground = Color(0xFF1C1B1F),
    surface = White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = White,
)

private val DarkColorScheme = darkColorScheme(
    primary = FairwayGreen,
    onPrimary = DarkGreen,
    primaryContainer = GolfGreen,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF90A4AE),
    onSecondary = Color(0xFF263238),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFCFD8DC),
    tertiary = Color(0xFFBCAAA4),
    onTertiary = Color(0xFF3E2723),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF1B5E20),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

@Composable
fun SimpleGolfGPSTheme(
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
        typography = Typography(),
        content = content
    )
}
