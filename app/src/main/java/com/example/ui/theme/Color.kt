package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.NoteColor

// Warm Editorial Studio Palette — Light
val TerracottaPrimary = Color(0xFFC2572D)
val TerracottaOnPrimary = Color(0xFFFFFFFF)
val TerracottaContainer = Color(0xFFFFDBD0)
val TerracottaOnContainer = Color(0xFF3E1203)

val SageSecondary = Color(0xFF47685B)
val SageOnSecondary = Color(0xFFFFFFFF)
val SageContainer = Color(0xFFC9EEDE)
val SageOnContainer = Color(0xFF062017)

val AmberTertiary = Color(0xFF8B5D00)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberContainer = Color(0xFFFFDEA8)
val AmberOnContainer = Color(0xFF2D1B00)

val AlabasterBackground = Color(0xFFFAF7F2)
val EspressoOnBackground = Color(0xFF1F1B16)
val IvorySurface = Color(0xFFFFFCF7)
val WarmSandSurfaceVariant = Color(0xFFEDE6DC)
val TaupeOnSurfaceVariant = Color(0xFF5D564E)
val WarmOutline = Color(0xFF8C8378)
val HairlineOutlineVariant = Color(0xFFE0D7CA)

// Warm Editorial Studio Palette — Dark
val TerracottaDarkPrimary = Color(0xFFFFB59B)
val TerracottaDarkOnPrimary = Color(0xFF5E1B04)
val TerracottaDarkContainer = Color(0xFF7D2E13)
val TerracottaDarkOnContainer = Color(0xFFFFDBD0)

val SageDarkSecondary = Color(0xFFADD2C2)
val SageDarkOnSecondary = Color(0xFF18372B)
val SageDarkContainer = Color(0xFF2F4F42)
val SageDarkOnContainer = Color(0xFFC9EEDE)

val AmberDarkTertiary = Color(0xFFF6BE57)
val AmberDarkOnTertiary = Color(0xFF482E00)
val AmberDarkContainer = Color(0xFF674300)
val AmberDarkOnContainer = Color(0xFFFFDEA8)

val CharcoalBackground = Color(0xFF141210)
val WarmIvoryOnBackground = Color(0xFFF3EFE9)
val EspressoSurface = Color(0xFF1E1B18)
val DarkTaupeSurfaceVariant = Color(0xFF2D2823)
val MutedStoneOnSurfaceVariant = Color(0xFFC8BFB4)
val DarkOutline = Color(0xFF948B80)
val DarkHairlineOutlineVariant = Color(0xFF3C362F)

val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)

data class NoteSurfaceTokens(
    val container: Color,
    val border: Color,
    val swatch: Color
)

fun NoteColor.surfaceTokens(isDarkTheme: Boolean): NoteSurfaceTokens {
    return if (isDarkTheme) {
        when (this) {
            NoteColor.DEFAULT -> NoteSurfaceTokens(
                container = Color(0xFF211E1A),
                border = Color(0xFF3A342D),
                swatch = Color(0xFF3A342D)
            )
            NoteColor.SAND -> NoteSurfaceTokens(
                container = Color(0xFF2C251D),
                border = Color(0xFF4E4030),
                swatch = Color(0xFFC79D6D)
            )
            NoteColor.SAGE -> NoteSurfaceTokens(
                container = Color(0xFF1C2923),
                border = Color(0xFF324B3F),
                swatch = Color(0xFF77A892)
            )
            NoteColor.SKY -> NoteSurfaceTokens(
                container = Color(0xFF1A252F),
                border = Color(0xFF2E4357),
                swatch = Color(0xFF739EC2)
            )
            NoteColor.ROSE -> NoteSurfaceTokens(
                container = Color(0xFF2E1E20),
                border = Color(0xFF523337),
                swatch = Color(0xFFD1838A)
            )
            NoteColor.LAVENDER -> NoteSurfaceTokens(
                container = Color(0xFF262130),
                border = Color(0xFF443957),
                swatch = Color(0xFFA794CC)
            )
            NoteColor.AMBER -> NoteSurfaceTokens(
                container = Color(0xFF2D2516),
                border = Color(0xFF534324),
                swatch = Color(0xFFD9AE54)
            )
        }
    } else {
        when (this) {
            NoteColor.DEFAULT -> NoteSurfaceTokens(
                container = Color(0xFFFFFFFF),
                border = Color(0xFFE5DDD1),
                swatch = Color(0xFFF5EFE6)
            )
            NoteColor.SAND -> NoteSurfaceTokens(
                container = Color(0xFFFBF0E1),
                border = Color(0xFFEAD4B7),
                swatch = Color(0xFFE6BE8A)
            )
            NoteColor.SAGE -> NoteSurfaceTokens(
                container = Color(0xFFE9F3ED),
                border = Color(0xFFC6DFD0),
                swatch = Color(0xFF80B39A)
            )
            NoteColor.SKY -> NoteSurfaceTokens(
                container = Color(0xFFE8F1F8),
                border = Color(0xFFC3DAEB),
                swatch = Color(0xFF7AA7CC)
            )
            NoteColor.ROSE -> NoteSurfaceTokens(
                container = Color(0xFFFAECEE),
                border = Color(0xFFEDC7CC),
                swatch = Color(0xFFDB8D96)
            )
            NoteColor.LAVENDER -> NoteSurfaceTokens(
                container = Color(0xFFF1ECF9),
                border = Color(0xFFD8CBEF),
                swatch = Color(0xFFA994D1)
            )
            NoteColor.AMBER -> NoteSurfaceTokens(
                container = Color(0xFFFDF5DF),
                border = Color(0xFFF1DDAA),
                swatch = Color(0xFFE3B857)
            )
        }
    }
}
