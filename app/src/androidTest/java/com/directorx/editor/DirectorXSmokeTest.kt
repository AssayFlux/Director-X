package com.directorx.editor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.directorx.editor.ui.DirectorXTestTags
import com.directorx.editor.engine.AppearanceMode
import com.directorx.editor.engine.DesktopOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.filters.SdkSuppress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class DirectorXSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>().apply {
        enableAccessibilityChecks()
    }

    @Before
    fun waitForInitialComposeHierarchy() {
        compose.waitForComposeHierarchy()
    }

    @Test
    fun projectEditorExportAndSettingsSurfacesOpen() {
        compose.onNodeWithTag(DirectorXTestTags.PROJECTS_SCREEN).assertIsDisplayed()
        compose.assertAccessibilityChecksPass()

        compose.onNodeWithTag(DirectorXTestTags.PROJECTS_CREATE_PROJECT).performClick()
        compose.onNodeWithTag(DirectorXTestTags.TEMPLATE_SHEET).assertIsDisplayed()
        compose.onNodeWithTag(DirectorXTestTags.TEMPLATE_BLANK)
            .performScrollTo()
            .performClick()

        compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_SCREEN)
        compose.waitForIdle()
        assertTrue(
            compose.onAllNodesWithTag(DirectorXTestTags.TUTORIAL_SCREEN)
                .fetchSemanticsNodes()
                .isEmpty()
        )
        dismissTutorialIfPresent()
        compose.assertAccessibilityChecksPass()

        compose.onNodeWithTag(DirectorXTestTags.EDITOR_EMPTY_ADD_MEDIA).assertIsDisplayed().performClick()
        compose.onNodeWithTag(DirectorXTestTags.MEDIA_PICKER_SHEET).assertIsDisplayed()
        compose.assertAccessibilityChecksPass()
        compose.onNodeWithTag(DirectorXTestTags.MEDIA_PICKER_CLOSE).performClick()

        compose.onNodeWithTag(DirectorXTestTags.EDITOR_EXPORT).assertIsDisplayed().performClick()
        compose.onNodeWithTag(DirectorXTestTags.EXPORT_SHEET).assertIsDisplayed()
        compose.onNodeWithTag(DirectorXTestTags.TIMELINE_EXCHANGE_IMPORT)
            .performScrollTo()
            .assertIsDisplayed()
        compose.assertAccessibilityChecksPass()
        compose.onNodeWithTag(DirectorXTestTags.EXPORT_CLOSE).performClick()

        compose.onNodeWithTag(DirectorXTestTags.EDITOR_BACK).performClick()
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.PROJECTS_SCREEN)
        compose.waitUntilNoNodesExist(DirectorXTestTags.EDITOR_SCREEN)

        compose.onNodeWithTag(DirectorXTestTags.PROJECTS_SETTINGS).performClick()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_SCREEN).assertIsDisplayed()
        compose.assertAccessibilityChecksPass()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_CATEGORY_APP).performClick()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_PRIVACY_OPEN)
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_PRIVACY_DASHBOARD).assertIsDisplayed()
        compose.assertAccessibilityChecksPass()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_PRIVACY_CLOSE).performClick()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_LICENSES_OPEN)
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_LICENSES_DIALOG).assertIsDisplayed()
        compose.assertAccessibilityChecksPass()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_LICENSES_CLOSE).performClick()

        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_REPLAY_TUTORIAL)
            .performScrollTo()
            .performClick()
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_SCREEN)
        compose.onNodeWithTag(DirectorXTestTags.TUTORIAL_SCREEN).assertIsDisplayed()
        compose.onNodeWithTag(DirectorXTestTags.TUTORIAL_SKIP).performClick()
        compose.onNodeWithTag(DirectorXTestTags.EDITOR_SCREEN).assertIsDisplayed()
        compose.onNodeWithTag(DirectorXTestTags.EDITOR_BACK).performClick()
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.SETTINGS_SCREEN)
        compose.waitUntilNoNodesExist(DirectorXTestTags.EDITOR_SCREEN)

        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_BACK)
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag(DirectorXTestTags.SETTINGS_BACK).performClick()

        compose.waitUntilAtLeastOneExists(DirectorXTestTags.PROJECTS_SCREEN)
        compose.waitUntilNoNodesExist(DirectorXTestTags.SETTINGS_SCREEN)
    }

    @Test
    @SdkSuppress(minSdkVersion = 33)
    fun pseudoLocalesRenderExpandedAndRtlExportSurfaces() {
        try {
            setApplicationLocale("en-XA")
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.PROJECTS_SCREEN)
            compose.onNodeWithTag(DirectorXTestTags.PROJECTS_SCREEN).assertIsDisplayed()
            compose.onNodeWithTag(DirectorXTestTags.PROJECTS_CREATE_PROJECT).performClick()
            compose.onNodeWithTag(DirectorXTestTags.TEMPLATE_BLANK)
                .performScrollTo()
                .performClick()
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_SCREEN)
            dismissTutorialIfPresent()
            compose.onNodeWithTag(DirectorXTestTags.EDITOR_EXPORT).performClick()
            compose.onNodeWithTag(DirectorXTestTags.EXPORT_SHEET).assertIsDisplayed()
            compose.assertAccessibilityChecksPass()
            assertTrue(compose.onRoot().captureToImage().width > 0)

            setApplicationLocale("ar-XB")
            compose.onNodeWithTag(DirectorXTestTags.EXPORT_SHEET).assertIsDisplayed()
            compose.assertAccessibilityChecksPass()
            assertEquals(
                android.view.View.LAYOUT_DIRECTION_RTL,
                compose.activity.resources.configuration.layoutDirection,
            )
            assertTrue(compose.onRoot().captureToImage().width > 0)
        } finally {
            compose.activity
                .getSystemService(android.app.LocaleManager::class.java)
                .applicationLocales = android.os.LocaleList.getEmptyLocaleList()
        }
    }

    @Test
    fun highContrastPhoneAndDesktopEditorSurfacesRender() {
        try {
            updateAppearance(AppearanceMode.HIGH_CONTRAST, DesktopOverride.FORCE_OFF)
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.PROJECTS_SCREEN)
            compose.onNodeWithTag(DirectorXTestTags.PROJECTS_CREATE_PROJECT).performClick()
            compose.onNodeWithTag(DirectorXTestTags.TEMPLATE_BLANK)
                .performScrollTo()
                .performClick()
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_SCREEN)
            dismissTutorialIfPresent()
            compose.assertAccessibilityChecksPass()
            assertTrue(compose.onRoot().captureToImage().width > 0)

            compose.onNodeWithTag(DirectorXTestTags.EDITOR_EMPTY_ADD_MEDIA).performClick()
            compose.onNodeWithTag(DirectorXTestTags.MEDIA_PICKER_SHEET).assertIsDisplayed()
            compose.assertAccessibilityChecksPass()
            assertTrue(compose.onRoot().captureToImage().width > 0)
            compose.onNodeWithTag(DirectorXTestTags.MEDIA_PICKER_CLOSE).performClick()

            compose.onNodeWithTag(DirectorXTestTags.EDITOR_EXPORT).performClick()
            compose.onNodeWithTag(DirectorXTestTags.EXPORT_SHEET).assertIsDisplayed()
            compose.assertAccessibilityChecksPass()
            assertTrue(compose.onRoot().captureToImage().width > 0)
            compose.onNodeWithTag(DirectorXTestTags.EXPORT_CLOSE).performClick()

            updateAppearance(AppearanceMode.HIGH_CONTRAST, DesktopOverride.FORCE_ON)
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_DESKTOP_SIDEBAR)
            compose.onNodeWithTag(DirectorXTestTags.EDITOR_DESKTOP_SIDEBAR).assertIsDisplayed()
            compose.assertAccessibilityChecksPass()
            assertTrue(compose.onRoot().captureToImage().width > 0)
            compose.onNodeWithTag(DirectorXTestTags.EDITOR_EXPORT).performClick()
            compose.onNodeWithTag(DirectorXTestTags.EXPORT_SHEET).assertIsDisplayed()
            compose.assertAccessibilityChecksPass()
            assertTrue(compose.onRoot().captureToImage().width > 0)
        } finally {
            updateAppearance(AppearanceMode.PRIMARY_BRAND, DesktopOverride.AUTO)
        }
    }

    private fun updateAppearance(appearanceMode: AppearanceMode, desktopOverride: DesktopOverride) {
        runBlocking {
            // Keep the preference round-trip bounded: legacy aliases are normalized by
            // mapPreferencesToAppSettings and must never leave a device test hung forever.
            withTimeout(15_000L) {
                compose.activity.settingsRepository.updateAppearanceMode(appearanceMode)
                compose.activity.settingsRepository.updateDesktopOverride(desktopOverride)
                compose.activity.settingsRepository.settings.first { settings ->
                    settings.appearanceMode == appearanceMode &&
                        settings.desktopModeOverride == desktopOverride
                }
            }
        }
        compose.waitForIdle()
        compose.waitForComposeHierarchy()
    }

    private fun setApplicationLocale(languageTag: String) {
        compose.activity
            .getSystemService(android.app.LocaleManager::class.java)
            .applicationLocales = android.os.LocaleList.forLanguageTags(languageTag)
        compose.waitUntil(timeoutMillis = 20_000L) {
            compose.activity.resources.configuration.locales[0].toLanguageTag() == languageTag
        }
        compose.waitForComposeHierarchy()
    }

    private fun dismissTutorialIfPresent() {
        runCatching {
            compose.waitUntil(timeoutMillis = 1_200L) {
                compose.onAllNodesWithTag(DirectorXTestTags.TUTORIAL_SKIP)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }
        val tutorialNodes = runCatching {
            compose.onAllNodesWithTag(DirectorXTestTags.TUTORIAL_SKIP).fetchSemanticsNodes()
        }.getOrDefault(emptyList())
        if (tutorialNodes.isNotEmpty()) {
            compose.onNodeWithTag(DirectorXTestTags.TUTORIAL_SKIP).performClick()
            compose.waitUntil(timeoutMillis = 5_000L) {
                compose.onAllNodesWithTag(DirectorXTestTags.TUTORIAL_SKIP)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        }
    }

}
