package com.directorx.editor.ui.editor

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CompactControlAccessibilityContractTest {

    @Test
    fun compact_effect_remove_action_keeps_a_48dp_button_surface() {
        val source = locate("app/src/main/java/com/directorx/editor/ui/editor/AudioMixerPanel.kt").readText()

        assertTrue(source.contains(".size(48.dp)"))
        assertTrue(source.contains("clickable(role = Role.Button, onClick = onRemove)"))
        assertTrue(source.contains("cd_mixer_remove_effect"))
        assertTrue(source.contains(".size(16.dp)"))
    }

    @Test
    fun radial_actions_keep_their_40dp_visual_inside_a_48dp_target() {
        val source = locate("app/src/main/java/com/directorx/editor/ui/editor/RadialActionMenu.kt").readText()

        assertTrue(source.contains(".size(TouchTarget.minimum)"))
        assertTrue(source.contains(".size(40.dp)"))
        assertTrue(source.contains("clickable(role = Role.Button)"))
        assertTrue(source.contains(".semantics { contentDescription = actionLabel }"))
    }

    @Test
    fun editor_search_action_uses_the_shared_touch_target_token() {
        val source = locate("app/src/main/java/com/directorx/editor/ui/editor/EditorScreen.kt").readText()

        assertTrue(source.contains("onClick = onSearch"))
        assertTrue(source.contains("modifier = Modifier.size(TouchTarget.minimum)"))
    }

    @Test
    fun text_and_glow_swatches_are_localized_radio_choices() {
        val source = locate("app/src/main/java/com/directorx/editor/ui/editor/TextEditorSheet.kt").readText()

        assertTrue(source.contains("private val textColorOptions"))
        assertTrue(source.contains("private val glowColorOptions"))
        assertTrue(source.contains(".size(48.dp)"))
        assertTrue(source.contains("role = Role.RadioButton"))
        assertTrue(source.contains("contentDescription = optionLabel"))
        assertTrue(source.contains(".size(34.dp)"))
        assertTrue(source.contains(".size(26.dp)"))
    }

    @Test
    fun timeline_trim_inputs_meet_the_48dp_touch_height() {
        val source = locate("app/src/main/java/com/directorx/editor/ui/editor/TimelineTrimControls.kt").readText()

        assertTrue(source.contains(".height(48.dp)"))
        assertTrue(!source.contains(".height(42.dp)"))
    }

    @Test
    fun canvas_gesture_hit_regions_use_density_aware_24dp_radii() {
        val speed = locate("app/src/main/java/com/directorx/editor/ui/editor/SpeedCurveEditor.kt").readText()
        val mask = locate("app/src/main/java/com/directorx/editor/ui/editor/MaskEditorPanel.kt").readText()
        val keyframes = locate("app/src/main/java/com/directorx/editor/ui/editor/KeyframeCurveEditor.kt").readText()
        val transform = locate("app/src/main/java/com/directorx/editor/ui/editor/TransformOverlay.kt").readText()

        assertTrue(speed.contains("with(LocalDensity.current) { 24.dp.toPx() }"))
        assertTrue(mask.contains("with(LocalDensity.current) { 24.dp.toPx() }"))
        assertTrue(keyframes.contains("with(LocalDensity.current) { 24.dp.toPx() }"))
        assertTrue(transform.contains("with(LocalDensity.current) { 24.dp.toPx() }"))
        assertTrue(!speed.contains("val hitRadius = 30f"))
        assertTrue(!mask.contains("val hitRadius = 30f"))
        assertTrue(!keyframes.contains("val hitRadius = 20f"))
        assertTrue(!transform.contains("HANDLE_RADIUS * 3"))
    }

    private fun locate(relativePath: String): File {
        var current = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        repeat(8) {
            val candidate = File(current, relativePath)
            if (candidate.isFile) return candidate
            current = current.parentFile ?: return@repeat
        }
        error("Could not locate $relativePath")
    }
}
