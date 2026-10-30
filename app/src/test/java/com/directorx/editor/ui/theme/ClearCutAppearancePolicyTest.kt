package com.directorx.editor.ui.theme

import com.directorx.editor.engine.AppearanceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearCutAppearancePolicyTest {

    @Test
    fun appearanceMenuOffersTenDistinctThemes() {
        assertEquals(10, ClearCutThemeDefaults.availableThemes.size)
        assertEquals(10, ClearCutThemeDefaults.availableThemes.distinct().size)
        assertEquals(AppearanceMode.PRIMARY_BRAND, ClearCutThemeDefaults.availableThemes.first())
        val schemes = ClearCutThemeDefaults.availableThemes.map { mode ->
            ClearCutThemeDefaults.colorSchemeFor(mode).background to
                ClearCutThemeDefaults.colorSchemeFor(mode).primary
        }
        assertEquals(10, schemes.distinct().size)
    }

    @Test
    fun legacyAppearanceValuesResolveWithoutBreakingSavedPreferences() {
        assertEquals(AppearanceMode.PRIMARY_BRAND, ClearCutThemeDefaults.resolveMode(AppearanceMode.DARK))
        assertEquals(AppearanceMode.PRIMARY_BRAND, ClearCutThemeDefaults.resolveMode(AppearanceMode.HIGH_CONTRAST_DARK))
        assertEquals(AppearanceMode.PRIMARY_BRAND, ClearCutThemeDefaults.resolveMode(AppearanceMode.HIGH_CONTRAST))
    }

    @Test
    fun highContrastSemanticTextMeetsWcagAa() {
        val colors = ClearCutThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

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
            assertTrue(ClearCutThemeDefaults.contrastRatio(colors.text, surface) >= 4.5)
            assertTrue(ClearCutThemeDefaults.contrastRatio(colors.subtext, surface) >= 4.5)
        }
        assertTrue(ClearCutThemeDefaults.contrastRatio(colors.disabledText, colors.disabledSurface) >= 4.5)
    }

    @Test
    fun highContrastSemanticStrokesMeetNonTextFloor() {
        val colors = ClearCutThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        assertTrue(ClearCutThemeDefaults.contrastRatio(colors.cardStroke, colors.panel) >= 3.0)
        assertTrue(ClearCutThemeDefaults.contrastRatio(colors.cardStrokeStrong, colors.panel) >= 3.0)
        assertTrue(ClearCutThemeDefaults.contrastRatio(colors.cardStrokeStrong, colors.panelHighest) >= 3.0)
    }

    @Test
    fun lowEmphasisMochaTokensStayBelowSemanticIndicatorFloor() {
        val overlayOnPanel = ClearCutThemeDefaults.contrastRatio(Mocha.Overlay0, Mocha.PanelHighest)
        val strokeOnPanel = ClearCutThemeDefaults.contrastRatio(Mocha.CardStrokeStrong, Mocha.Panel)
        val highContrast = ClearCutThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        assertTrue(overlayOnPanel < 3.0)
        assertTrue(strokeOnPanel < 3.0)
        assertTrue(ClearCutThemeDefaults.contrastRatio(highContrast.cardStrokeStrong, highContrast.panel) >= 3.0)
    }

    @Test
    fun highContrastBrandActionsHaveReadableLabels() {
        val scheme = ClearCutThemeDefaults.colorSchemeFor(AppearanceMode.HIGH_CONTRAST_DARK)
        listOf(
            scheme.onPrimary to scheme.primary,
            scheme.onSecondary to scheme.secondary,
            scheme.onTertiary to scheme.tertiary,
            scheme.onError to scheme.error,
        ).forEach { (foreground, background) ->
            assertTrue(
                "Brand action label contrast failed for $background",
                ClearCutThemeDefaults.contrastRatio(foreground, background) >= 4.5,
            )
        }
    }

    @Test
    fun highContrastIndicatorsMeetNonTextFloor() {
        val colors = ClearCutThemeDefaults.colorsFor(AppearanceMode.HIGH_CONTRAST_DARK)

        listOf(colors.overlay, colors.overlayStrong, colors.focusRing).forEach { indicator ->
            assertTrue(ClearCutThemeDefaults.contrastRatio(indicator, colors.panel) >= 3.0)
            assertTrue(ClearCutThemeDefaults.contrastRatio(indicator, colors.panelHighest) >= 3.0)
        }
    }

    @Test
    fun vintageCreamKeepsPrimaryTextReadable() {
        val colors = ClearCutThemeDefaults.colorsFor(AppearanceMode.DARK)

        assertTrue(ClearCutThemeDefaults.contrastRatio(colors.text, colors.panel) >= 4.5)
        assertTrue(ClearCutThemeDefaults.contrastRatio(colors.subtext, colors.panel) >= 4.5)
    }

    @Test
    fun radiusTokensStayWithinProfessionalGeometryCap() {
        listOf(Radius.xs, Radius.sm, Radius.md, Radius.lg, Radius.xl, Radius.xxl).forEach { radius ->
            assertTrue("Radius token exceeded 12dp: $radius", radius.value <= 12f)
        }
    }
}
