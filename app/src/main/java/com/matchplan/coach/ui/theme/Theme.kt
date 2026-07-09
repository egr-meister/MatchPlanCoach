package com.matchplan.coach.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Extra dark-mode surfaces (declared first so top-level init order is safe).
private val DarkBackground = Color(0xFF0F1420)
private val DarkSurface = Color(0xFF1A2233)

private val LightColors = lightColorScheme(
    primary = DeepNavyBlue,
    onPrimary = WhiteCard,
    primaryContainer = PaleBlueBackground,
    onPrimaryContainer = DeepNavy,
    secondary = StrongGreen,
    onSecondary = WhiteCard,
    secondaryContainer = SoftGreenPanel,
    onSecondaryContainer = DeepNavy,
    tertiary = FieldGreen,
    onTertiary = WhiteCard,
    background = LightAppBackground,
    onBackground = DarkText,
    surface = WhiteCard,
    onSurface = DarkText,
    surfaceVariant = PaleBlueBackground,
    onSurfaceVariant = SecondaryDark,
    error = ErrorRed,
    onError = WhiteCard,
    outline = MutedLabelGray
)

private val DarkColors = darkColorScheme(
    primary = PaleBlueBackground,
    onPrimary = DeepNavy,
    primaryContainer = DeepNavyBlue,
    onPrimaryContainer = WhiteCard,
    secondary = StatusGreen,
    onSecondary = DeepNavy,
    secondaryContainer = StrongGreen,
    onSecondaryContainer = WhiteCard,
    tertiary = FieldGreen,
    onTertiary = WhiteCard,
    background = DarkBackground,
    onBackground = WhiteCard,
    surface = DarkSurface,
    onSurface = WhiteCard,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = MutedLabelGray,
    error = ErrorRed,
    onError = WhiteCard,
    outline = SecondaryDark
)

@Composable
fun MatchPlanCoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
