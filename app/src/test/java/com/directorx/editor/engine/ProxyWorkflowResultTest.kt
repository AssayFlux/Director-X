package com.directorx.editor.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyWorkflowResultTest {
    @Test
    fun proxyBatchIsCompleteOnlyWhenNoClipFailed() {
        assertTrue(ProxyWorkflowEngine.ProxyBatchResult(4, 3, 1, 0).isComplete)
        assertFalse(ProxyWorkflowEngine.ProxyBatchResult(4, 2, 1, 1).isComplete)
    }

    @Test
    fun proxyDeleteIsCompleteOnlyWhenNoFileDeletionFailed() {
        assertTrue(ProxyWorkflowEngine.ProxyDeleteResult(3, 0, 2).isComplete)
        assertFalse(ProxyWorkflowEngine.ProxyDeleteResult(2, 1, 0).isComplete)
    }
}
