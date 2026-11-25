package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NabraawyDarkColorScheme = darkColorScheme(
    primary = ElectricSky,
    onPrimary = DeepNavyBg,
    primaryContainer = DeepRoyalContainer,
    onPrimaryContainer = TextPrimary,
    secondary = WarmAmber,
    onSecondary = DeepNavyBg,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = WarmAmber,
    tertiary = VioletInsight,
    onTertiary = DeepNavyBg,
    tertiaryContainer = VioletContainer,
    onTertiaryContainer = TextPrimary,
    background = DeepNavyBg,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLow = NavySurface,
    surfaceContainer = NavyCard,
    surfaceContainerHigh = NavyCardElevated,
    outline = BorderNavy,
    outlineVariant = BorderNavy.copy(alpha = 0.55f),
    error = CoralError,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = CoralError
)

private val NabraawyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NabraawyStudyTheme(
    isArabic: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NabraawyDarkColorScheme,
        typography = buildNabraawyTypography(isArabic = isArabic),
        shapes = NabraawyShapes,
        content = content
    )
}
