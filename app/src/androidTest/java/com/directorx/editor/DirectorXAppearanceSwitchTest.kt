package com.directorx.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.directorx.editor.engine.AppearanceMode
import com.directorx.editor.ui.theme.DirectorXFilterChip
import com.directorx.editor.ui.theme.DirectorXThemeDefaults
import com.directorx.editor.ui.theme.DirectorXStatusTag
import com.directorx.editor.ui.theme.DirectorXTheme
import com.directorx.editor.ui.theme.LocalDirectorXColors
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectorXAppearanceSwitchTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun liveAppearanceSwitchUpdatesSemanticColorsAndSharedChrome() {
        var appearanceMode by mutableStateOf(AppearanceMode.MIDNIGHT_CINEMA)

        composeRule.setContent {
            DirectorXTheme(appearanceMode = appearanceMode) {
                val colors = LocalDirectorXColors.current
                Column {
                    Text(
                        text = colors.mode.name,
                        modifier = Modifier.testTag("appearance-mode"),
                    )
                    Text(
                        text = colors.accent.toString(),
                        modifier = Modifier.testTag("appearance-accent"),
                    )
                    DirectorXStatusTag(
                        text = "Ready",
                        accent = colors.success,
                        modifier = Modifier.testTag("appearance-status-tag"),
                    )
                    DirectorXFilterChip(
                        text = "Selected",
                        selected = true,
                        onClick = {},
                        modifier = Modifier.testTag("appearance-filter-chip"),
                    )
                    Button(onClick = { appearanceMode = AppearanceMode.FOREST_ATELIER }) {
                        Text("Switch appearance")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("appearance-mode").assertTextEquals("MIDNIGHT_CINEMA")
        val midnightAccent = DirectorXThemeDefaults.colorsFor(AppearanceMode.MIDNIGHT_CINEMA).accent.toString()
        composeRule.onNodeWithTag("appearance-accent").assertTextEquals(midnightAccent)
        composeRule.onNodeWithTag("appearance-status-tag").assertExists()
        composeRule.onNodeWithTag("appearance-filter-chip").assertExists()

        composeRule.onNodeWithText("Switch appearance").performClick()

        composeRule.onNodeWithTag("appearance-mode").assertTextEquals("FOREST_ATELIER")
        val forestAccent = DirectorXThemeDefaults.colorsFor(AppearanceMode.FOREST_ATELIER).accent.toString()
        composeRule.onNodeWithTag("appearance-accent").assertTextEquals(forestAccent)
        assertNotEquals("The selected appearance must replace the semantic accent.", midnightAccent, forestAccent)
        composeRule.onNodeWithTag("appearance-status-tag").assertExists()
        composeRule.onNodeWithTag("appearance-filter-chip").assertExists()
    }
}
