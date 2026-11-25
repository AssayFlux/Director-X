package com.directorx.editor.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimelineTrimControlsTest {
    @Test
    fun parsesMinuteSecondAndDecimalSecondInputs() {
        assertEquals(62_500L, parseTrimTime("1:02.5"))
        assertEquals(119_990L, parseTrimTime("1:59.99"))
        assertEquals(90_500L, parseTrimTime("90,5"))
    }

    @Test
    fun rejectsInvalidMinuteSecondFieldsAndNonFiniteValues() {
        assertNull(parseTrimTime("-1:30"))
        assertNull(parseTrimTime("1:-1"))
        assertNull(parseTrimTime("1:60"))
        assertNull(parseTrimTime("1:"))
        assertNull(parseTrimTime("NaN"))
        assertNull(parseTrimTime("Infinity"))
        assertNull(parseTrimTime("9223372036854775807:00"))
    }
}
