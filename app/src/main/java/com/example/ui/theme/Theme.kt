package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val FitBusterShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FitBusterTheme(
    accentPreset: String = "NEON_LIME",
    content: @Composable () -> Unit
) {
    val primaryAccent = when (accentPreset) {
        "ELECTRIC_VOLT" -> ElectricVolt
        "CYBER_EMERALD" -> CyberEmerald
        else -> NeonLime
    }

    val colorScheme = darkColorScheme(
        primary = primaryAccent,
        onPrimary = ObsidianBlack,
        primaryContainer = primaryAccent.copy(alpha = 0.16f),
        onPrimaryContainer = primaryAccent,
        secondary = HydrationCyan,
        onSecondary = ObsidianBlack,
        secondaryContainer = HydrationCyan.copy(alpha = 0.16f),
        onSecondaryContainer = HydrationCyan,
        tertiary = FlameAmber,
        onTertiary = ObsidianBlack,
        tertiaryContainer = FlameAmber.copy(alpha = 0.16f),
        onTertiaryContainer = FlameAmber,
        background = ObsidianBlack,
        onBackground = CrispWhite,
        surface = CarbonSurface,
        onSurface = CrispWhite,
        surfaceVariant = ElevatedCarbon,
        onSurfaceVariant = CoolGray,
        surfaceContainerLowest = ObsidianBlack,
        surfaceContainerLow = CarbonSurface,
        surfaceContainer = ElevatedCarbon,
        surfaceContainerHigh = CharcoalMuted,
        outline = CarbonCardBorder,
        outlineVariant = Color(0xFF1E242E),
        error = AlertCrimson,
        onError = CrispWhite,
        errorContainer = AlertCrimson.copy(alpha = 0.18f),
        onErrorContainer = AlertCrimson
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitBusterTypography,
        shapes = FitBusterShapes,
        content = content
    )
}
