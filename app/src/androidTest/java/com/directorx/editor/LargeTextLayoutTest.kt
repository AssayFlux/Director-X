package com.directorx.editor

import android.os.ParcelFileDescriptor
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.directorx.editor.ui.DirectorXTestTags
import com.directorx.editor.ui.editor.PanelId
import kotlin.math.abs
import kotlin.math.roundToInt
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 37)
class LargeTextLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>().apply {
        enableAccessibilityChecks()
    }

    @Before
    fun resetDisplayBeforeTest() {
        resetDisplay()
        recreateActivity()
        compose.waitForComposeHierarchy()
    }

    @After
    fun restoreDisplayAfterTest() {
        resetDisplay()
    }

    @Test
    fun fontScale200_phoneSurfacesRemainUsable() = verifyLayout(2.0f, Viewport.PHONE)

    @Test
    fun fontScale300_phoneSurfacesRemainUsable() = verifyLayout(3.0f, Viewport.PHONE)

    @Test
    fun fontScale200_largeScreenSurfacesRemainUsable() = verifyLayout(2.0f, Viewport.LARGE)

    @Test
    fun fontScale300_largeScreenSurfacesRemainUsable() = verifyLayout(3.0f, Viewport.LARGE)

    @Test
    fun fontScale200_desktopSurfacesRemainUsable() = verifyLayout(2.0f, Viewport.DESKTOP)

    @Test
    fun fontScale300_desktopSurfacesRemainUsable() = verifyLayout(3.0f, Viewport.DESKTOP)

    private fun verifyLayout(fontScale: Float, viewport: Viewport) {
        val label = "font-${(fontScale * 100).roundToInt()}-${viewport.label}"
        try {
            configureDisplay(fontScale, viewport)
            openBlankEditor()
            assertSurface(
                label = "$label-editor",
                rootTag = DirectorXTestTags.EDITOR_SCREEN,
                requiredActionTags = listOf(
                    DirectorXTestTags.EDITOR_BACK,
                    DirectorXTestTags.EDITOR_EXPORT,
                    DirectorXTestTags.EDITOR_EMPTY_ADD_MEDIA,
                ),
            )

            compose.onNodeWithTag(DirectorXTestTags.EDITOR_EXPORT).performClick()
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.EXPORT_SHEET)
            assertSurface(
                label = "$label-export",
                rootTag = DirectorXTestTags.EXPORT_SHEET,
                requiredActionTags = listOf(
                    DirectorXTestTags.EXPORT_CLOSE,
                    DirectorXTestTags.EXPORT_PRIMARY_ACTION,
                ),
            )
            compose.onNodeWithTag(DirectorXTestTags.EXPORT_CLOSE).performClick()
            waitForPanelClosed(PanelId.EXPORT_SHEET)

            openProjectTool("media_manager", DirectorXTestTags.MEDIA_MANAGER_PANEL)
            assertSurface(
                label = "$label-media-manager",
                rootTag = DirectorXTestTags.MEDIA_MANAGER_PANEL,
                requiredActionTags = listOf(DirectorXTestTags.MEDIA_MANAGER_CLOSE),
            )
            compose.onNodeWithTag(DirectorXTestTags.MEDIA_MANAGER_CLOSE).performClick()
            waitForPanelClosed(PanelId.MEDIA_MANAGER)

            openProjectTool("batch_export", DirectorXTestTags.BATCH_EXPORT_PANEL)
            assertSurface(
                label = "$label-batch-export",
                rootTag = DirectorXTestTags.BATCH_EXPORT_PANEL,
                requiredActionTags = listOf(DirectorXTestTags.BATCH_EXPORT_CLOSE),
            )
            compose.onNodeWithTag(DirectorXTestTags.BATCH_EXPORT_CLOSE).performClick()
            waitForPanelClosed(PanelId.BATCH_EXPORT)

            compose.onNodeWithTag(DirectorXTestTags.EDITOR_BACK).performClick()
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.PROJECTS_SCREEN)
            compose.onNodeWithTag(DirectorXTestTags.PROJECTS_SETTINGS)
                .performScrollTo()
                .assertIsDisplayed()
                .performClick()
            compose.waitUntilAtLeastOneExists(DirectorXTestTags.SETTINGS_SCREEN)
            assertSurface(
                label = "$label-settings",
                rootTag = DirectorXTestTags.SETTINGS_SCREEN,
                requiredActionTags = listOf(DirectorXTestTags.SETTINGS_BACK),
            )
        } catch (error: Throwable) {
            throw compose.failureWithQaDiagnostics(label, error)
        }
    }

    private fun openBlankEditor() {
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.PROJECTS_SCREEN)
        compose.onNodeWithTag(DirectorXTestTags.PROJECTS_CREATE_PROJECT)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.TEMPLATE_SHEET)
        compose.onNodeWithTag(DirectorXTestTags.TEMPLATE_GRID)
            .performScrollTo()
            .performScrollToNode(hasTestTag(DirectorXTestTags.TEMPLATE_BLANK))
        compose.onNodeWithTag(DirectorXTestTags.TEMPLATE_BLANK)
            .assertIsDisplayed()
            .performClick()
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_SCREEN)
        val tutorialNodes = runCatching {
            compose.onAllNodesWithTag(DirectorXTestTags.TUTORIAL_SKIP).fetchSemanticsNodes()
        }.getOrDefault(emptyList())
        if (tutorialNodes.isNotEmpty()) {
            compose.onNodeWithTag(DirectorXTestTags.TUTORIAL_SKIP).performClick()
        }
    }

    private fun openProjectTool(actionId: String, panelTag: String) {
        compose.onNodeWithTag(
            DirectorXTestTags.EDITOR_TOOL_TAB_PREFIX + "project_tools",
        ).performClick()
        compose.waitUntilAtLeastOneExists(DirectorXTestTags.EDITOR_TOOL_ACTION_LIST)
        val actionTag = DirectorXTestTags.EDITOR_TOOL_ACTION_PREFIX + actionId
        compose.onNodeWithTag(DirectorXTestTags.EDITOR_TOOL_ACTION_LIST)
            .performScrollToNode(hasTestTag(actionTag))
        compose.waitUntilAtLeastOneExists(actionTag)
        compose.onNodeWithTag(actionTag)
            .performScrollTo()
            .performClick()
        compose.waitUntilAtLeastOneExists(panelTag)
    }

    private fun waitForPanelClosed(panel: PanelId) {
        compose.waitUntil(timeoutMillis = 5_000L) {
            compose.onNodeWithTag(DirectorXTestTags.EDITOR_SCREEN)
                .fetchSemanticsNode()
                .config
                .getOrNull(DirectorXTestTags.OPEN_PANEL_IDS)
                ?.contains(panel.name) == false
        }
        compose.waitForIdle()
    }

    private fun assertSurface(
        label: String,
        rootTag: String,
        requiredActionTags: List<String>,
    ) {
        compose.onNodeWithTag(rootTag).assertIsDisplayed()
        val bounds = requiredActionTags.map { tag ->
            val interactions = compose.onAllNodesWithTag(tag)
            val nodes = interactions.fetchSemanticsNodes(atLeastOneRootRequired = true)
            check(nodes.isNotEmpty()) { "Required action is missing: $tag" }
            val activeIndex = nodes.indices.minBy { index ->
                abs(nodes[index].boundsInWindow.top)
            }
            val interaction = interactions[activeIndex]
            if (runCatching { interaction.assertIsDisplayed() }.isFailure) {
                interaction.performScrollTo()
            }
            interaction.assertIsDisplayed()
            tag to interaction.fetchSemanticsNode().boundsInWindow
        }
        bounds.forEachIndexed { index, first ->
            bounds.drop(index + 1).forEach { second ->
                check(!first.second.intersects(second.second)) {
                    "Required actions overlap: ${first.first}=${first.second}, " +
                        "${second.first}=${second.second}"
                }
            }
        }
        compose.assertAccessibilityChecksPass(label)
    }

    private fun configureDisplay(fontScale: Float, viewport: Viewport) {
        shell("wm density $TEST_DENSITY_DPI")
        shell("wm size ${viewport.widthDp}x${viewport.heightDp}")
        shell("settings put system font_scale $fontScale")
        shell("am broadcast -a android.intent.action.CONFIGURATION_CHANGED")
        recreateActivity()
        compose.waitUntil(timeoutMillis = 30_000L) {
            runCatching {
                val configuration = compose.activity.resources.configuration
                abs(configuration.fontScale - fontScale) < 0.05f &&
                    abs(configuration.screenWidthDp - viewport.widthDp) <= 24
            }.getOrDefault(false)
        }
        compose.waitForComposeHierarchy()
    }

    private fun resetDisplay() {
        shell("wm size reset")
        shell("wm density reset")
        shell("settings put system font_scale 1.0")
        shell("am broadcast -a android.intent.action.CONFIGURATION_CHANGED")
    }

    private fun recreateActivity() {
        val activity = compose.activity
        activity.runOnUiThread { activity.recreate() }
    }

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor)
            .bufferedReader()
            .use { reader -> reader.readText() }
    }

    private enum class Viewport(
        val label: String,
        val widthDp: Int,
        val heightDp: Int,
    ) {
        PHONE("phone", 360, 800),
        LARGE("large", 600, 800),
        DESKTOP("desktop", 1000, 700),
    }

    private companion object {
        const val TEST_DENSITY_DPI = 160
    }

    private fun Rect.intersects(other: Rect): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom
}
