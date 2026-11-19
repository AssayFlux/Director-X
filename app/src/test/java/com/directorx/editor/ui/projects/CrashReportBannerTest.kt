package com.directorx.editor.ui.projects

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.directorx.editor.MainActivity
import com.directorx.editor.DirectorXApp
import com.directorx.editor.ui.ClearCutTestTags
import com.directorx.editor.ui.theme.DirectorXTheme
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Verifies the crash notice's three accessible actions independently of ActivityResultRegistry. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = DirectorXApp::class, sdk = [35], qualifiers = "w360dp-h800dp-xxhdpi")
class CrashReportBannerTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val occurredAt = System.currentTimeMillis() - 60 * 60 * 1000L
    private val notice = CrashReportNotice(
        kind = CrashReportKind.CRASH,
        occurredAtEpochMs = occurredAt,
        coversThroughEpochMs = occurredAt,
        detail = "java.lang.IllegalStateException",
        appVersion = "0.1.0",
    )

    @Test
    fun saveActionInvokesItsCallback() {
        val tapped = AtomicBoolean(false)
        showBanner(onSave = { tapped.set(true) })

        compose.onNodeWithTag(ClearCutTestTags.CRASH_REPORT_SAVE)
            .assertHasClickAction()
            .performClick()

        assertTrue("Save callback should run", tapped.get())
    }

    @Test
    fun copyActionInvokesItsCallback() {
        val tapped = AtomicBoolean(false)
        showBanner(onCopy = { tapped.set(true) })

        compose.onNodeWithTag(ClearCutTestTags.CRASH_REPORT_COPY)
            .assertHasClickAction()
            .performClick()
        compose.waitUntil(5_000L) { tapped.get() }

        assertTrue("Copy callback should run", tapped.get())
    }

    @Test
    fun dismissActionInvokesItsCallback() {
        val tapped = AtomicBoolean(false)
        showBanner(onDismiss = { tapped.set(true) })

        compose.onNodeWithTag(ClearCutTestTags.CRASH_REPORT_DISMISS)
            .assertHasClickAction()
            .performClick()
        compose.waitUntil(5_000L) { tapped.get() }

        assertTrue("Dismiss callback should run", tapped.get())
    }

    @Test
    fun saveActionIsDisabledWhileTheReportIsBeingWritten() {
        showBanner(saving = true)

        compose.onNodeWithTag(ClearCutTestTags.CRASH_REPORT_SAVE)
            .assertIsNotEnabled()
    }

    private fun showBanner(
        saving: Boolean = false,
        onSave: () -> Unit = {},
        onCopy: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        compose.setContent {
            DirectorXTheme {
                CrashReportBanner(
                    notice = notice,
                    saving = saving,
                    onSaveReport = onSave,
                    onCopySummary = onCopy,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}
