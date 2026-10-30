package com.novacut.editor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.novacut.editor.engine.AppearanceMode

// Director X vintage darkroom palette. Names are kept stable to avoid breaking
// existing editor modules; values now follow the warm-cream brand system.
object Mocha {
    val Midnight = Color(0xFFF5F0E6)       // Canvas: aged paper
    val Crust = Color(0xFFEAE3D2)          // Surface: warm beige
    val Mantle = Color(0xFFF0E9DB)
    val Base = Color(0xFFF5F0E6)
    val Panel = Color(0xFFEAE3D2)
    val PanelRaised = Color(0xFFE2D8C5)
    val PanelHighest = Color(0xFFD9CDB7)
    val CardStroke = Color(0xFFD3C8B7)
    val CardStrokeStrong = Color(0xFFC9BEAC)
    val Glow = Color(0x33C29B38)
    val GlowSoft = Color(0x225A6B57)
    val GlowWarm = Color(0x22C29B38)
    val Surface0 = Color(0xFFEAE3D2)
    val Surface1 = Color(0xFFE2D8C5)
    val Surface2 = Color(0xFFD9CDB7)
    val Overlay0 = Color(0xFFB9AF9F)
    val Overlay1 = Color(0xFF8C8275)
    val Overlay2 = Color(0xFF71675B)
    val Subtext0 = Color(0xFF655B50)
    val Subtext1 = Color(0xFF5B5147)
    val Text = Color(0xFF2C2621)
    // Muted editorial accents. Brand CTA and success are exact requested values.
    val Lavender = Color(0xFF9A8A9C)
    val Blue = Color(0xFF7B9295)
    val Sapphire = Color(0xFF66868A)
    val Sky = Color(0xFFC29B38)             // Vintage amber
    val Teal = Color(0xFF5A6B57)             // Dark sage
    val Green = Color(0xFF5A6B57)
    val Yellow = Color(0xFFC29B38)
    val Peach = Color(0xFFA98432)
    val Maroon = Color(0xFF8A5B4D)
    val Red = Color(0xFFA7473A)
    val Mauve = Color(0xFF96775C)
    val Pink = Color(0xFFA77C75)
    val Flamingo = Color(0xFFA77C75)
    val Rosewater = Color(0xFFD8C9B5)
}

/**
 * Stable media/category accents. Feature surfaces may use these to preserve
 * identity, but structural chrome and readable content must use
 * [LocalClearCutColors] so the high-contrast cream scheme can strengthen them.
 */
object ClearCutAccents {
    /** Stable neutral accent for content that is defined outside composition. */
    val Neutral = Mocha.Subtext0
    val Lavender = Mocha.Lavender
    val Blue = Mocha.Blue
    val Sapphire = Mocha.Sapphire
    val Sky = Mocha.Sky
    val Teal = Mocha.Teal
    val Green = Mocha.Green
    val Yellow = Mocha.Yellow
    val Peach = Mocha.Peach
    val Maroon = Mocha.Maroon
    val Red = Mocha.Red
    val Mauve = Mocha.Mauve
    val Pink = Mocha.Pink
    val Flamingo = Mocha.Flamingo
    val Rosewater = Mocha.Rosewater
}

private val ClearCutDarkColorScheme = lightColorScheme(
    primary = Mocha.Sky,
    onPrimary = Mocha.Text,
    primaryContainer = Color(0xFFE7D6A4),
    onPrimaryContainer = Mocha.Text,
    secondary = Mocha.Teal,
    onSecondary = Color(0xFFF5F0E6),
    secondaryContainer = Color(0xFFD5DDCD),
    onSecondaryContainer = Mocha.Text,
    tertiary = Color(0xFF9B8E7B),
    onTertiary = Mocha.Text,
    tertiaryContainer = Color(0xFFE5D8C6),
    onTertiaryContainer = Mocha.Text,
    error = Mocha.Red,
    onError = Color(0xFFFFF9F2),
    errorContainer = Color(0xFFF1D6CF),
    onErrorContainer = Color(0xFF64271F),
    background = Mocha.Midnight,
    onBackground = Mocha.Text,
    surface = Mocha.Base,
    onSurface = Mocha.Text,
    surfaceVariant = Mocha.Panel,
    onSurfaceVariant = Mocha.Subtext1,
    outline = Mocha.Overlay1,
    outlineVariant = Mocha.CardStroke,
    inverseSurface = Mocha.Text,
    inverseOnSurface = Mocha.Midnight,
    inversePrimary = Color(0xFFE7C76B),
    surfaceDim = Color(0xFFE4DCCB),
    surfaceBright = Color(0xFFFFFCF5),
    surfaceContainerLowest = Color(0xFFFFFCF5),
    surfaceContainerLow = Mocha.Mantle,
    surfaceContainer = Mocha.Panel,
    surfaceContainerHigh = Mocha.PanelRaised,
    surfaceContainerHighest = Mocha.PanelHighest
)

private val ClearCutHighContrastColorScheme = lightColorScheme(
    primary = Color(0xFFA77C14),
    onPrimary = Color(0xFF1D1915),
    primaryContainer = Color(0xFFE7D6A4),
    onPrimaryContainer = Color(0xFF1D1915),
    secondary = Color(0xFF5A6B57),
    onSecondary = Color(0xFFFFFCF5),
    secondaryContainer = Color(0xFFD5DDCD),
    onSecondaryContainer = Color(0xFF1D1915),
    tertiary = Color(0xFF8C5A3C),
    onTertiary = Color(0xFFFFFCF5),
    tertiaryContainer = Color(0xFFE5D8C6),
    onTertiaryContainer = Color(0xFF1D1915),
    error = Color(0xFF8F2E24),
    onError = Color(0xFFFFFCF5),
    errorContainer = Color(0xFFF1D6CF),
    onErrorContainer = Color(0xFF64271F),
    background = Color(0xFFF5F0E6),
    onBackground = Color(0xFF1D1915),
    surface = Color(0xFFF5F0E6),
    onSurface = Color(0xFF1D1915),
    surfaceVariant = Color(0xFFEAE3D2),
    onSurfaceVariant = Color(0xFF302922),
    outline = Color(0xFF554D45),
    outlineVariant = Color(0xFF665D52),
    inverseSurface = Color(0xFF1D1915),
    inverseOnSurface = Color(0xFFF5F0E6),
    inversePrimary = Color(0xFFE7C76B),
    surfaceDim = Color(0xFFE4DCCB),
    surfaceBright = Color(0xFFFFFCF5),
    surfaceContainerLowest = Color(0xFFFFFCF5),
    surfaceContainerLow = Color(0xFFF5F0E6),
    surfaceContainer = Color(0xFFEAE3D2),
    surfaceContainerHigh = Color(0xFFE2D8C5),
    surfaceContainerHighest = Color(0xFFD9CDB7)
)

data class ClearCutSemanticColors(
    val mode: AppearanceMode,
    val highContrast: Boolean,
    val background: Color,
    val backgroundMid: Color,
    val panel: Color,
    val panelRaised: Color,
    val panelHighest: Color,
    val cardStroke: Color,
    val cardStrokeStrong: Color,
    val text: Color,
    val subtext: Color,
    val subtextStrong: Color,
    val disabledText: Color,
    val accent: Color,
    val accentSecondary: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val focusRing: Color,
    val canvas: Color,
    val selectedSurface: Color,
    val disabledSurface: Color,
    val surfaceBase: Color,
    val surfaceLow: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val overlay: Color,
    val overlayStrong: Color,
    val onAccent: Color,
)

val LocalClearCutColors = staticCompositionLocalOf {
    ClearCutThemeDefaults.colorsFor(AppearanceMode.DARK)
}

object ClearCutThemeDefaults {
    /**
     * Director X ships a warm-cream editorial scheme and a high-contrast cream variant.
     * The legacy enum names remain for preference compatibility; labels are rebranded in UI.
     */
    fun resolveMode(mode: AppearanceMode): AppearanceMode = mode

    fun colorSchemeFor(resolvedMode: AppearanceMode) = when (resolvedMode) {
        AppearanceMode.HIGH_CONTRAST_DARK -> ClearCutHighContrastColorScheme
        AppearanceMode.DARK -> ClearCutDarkColorScheme
    }

    fun colorsFor(resolvedMode: AppearanceMode): ClearCutSemanticColors = when (resolvedMode) {
        AppearanceMode.HIGH_CONTRAST_DARK -> ClearCutSemanticColors(
            mode = resolvedMode,
            highContrast = true,
            background = Color(0xFFF5F0E6),
            backgroundMid = Color(0xFFF0E9DB),
            panel = Color(0xFFF5F0E6),
            panelRaised = Color(0xFFF0E9DB),
            panelHighest = Color(0xFFEAE3D2),
            cardStroke = Color(0xFF665D52),
            cardStrokeStrong = Color(0xFF554D45),
            text = Color(0xFF1D1915),
            subtext = Color(0xFF302922),
            subtextStrong = Color(0xFF2C2621),
            disabledText = Color(0xFF4B4238),
            accent = Color(0xFFA77C14),
            accentSecondary = Color(0xFF5A6B57),
            success = Color(0xFF5A6B57),
            warning = Color(0xFF946B12),
            danger = Color(0xFF8F2E24),
            focusRing = Color(0xFF2C2621),
            canvas = Color(0xFFF5F0E6),
            selectedSurface = Color(0xFFE7D6A4),
            disabledSurface = Color(0xFFD4C8B3),
            surfaceBase = Color(0xFFF5F0E6),
            surfaceLow = Color(0xFFEAE3D2),
            surface = Color(0xFFE2D8C5),
            surfaceHigh = Color(0xFFD9CDB7),
            overlay = Color(0xFF665D52),
            overlayStrong = Color(0xFF2C2621),
            onAccent = Color(0xFF1D1915),
        )
        AppearanceMode.DARK -> ClearCutSemanticColors(
            mode = resolvedMode,
            highContrast = false,
            background = Color(0xFFF5F0E6),
            backgroundMid = Color(0xFFF0E9DB),
            panel = Color(0xFFEAE3D2),
            panelRaised = Color(0xFFE2D8C5),
            panelHighest = Color(0xFFD9CDB7),
            cardStroke = Color(0xFFD3C8B7),
            cardStrokeStrong = Color(0xFFC9BEAC),
            text = Color(0xFF2C2621),
            subtext = Color(0xFF655B50),
            subtextStrong = Color(0xFF5B5147),
            disabledText = Color(0xFF817669),
            accent = Color(0xFFC29B38),
            accentSecondary = Color(0xFF5A6B57),
            success = Color(0xFF5A6B57),
            warning = Color(0xFFA98432),
            danger = Color(0xFFA7473A),
            focusRing = Color(0xFF8C6F23),
            canvas = Color(0xFFF5F0E6),
            selectedSurface = Color(0x33C29B38),
            disabledSurface = Color(0xFFE4DDCF),
            surfaceBase = Color(0xFFF5F0E6),
            surfaceLow = Color(0xFFEAE3D2),
            surface = Color(0xFFE2D8C5),
            surfaceHigh = Color(0xFFD9CDB7),
            overlay = Color(0xFFB9AF9F),
            overlayStrong = Color(0xFF8C8275),
            onAccent = Color(0xFF2C2621),
        )
    }

    fun contrastRatio(foreground: Color, background: Color): Double {
        val fg = relativeLuminance(foreground)
        val bg = relativeLuminance(background)
        val lighter = maxOf(fg, bg)
        val darker = minOf(fg, bg)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        val r = linearizedChannel(color.red)
        val g = linearizedChannel(color.green)
        val b = linearizedChannel(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun linearizedChannel(channel: Float): Double {
        val srgb = channel.toDouble()
        return if (srgb <= 0.03928) {
            srgb / 12.92
        } else {
            Math.pow((srgb + 0.055) / 1.055, 2.4)
        }
    }
}

private val ClearCutFontFamily = FontFamily.SansSerif
private val DirectorXDisplayFontFamily = FontFamily.Serif

private val ClearCutTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DirectorXDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DirectorXDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DirectorXDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DirectorXDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DirectorXDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ClearCutFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.2.sp
    )
)

private val ClearCutShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.xl),
    extraLarge = RoundedCornerShape(Radius.xxl),
)

@Composable
fun ClearCutTheme(
    appearanceMode: AppearanceMode = AppearanceMode.DARK,
    content: @Composable () -> Unit
) {
    val resolvedMode = ClearCutThemeDefaults.resolveMode(appearanceMode)
    CompositionLocalProvider(LocalClearCutColors provides ClearCutThemeDefaults.colorsFor(resolvedMode)) {
        MaterialTheme(
            colorScheme = ClearCutThemeDefaults.colorSchemeFor(resolvedMode),
            typography = ClearCutTypography,
            shapes = ClearCutShapes,
            content = content
        )
    }
}
