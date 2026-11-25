package com.directorx.editor.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppChromeButtonLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun primaryAndSecondaryButtonLabelsRemainCenteredInsideTouchTargets() {
        compose.setContent {
            DirectorXTheme {
                Column(Modifier.padding(16.dp)) {
                    DirectorXPrimaryButton(
                        text = "Primary action",
                        onClick = {},
                        modifier = Modifier.testTag("primary-button"),
                    )
                    DirectorXSecondaryButton(
                        text = "Secondary action",
                        onClick = {},
                        modifier = Modifier.testTag("secondary-button"),
                    )
                }
            }
        }

        assertLabelCentered("primary-button", "Primary action")
        assertLabelCentered("secondary-button", "Secondary action")
    }

    private fun assertLabelCentered(buttonTag: String, label: String) {
        val buttonBounds = compose.onNodeWithTag(buttonTag)
            .fetchSemanticsNode().boundsInRoot
        val labelBounds = compose.onNodeWithText(label, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot

        assertTrue(
            "$label should occupy its intrinsic text height, not the whole touch target",
            labelBounds.height < buttonBounds.height * 0.7f,
        )
        val buttonCenter = (buttonBounds.top + buttonBounds.bottom) / 2f
        val labelCenter = (labelBounds.top + labelBounds.bottom) / 2f
        assertTrue(
            "$label should be vertically centered in its button",
            abs(buttonCenter - labelCenter) < buttonBounds.height * 0.15f,
        )
    }
}
