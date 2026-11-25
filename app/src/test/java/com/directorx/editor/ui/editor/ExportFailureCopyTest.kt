package com.directorx.editor.ui.editor

import com.directorx.editor.R
import com.directorx.editor.engine.ExportFailureCause
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Seven distinct terminal export failures used to collapse into one generic
 * sentence, in a project whose central claim is that exports do not lie. Each
 * cause must reach the user as its own message with its own remediation.
 */
class ExportFailureCopyTest {

    @Test
    fun everyTerminalCauseHasItsOwnMessage() {
        val specific = ExportFailureCause.entries
            .filter { it != ExportFailureCause.UNKNOWN }

        val messages = specific.map { exportFailureCopyFor(it).messageRes }
        assertEquals(
            "each cause needs distinct copy: ${specific.zip(messages)}",
            specific.size,
            messages.toSet().size
        )
        assertTrue(
            "no specific cause may reuse the generic export-failed string",
            messages.none { it == R.string.export_video_failed_message }
        )
    }

    @Test
    fun everyCauseCarriesARemediationLine() {
        ExportFailureCause.entries.forEach { cause ->
            val copy = exportFailureCopyFor(cause)
            assertTrue("$cause has no message", copy.messageRes != 0)
            assertTrue("$cause has no remediation", copy.remediationRes != 0)
            assertTrue("$cause reuses its message as remediation", copy.messageRes != copy.remediationRes)
        }
    }

    @Test
    fun onlyTheUnknownCauseFallsBackToGenericCopy() {
        assertEquals(
            R.string.export_video_failed_message,
            exportFailureCopyFor(ExportFailureCause.UNKNOWN).messageRes
        )
        assertEquals(
            "a missing cause is treated as unknown, not as a specific failure",
            exportFailureCopyFor(ExportFailureCause.UNKNOWN),
            exportFailureCopyFor(null)
        )
    }

    @Test
    fun theCausesTheEngineComputesAreAllRepresented() {
        // The failures named in the audit: transformer error, zero-byte output,
        // failed verification, stall, unsupported standalone audio codec, subtitle
        // burn-in failure, and storage refusal.
        val required = listOf(
            ExportFailureCause.ENCODER_FAILED,
            ExportFailureCause.EMPTY_OUTPUT,
            ExportFailureCause.VERIFICATION_FAILED,
            ExportFailureCause.STALLED,
            ExportFailureCause.AUDIO_ENCODE_FAILED,
            ExportFailureCause.SUBTITLE_BURN_IN_FAILED,
            ExportFailureCause.STORAGE,
            ExportFailureCause.GPU_EFFECT_DEGRADED,
        )

        assertTrue(ExportFailureCause.entries.containsAll(required))
        assertEquals(required.size, required.map { exportFailureCopyFor(it).messageRes }.toSet().size)
    }
}
