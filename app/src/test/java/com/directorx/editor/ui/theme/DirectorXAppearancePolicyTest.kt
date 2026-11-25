package com.directorx.editor.ui.theme

import com.directorx.editor.engine.AppearanceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectorXAppearancePolicyTest {

    @Test
    fun appearanceMenuOffersTenDistinctThemes() {
        assertEquals(10, DirectorXThemeDefaults.availableThemes.size)
        assertEquals(10, DirectorXThemeDefaults.availableThemes.distinct().size)
        assertEquals(AppearanceMode.PRIMARY_BRAND, DirectorXThemeDefaults.availableThemes.first())
        val schemes = DirectorXThemeDefaults.availableThemes.map { mode ->
            DirectorXThemeDefaults.colorSchemeFor(mode).background to
                DirectorXThemeDefaults.colorSchemeFor(mode).primary
        }
        assertEquals(10, schemes.distinct().size)
    }

    @Test
    fun legacyAppearanceValuesResolveWithoutBreakingSavedPreferences() {
        assertEquals(AppearanceMode.PRIMARY_BRAND, DirectorXThemeDefaults.resolveMode(AppearanceMode.DARK))
        assertEquals(AppearanceMode.PRIMARY_BRAND, DirectorXThemeDefaults.resolveMode(AppearanceMode.HIGH_CONTRAST_DARK))
        assertEquals(AppearanceMode.PRIMARY_BRAND, DirectorXThemeDefaults.resolveMode(AppearanceMode.HIGH_CONTRAST))
    }

    @Test
    fun highContrastSemanticTextMeetsWcagAa() {
        val colors = DirectorXThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        listOf(
            colors.background,
            colors.backgroundMid,
            colors.panel,
            colors.panelRaised,
            colors.panelHighest,
            colors.surfaceBase,
            colors.surfaceLow,
            colors.surface,
            colors.surfaceHigh,
        ).forEach { surface ->
            assertTrue(DirectorXThemeDefaults.contrastRatio(colors.text, surface) >= 4.5)
            assertTrue(DirectorXThemeDefaults.contrastRatio(colors.subtext, surface) >= 4.5)
        }
        assertTrue(DirectorXThemeDefaults.contrastRatio(colors.disabledText, colors.disabledSurface) >= 4.5)
    }

    @Test
    fun highContrastSemanticStrokesMeetNonTextFloor() {
        val colors = DirectorXThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        assertTrue(DirectorXThemeDefaults.contrastRatio(colors.cardStroke, colors.panel) >= 3.0)
        assertTrue(DirectorXThemeDefaults.contrastRatio(colors.cardStrokeStrong, colors.panel) >= 3.0)
        assertTrue(DirectorXThemeDefaults.contrastRatio(colors.cardStrokeStrong, colors.panelHighest) >= 3.0)
    }

    @Test
    fun lowEmphasisMochaTokensStayBelowSemanticIndicatorFloor() {
        val overlayOnPanel = DirectorXThemeDefaults.contrastRatio(Mocha.Overlay0, Mocha.PanelHighest)
        val strokeOnPanel = DirectorXThemeDefaults.contrastRatio(Mocha.CardStrokeStrong, Mocha.Panel)
        val highContrast = DirectorXThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        assertTrue(overlayOnPanel < 3.0)
        assertTrue(strokeOnPanel < 3.0)
        assertTrue(DirectorXThemeDefaults.contrastRatio(highContrast.cardStrokeStrong, highContrast.panel) >= 3.0)
    }

    @Test
    fun highContrastBrandActionsHaveReadableLabels() {
        val scheme = DirectorXThemeDefaults.colorSchemeFor(AppearanceMode.HIGH_CONTRAST_DARK)
        listOf(
            scheme.onPrimary to scheme.primary,
            scheme.onSecondary to scheme.secondary,
            scheme.onTertiary to scheme.tertiary,
            scheme.onError to scheme.error,
        ).forEach { (foreground, background) ->
            assertTrue(
                "Brand action label contrast failed for $background",
                DirectorXThemeDefaults.contrastRatio(foreground, background) >= 4.5,
            )
        }
    }

    @Test
    fun highContrastIndicatorsMeetNonTextFloor() {
        val colors = DirectorXThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        listOf(colors.overlay, colors.overlayStrong, colors.focusRing).forEach { indicator ->
            assertTrue(DirectorXThemeDefaults.contrastRatio(indicator, colors.panel) >= 3.0)
            assertTrue(DirectorXThemeDefaults.contrastRatio(indicator, colors.panelHighest) >= 3.0)
        }
    }

    @Test
    fun vintageCreamKeepsPrimaryTextReadable() {
        val colors = DirectorXThemeDefaults.colorsFor(AppearanceMode.DARK)

        assertTrue(DirectorXThemeDefaults.contrastRatio(colors.text, colors.panel) >= 4.5)
        assertTrue(DirectorXThemeDefaults.contrastRatio(colors.subtext, colors.panel) >= 4.5)
    }

    @Test
    fun radiusTokensStayWithinProfessionalGeometryCap() {
        listOf(Radius.xs, Radius.sm, Radius.md, Radius.lg, Radius.xl, Radius.xxl).forEach { radius ->
            assertTrue("Radius token exceeded 12dp: $radius", radius.value <= 12f)
        }
    }
}
