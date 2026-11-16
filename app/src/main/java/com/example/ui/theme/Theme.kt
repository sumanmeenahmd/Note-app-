package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.ThemePreference

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaDarkPrimary,
    onPrimary = TerracottaDarkOnPrimary,
    primaryContainer = TerracottaDarkContainer,
    onPrimaryContainer = TerracottaDarkOnContainer,
    secondary = SageDarkSecondary,
    onSecondary = SageDarkOnSecondary,
    secondaryContainer = SageDarkContainer,
    onSecondaryContainer = SageDarkOnContainer,
    tertiary = AmberDarkTertiary,
    onTertiary = AmberDarkOnTertiary,
    tertiaryContainer = AmberDarkContainer,
    onTertiaryContainer = AmberDarkOnContainer,
    background = CharcoalBackground,
    onBackground = WarmIvoryOnBackground,
    surface = EspressoSurface,
    onSurface = WarmIvoryOnBackground,
    surfaceVariant = DarkTaupeSurfaceVariant,
    onSurfaceVariant = MutedStoneOnSurfaceVariant,
    surfaceContainerLow = ColorTokens.DarkSurfaceLow,
    surfaceContainer = ColorTokens.DarkSurface,
    surfaceContainerHigh = ColorTokens.DarkSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkHairlineOutlineVariant,
    error = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = SageOnContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberOnContainer,
    background = AlabasterBackground,
    onBackground = EspressoOnBackground,
    surface = IvorySurface,
    onSurface = EspressoOnBackground,
    surfaceVariant = WarmSandSurfaceVariant,
    onSurfaceVariant = TaupeOnSurfaceVariant,
    surfaceContainerLow = ColorTokens.LightSurfaceLow,
    surfaceContainer = ColorTokens.LightSurface,
    surfaceContainerHigh = ColorTokens.LightSurfaceHigh,
    outline = WarmOutline,
    outlineVariant = HairlineOutlineVariant,
    error = ErrorLight
)

private object ColorTokens {
    val LightSurfaceLow = androidx.compose.ui.graphics.Color(0xFFF6F1E9)
    val LightSurface = androidx.compose.ui.graphics.Color(0xFFF1EAE0)
    val LightSurfaceHigh = androidx.compose.ui.graphics.Color(0xFFEBE2D6)

    val DarkSurfaceLow = androidx.compose.ui.graphics.Color(0xFF1A1714)
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF221E1A)
    val DarkSurfaceHigh = androidx.compose.ui.graphics.Color(0xFF2C2722)
}

val FolioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FolioNotesTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themePreference) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalIsDarkTheme provides useDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FolioTypography,
            shapes = FolioShapes,
            content = content
        )
    }
}
