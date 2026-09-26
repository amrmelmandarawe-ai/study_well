package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    primary = SleekBlue400,
    onPrimary = SleekNavy950,
    primaryContainer = SleekNavy800,
    onPrimaryContainer = SleekBlue100,
    secondary = SleekGold400,
    onSecondary = SleekNavy950,
    secondaryContainer = SleekNavy700,
    onSecondaryContainer = SleekGoldLight,
    tertiary = SleekBlue500,
    onTertiary = SleekNavy950,
    background = SleekBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SleekSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SleekSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SleekBorderDark,
    outlineVariant = SleekBorderDark,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun sleekTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = SleekNavy900.copy(alpha = 0.85f),
    unfocusedContainerColor = SleekNavy900.copy(alpha = 0.65f),
    disabledContainerColor = SleekNavy900.copy(alpha = 0.35f),
    focusedBorderColor = SleekBlue400,
    unfocusedBorderColor = Color(0xFF475569),
    focusedLabelColor = SleekBlue400,
    unfocusedLabelColor = Color(0xFF94A3B8),
    cursorColor = SleekGold400,
    focusedLeadingIconColor = SleekBlue400,
    unfocusedLeadingIconColor = Color(0xFF94A3B8),
    focusedTrailingIconColor = SleekBlue400,
    unfocusedTrailingIconColor = Color(0xFF94A3B8),
    focusedPlaceholderColor = Color(0xFF94A3B8),
    unfocusedPlaceholderColor = Color(0xFF64748B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true for Study Well signature sleek dark theme
    dynamicColor: Boolean = false, // Set to false to preserve Study Well brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

