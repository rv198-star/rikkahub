package me.rerere.rikkahub.ui.context

import androidx.navigation3.runtime.NavKey
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildModePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigatorTest {
    @Test
    fun `child mode navigation sends blocked screens to settings fallback`() {
        val backStack = mutableListOf<NavKey>(Screen.Chat("chat-id"))
        val navigator = Navigator(
            backStack = backStack,
            childModePolicy = BrainyPalChildModePolicy.enabled(),
        )

        navigator.navigate(Screen.SettingProvider)

        assertEquals(listOf(Screen.Chat("chat-id"), Screen.Setting), backStack)
    }

    @Test
    fun `child mode clear navigation replaces blocked screen with settings fallback`() {
        val backStack = mutableListOf<NavKey>(Screen.Chat("chat-id"), Screen.BrainyPalPractice)
        val navigator = Navigator(
            backStack = backStack,
            childModePolicy = BrainyPalChildModePolicy.enabled(),
        )

        navigator.clearAndNavigate(Screen.Developer)

        assertEquals(listOf(Screen.Setting), backStack)
    }

    @Test
    fun `child mode navigation allows BrainyPal child webview`() {
        val backStack = mutableListOf<NavKey>(Screen.BrainyPalPractice)
        val navigator = Navigator(
            backStack = backStack,
            childModePolicy = BrainyPalChildModePolicy.enabled(),
        )
        val childWebView = Screen.WebView(url = "http://192.168.1.20:8000/child")

        navigator.navigate(childWebView)

        assertEquals(listOf(Screen.BrainyPalPractice, childWebView), backStack)
    }
}
