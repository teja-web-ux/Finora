package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = WhiteCard,
    primaryContainer = MintGreen,
    onPrimaryContainer = DarkNavy,
    secondary = MintDark,
    onSecondary = WhiteCard,
    secondaryContainer = Color(0xFFD4F8ED),
    onSecondaryContainer = DarkNavy,
    tertiary = ExpenseRed,
    onTertiary = WhiteCard,
    background = LightBg,
    onBackground = TextPrimary,
    surface = WhiteCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEBF1FA),
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = ExpenseRed,
    onError = WhiteCard
)

private val DarkColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = DarkNavy,
    primaryContainer = DeepNavy,
    onPrimaryContainer = MintGreen,
    secondary = MintDark,
    onSecondary = DarkNavy,
    secondaryContainer = NavySurface,
    onSecondaryContainer = MintGreen,
    tertiary = ExpenseRed,
    onTertiary = WhiteCard,
    background = DarkNavy,
    onBackground = WhiteCard,
    surface = DeepNavy,
    onSurface = WhiteCard,
    surfaceVariant = NavySurface,
    onSurfaceVariant = Color(0xFFA0AEC0),
    outline = Color(0xFF23355D),
    error = ExpenseRed,
    onError = WhiteCard
)

@Composable
fun FinoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = if (darkTheme) DarkNavy.toArgb() else DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
