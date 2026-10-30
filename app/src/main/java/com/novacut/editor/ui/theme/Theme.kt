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

/**
 * Ten independent Director X palettes. Semantic roles are deliberately centralized so
 * every Compose screen using LocalClearCutColors / MaterialTheme follows the selection.
 */
private data class DirectorXPalette(
    val canvas: Color, val panel: Color, val raised: Color, val highest: Color,
    val text: Color, val subtext: Color, val accent: Color, val secondary: Color,
    val tertiary: Color, val dark: Boolean = false
)

object ClearCutThemeDefaults {
    val availableThemes: List<AppearanceMode> = listOf(
        AppearanceMode.PRIMARY_BRAND, AppearanceMode.MIDNIGHT_CINEMA,
        AppearanceMode.FOREST_ATELIER, AppearanceMode.BURGUNDY_EDITORIAL,
        AppearanceMode.OCEAN_STUDIO, AppearanceMode.GRAPHITE_LUXE,
        AppearanceMode.SANDSTONE, AppearanceMode.LAVENDER_FILM,
        AppearanceMode.COPPER_STUDIO, AppearanceMode.ARCTIC_MINIMAL
    )

    private fun palette(mode: AppearanceMode): DirectorXPalette = when (mode) {
        AppearanceMode.MIDNIGHT_CINEMA -> DirectorXPalette(Color(0xFF11151D),Color(0xFF1C2330),Color(0xFF283344),Color(0xFF344257),Color(0xFFF1F3F7),Color(0xFFB5C0D0),Color(0xFFD5A84B),Color(0xFF79A7B8),Color(0xFFB18BC4),true)
        AppearanceMode.FOREST_ATELIER -> DirectorXPalette(Color(0xFFF0F3EA),Color(0xFFE1E9D9),Color(0xFFD0DDC8),Color(0xFFBECEB4),Color(0xFF243126),Color(0xFF526653),Color(0xFF52734F),Color(0xFF9A7847),Color(0xFF668B83))
        AppearanceMode.BURGUNDY_EDITORIAL -> DirectorXPalette(Color(0xFFF7EFED),Color(0xFFEEDFDA),Color(0xFFE2CBC6),Color(0xFFD6B7B1),Color(0xFF352321),Color(0xFF70534E),Color(0xFF873F4C),Color(0xFFB18A62),Color(0xFF6D596C))
        AppearanceMode.OCEAN_STUDIO -> DirectorXPalette(Color(0xFFEDF4F5),Color(0xFFDCEAEC),Color(0xFFC8DDE1),Color(0xFFB4D0D6),Color(0xFF1D3036),Color(0xFF4D6870),Color(0xFF286D7A),Color(0xFF4F8E91),Color(0xFFB28A55))
        AppearanceMode.GRAPHITE_LUXE -> DirectorXPalette(Color(0xFF17191C),Color(0xFF22262B),Color(0xFF2D333A),Color(0xFF3A424B),Color(0xFFF2F2EF),Color(0xFFBEC3C7),Color(0xFFC6A15B),Color(0xFF8C9BA7),Color(0xFF9A8C7C),true)
        AppearanceMode.SANDSTONE -> DirectorXPalette(Color(0xFFF4EBDD),Color(0xFFE9DCC8),Color(0xFFDDCBB1),Color(0xFFD0B995),Color(0xFF332B22),Color(0xFF6D5D4B),Color(0xFF9A6B35),Color(0xFF6E7660),Color(0xFF9B7665))
        AppearanceMode.LAVENDER_FILM -> DirectorXPalette(Color(0xFFF2EFF7),Color(0xFFE5DFEF),Color(0xFFD8CFE6),Color(0xFFCABFDB),Color(0xFF30283B),Color(0xFF665A76),Color(0xFF75618F),Color(0xFF9B7185),Color(0xFF647D91))
        AppearanceMode.COPPER_STUDIO -> DirectorXPalette(Color(0xFFF5EEE8),Color(0xFFE9DCD0),Color(0xFFDDC9B8),Color(0xFFD0B6A0),Color(0xFF332720),Color(0xFF70594B),Color(0xFF9A5838),Color(0xFFB17A48),Color(0xFF65766B))
        AppearanceMode.ARCTIC_MINIMAL -> DirectorXPalette(Color(0xFFF1F5F8),Color(0xFFE2EAF0),Color(0xFFD2DEE7),Color(0xFFC1D1DE),Color(0xFF202D38),Color(0xFF536675),Color(0xFF426F91),Color(0xFF5E8F9B),Color(0xFF8A789D))
        else -> DirectorXPalette(Color(0xFFF5F0E6),Color(0xFFEAE3D2),Color(0xFFE2D8C5),Color(0xFFD9CDB7),Color(0xFF2C2621),Color(0xFF655B50),Color(0xFFC29B38),Color(0xFF5A6B57),Color(0xFF96775C))
    }

    /** Legacy DARK identifiers resolve to the new primary brand theme. */
    fun resolveMode(mode: AppearanceMode): AppearanceMode = when (mode) {
        AppearanceMode.DARK, AppearanceMode.HIGH_CONTRAST_DARK, AppearanceMode.HIGH_CONTRAST -> AppearanceMode.PRIMARY_BRAND
        else -> mode
    }

    fun colorSchemeFor(resolvedMode: AppearanceMode) : androidx.compose.material3.ColorScheme {
        val p = palette(resolveMode(resolvedMode))
        val onAccent = if (contrastRatio(p.text,p.accent) >= 4.5) p.text else Color.White
        return if (p.dark) androidx.compose.material3.darkColorScheme(
            primary=p.accent,onPrimary=if (contrastRatio(Color.Black,p.accent)>contrastRatio(Color.White,p.accent)) Color.Black else Color.White,
            secondary=p.secondary,onSecondary=Color(0xFF101418),tertiary=p.tertiary,onTertiary=Color(0xFF101418),
            background=p.canvas,onBackground=p.text,surface=p.panel,onSurface=p.text,
            surfaceVariant=p.raised,onSurfaceVariant=p.subtext,outline=p.subtext,outlineVariant=p.highest,
            error=Color(0xFFFF8A80),onError=Color(0xFF2B0906),
            surfaceContainerLowest=p.canvas,surfaceContainerLow=p.panel,surfaceContainer=p.panel,
            surfaceContainerHigh=p.raised,surfaceContainerHighest=p.highest
        ) else lightColorScheme(
            primary=p.accent,onPrimary=onAccent,primaryContainer=p.highest,onPrimaryContainer=p.text,
            secondary=p.secondary,onSecondary=Color.White,secondaryContainer=p.raised,onSecondaryContainer=p.text,
            tertiary=p.tertiary,onTertiary=Color.White,tertiaryContainer=p.highest,onTertiaryContainer=p.text,
            error=Color(0xFFB3261E),onError=Color.White,background=p.canvas,onBackground=p.text,
            surface=p.canvas,onSurface=p.text,surfaceVariant=p.panel,onSurfaceVariant=p.subtext,
            outline=p.subtext,outlineVariant=p.highest,inverseSurface=p.text,inverseOnSurface=p.canvas,
            surfaceContainerLowest=p.canvas,surfaceContainerLow=p.canvas,surfaceContainer=p.panel,
            surfaceContainerHigh=p.raised,surfaceContainerHighest=p.highest
        )
    }

    fun colorsFor(resolvedMode: AppearanceMode): ClearCutSemanticColors {
        val mode=resolveMode(resolvedMode); val p=palette(mode)
        val hc=resolvedMode==AppearanceMode.HIGH_CONTRAST || resolvedMode==AppearanceMode.HIGH_CONTRAST_DARK
        val text=if(hc) Color(0xFF17130F) else p.text
        val sub=if(hc) Color(0xFF302922) else p.subtext
        val stroke=if(hc) Color(0xFF554D45) else p.highest
        return ClearCutSemanticColors(
            mode=mode,highContrast=hc,background=p.canvas,backgroundMid=p.panel,panel=p.panel,
            panelRaised=p.raised,panelHighest=p.highest,cardStroke=stroke,cardStrokeStrong=if(hc) text else p.subtext,
            text=text,subtext=sub,subtextStrong=p.subtext,disabledText=if(hc) Color(0xFF4B4238) else p.subtext,
            accent=p.accent,accentSecondary=p.secondary,success=Color(0xFF47734B),warning=Color(0xFF946B12),
            danger=Color(0xFFB3261E),focusRing=if(hc) text else p.accent,canvas=p.canvas,
            selectedSurface=p.raised,disabledSurface=p.panel,surfaceBase=p.canvas,surfaceLow=p.panel,
            surface=p.panel,surfaceHigh=p.raised,overlay=stroke,overlayStrong=text,onAccent=if(contrastRatio(text,p.accent)>=4.5) text else Color.White
        )
    }

    fun contrastRatio(foreground: Color, background: Color): Double {
        val fg=relativeLuminance(foreground); val bg=relativeLuminance(background)
        return (maxOf(fg,bg)+0.05)/(minOf(fg,bg)+0.05)
    }
    private fun relativeLuminance(color: Color): Double {
        fun channel(v: Float): Double { val c=v.toDouble(); return if(c<=0.04045)c/12.92 else Math.pow((c+0.055)/1.055,2.4) }
        return 0.2126*channel(color.red)+0.7152*channel(color.green)+0.0722*channel(color.blue)
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
    CompositionLocalProvider(LocalClearCutColors provides ClearCutThemeDefaults.colorsFor(appearanceMode)) {
        MaterialTheme(
            colorScheme = ClearCutThemeDefaults.colorSchemeFor(if (appearanceMode == AppearanceMode.HIGH_CONTRAST || appearanceMode == AppearanceMode.HIGH_CONTRAST_DARK) appearanceMode else resolvedMode),
            typography = ClearCutTypography,
            shapes = ClearCutShapes,
            content = content
        )
    }
}
