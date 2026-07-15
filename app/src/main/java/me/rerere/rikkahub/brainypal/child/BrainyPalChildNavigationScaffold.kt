package me.rerere.rikkahub.brainypal.child

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Home03

enum class BrainyPalChildDestination(
    val label: String,
    val icon: ImageVector,
) {
    Home("首页", HugeIcons.Home03),
    Ask("问一问", HugeIcons.BubbleChatQuestion),
    Practice("练习", HugeIcons.Book03),
}

enum class BrainyPalChildNavigationMode {
    BottomBar,
    Rail,
}

object BrainyPalChildNavigationPolicy {
    const val TABLET_BREAKPOINT_DP = 840f

    fun modeForWidth(widthDp: Float): BrainyPalChildNavigationMode {
        return if (widthDp >= TABLET_BREAKPOINT_DP) {
            BrainyPalChildNavigationMode.Rail
        } else {
            BrainyPalChildNavigationMode.BottomBar
        }
    }
}

@Composable
fun BrainyPalChildNavigationScaffold(
    selectedDestination: BrainyPalChildDestination,
    onNavigate: (BrainyPalChildDestination) -> Unit,
    modifier: Modifier = Modifier,
    askEnabled: Boolean = true,
    practiceEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val navigationMode = BrainyPalChildNavigationPolicy.modeForWidth(maxWidth.value)
        if (navigationMode == BrainyPalChildNavigationMode.Rail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    BrainyPalChildDestination.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = destination == selectedDestination,
                            enabled = destination.isEnabled(askEnabled, practiceEnabled),
                            onClick = { onNavigate(destination) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar {
                        BrainyPalChildDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = destination == selectedDestination,
                                enabled = destination.isEnabled(askEnabled, practiceEnabled),
                                onClick = { onNavigate(destination) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    content()
                }
            }
        }
    }
}

private fun BrainyPalChildDestination.isEnabled(
    askEnabled: Boolean,
    practiceEnabled: Boolean,
): Boolean {
    return when (this) {
        BrainyPalChildDestination.Home -> true
        BrainyPalChildDestination.Ask -> askEnabled
        BrainyPalChildDestination.Practice -> practiceEnabled
    }
}
