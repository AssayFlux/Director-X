package com.directorx.editor.ui.editor

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasScrollAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.directorx.editor.engine.AppearanceMode
import com.directorx.editor.model.Effect
import com.directorx.editor.model.EffectType
import com.directorx.editor.ui.theme.DirectorXTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EffectAdjustmentPanelLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun effectsLibraryRemainsScrollableInCompactViewport() {
        compose.setContent {
            DirectorXTheme(appearanceMode = AppearanceMode.PRIMARY_BRAND) {
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(300.dp),
                ) {
                    EffectsPanel(
                        selectedClip = null,
                        onAddEffect = {},
                        onClose = {},
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        compose.onNodeWithTag("premium-editor-panel-scroll-content")
            .assertHasScrollAction()
        compose.onNodeWithText("Effects").assertIsDisplayed()
    }

    @Test
    fun narrowShortViewportKeepsEveryEffectParameterReachable() {
        compose.setContent {
            DirectorXTheme(appearanceMode = AppearanceMode.PRIMARY_BRAND) {
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(300.dp),
                ) {
                    EffectAdjustmentPanel(
                        effect = Effect(
                            type = EffectType.CHROMA_KEY,
                            params = EffectType.defaultParams(EffectType.CHROMA_KEY),
                        ),
                        onUpdateParams = {},
                        onRemove = {},
                        onClose = {},
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        compose.onNodeWithTag("premium-editor-panel-scroll-content")
            .assertHasScrollAction()
        compose.onNodeWithText("Similarity").assertIsDisplayed()
        compose.onNodeWithText("Spill")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
