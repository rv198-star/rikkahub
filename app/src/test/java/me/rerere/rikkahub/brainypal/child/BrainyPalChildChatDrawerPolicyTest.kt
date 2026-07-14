package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildChatDrawerPolicyTest {
    @Test
    fun `child chat does not mount the generic conversation drawer`() {
        val layout = BrainyPalChildChatDrawerPolicy.layoutFor(childMode = true)

        assertFalse(layout.mountConversationDrawer)
        assertFalse(layout.showConversationHistory)
        assertFalse(layout.showHomeReturnAction)
        assertFalse(layout.showFooterHomeShortcut)
        assertFalse(layout.allowConversationMaintenanceActions)
        assertEquals(Screen.BrainyPalHome, layout.homeReturnTarget)
        assertEquals("返回 BrainyPal", layout.homeReturnLabel)
    }

    @Test
    fun `child chat top bar keeps return action without history affordance`() {
        val layout = BrainyPalChildChatDrawerPolicy.layoutFor(childMode = true)

        assertEquals(BrainyPalChildChatTopStartAction.ReturnHome, layout.topStartAction)
        assertFalse(layout.showTopBarHistoryAction)
        assertEquals("历史对话", layout.historyActionLabel)
    }

    @Test
    fun `standard drawer keeps existing RikkaHub navigation affordances`() {
        val layout = BrainyPalChildChatDrawerPolicy.layoutFor(childMode = false)

        assertTrue(layout.mountConversationDrawer)
        assertTrue(layout.showConversationHistory)
        assertFalse(layout.showHomeReturnAction)
        assertFalse(layout.showFooterHomeShortcut)
        assertTrue(layout.allowConversationMaintenanceActions)
        assertEquals(BrainyPalChildChatTopStartAction.OpenDrawer, layout.topStartAction)
        assertFalse(layout.showTopBarHistoryAction)
    }
}
