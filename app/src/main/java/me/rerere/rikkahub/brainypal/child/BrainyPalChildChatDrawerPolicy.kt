package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.Screen

enum class BrainyPalChildChatTopStartAction {
    OpenDrawer,
    ReturnHome,
}

data class BrainyPalChildChatDrawerLayout(
    val showConversationHistory: Boolean,
    val showHomeReturnAction: Boolean,
    val showFooterHomeShortcut: Boolean,
    val showTopBarHistoryAction: Boolean,
    val allowConversationMaintenanceActions: Boolean,
    val homeReturnTarget: Screen,
    val homeReturnLabel: String,
    val topStartAction: BrainyPalChildChatTopStartAction,
    val historyActionLabel: String,
)

object BrainyPalChildChatDrawerPolicy {
    fun layoutFor(childMode: Boolean): BrainyPalChildChatDrawerLayout {
        return BrainyPalChildChatDrawerLayout(
            showConversationHistory = true,
            showHomeReturnAction = childMode,
            showFooterHomeShortcut = false,
            showTopBarHistoryAction = childMode,
            allowConversationMaintenanceActions = !childMode,
            homeReturnTarget = Screen.BrainyPalHome,
            homeReturnLabel = "返回 BrainyPal",
            topStartAction = if (childMode) {
                BrainyPalChildChatTopStartAction.ReturnHome
            } else {
                BrainyPalChildChatTopStartAction.OpenDrawer
            },
            historyActionLabel = "历史对话",
        )
    }
}
