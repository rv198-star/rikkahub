package me.rerere.rikkahub.ui.context

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildModePolicy

class Navigator(
    private val backStack: MutableList<NavKey>,
    private val childModePolicy: BrainyPalChildModePolicy = BrainyPalChildModePolicy.disabled(),
) {
    fun navigate(screen: Screen, builder: NavigateOptionsBuilder.() -> Unit = {}) {
        val decision = childModePolicy.evaluateScreen(screen)
        if (!decision.allowed) {
            navigateToChildSafeFallback(decision.fallbackScreen)
            return
        }

        val options = NavigateOptionsBuilder().apply(builder)

        options.popUpToScreen?.let { target ->
            val targetIndex = backStack.indexOfLast { it == target }
            if (targetIndex != -1) {
                val removeFromIndex = if (options.popUpToInclusive) targetIndex else targetIndex + 1
                repeat(backStack.size - removeFromIndex) {
                    backStack.removeLastOrNull()
                }
            }
        }

        if (options.launchSingleTop && backStack.lastOrNull() == screen) {
            return
        }

        backStack.add(screen)
    }

    fun clearAndNavigate(screen: Screen) {
        val decision = childModePolicy.evaluateScreen(screen)
        if (!decision.allowed) {
            backStack.clear()
            backStack.add(decision.fallbackScreen ?: Screen.Setting)
            return
        }

        backStack.clear()
        backStack.add(screen)
    }

    fun popBackStack() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    private fun navigateToChildSafeFallback(fallbackScreen: Screen?) {
        val fallback = fallbackScreen ?: Screen.Setting
        if (backStack.lastOrNull() != fallback) {
            backStack.add(fallback)
        }
    }
}

class NavigateOptionsBuilder {
    internal var popUpToScreen: Screen? = null
    internal var popUpToInclusive: Boolean = false
    var launchSingleTop: Boolean = false

    fun popUpTo(screen: Screen, builder: PopUpToBuilder.() -> Unit = {}) {
        val options = PopUpToBuilder().apply(builder)
        popUpToScreen = screen
        popUpToInclusive = options.inclusive
    }
}

class PopUpToBuilder {
    var inclusive: Boolean = false
}

val LocalNavController = compositionLocalOf<Navigator> {
    error("No Navigator provided")
}
