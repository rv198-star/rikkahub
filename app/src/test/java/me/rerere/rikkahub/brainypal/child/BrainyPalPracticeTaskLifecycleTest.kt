package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalPracticeTaskLifecycleTest {
    @Test
    fun `available pending and assigned tasks are accepted when opened from child task list`() {
        assertTrue(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("available"))
        assertTrue(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("pending"))
        assertTrue(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("assigned"))
    }

    @Test
    fun `active or review tasks are not reaccepted when reopened`() {
        assertFalse(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("accepted"))
        assertFalse(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("in_progress"))
        assertFalse(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("paused"))
        assertFalse(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("submitted"))
        assertFalse(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("reviewing"))
        assertFalse(BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen("completed"))
    }
}
