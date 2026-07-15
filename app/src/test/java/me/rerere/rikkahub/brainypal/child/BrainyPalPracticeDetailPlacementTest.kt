package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalPracticeDetailPlacementTest {
    @Test
    fun detailPaneAppearsAfterOverviewAndTaskList() {
        assertEquals(2, BrainyPalPracticeDetailPlacement.DETAIL_ITEM_INDEX)
    }
}
