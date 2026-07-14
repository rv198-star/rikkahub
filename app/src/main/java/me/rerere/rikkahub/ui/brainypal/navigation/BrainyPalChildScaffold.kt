package me.rerere.rikkahub.ui.brainypal.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Home03
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalSizes
import me.rerere.rikkahub.ui.brainypal.designsystem.components.BrainyPalIcon

enum class BrainyPalChildDestination(
    val label: String,
    val icon: ImageVector,
) {
    Home("首页", HugeIcons.Home03),
    Ask("问一问", HugeIcons.BubbleChatQuestion),
    Practice("练习", HugeIcons.Book03),
}

@Composable
fun BrainyPalChildScaffold(
    title: String,
    selectedDestination: BrainyPalChildDestination,
    onNavigate: (BrainyPalChildDestination) -> Unit,
    modifier: Modifier = Modifier,
    askEnabled: Boolean = true,
    practiceEnabled: Boolean = true,
    actions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= BrainyPalSizes.TabletBreakpoint
        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                NavigationRail {
                    BrainyPalChildDestination.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = destination == selectedDestination,
                            enabled = when (destination) {
                                BrainyPalChildDestination.Ask -> askEnabled
                                BrainyPalChildDestination.Practice -> practiceEnabled
                                BrainyPalChildDestination.Home -> true
                            },
                            onClick = { onNavigate(destination) },
                            icon = {
                                BrainyPalIcon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                    decorative = true,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    TopAppBar(
                        title = { Text(title) },
                        actions = { actions() },
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar {
                            BrainyPalChildDestination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected = destination == selectedDestination,
                                    enabled = when (destination) {
                                        BrainyPalChildDestination.Ask -> askEnabled
                                        BrainyPalChildDestination.Practice -> practiceEnabled
                                        BrainyPalChildDestination.Home -> true
                                    },
                                    onClick = { onNavigate(destination) },
                                    icon = {
                                        BrainyPalIcon(
                                            imageVector = destination.icon,
                                            contentDescription = null,
                                            decorative = true,
                                        )
                                    },
                                    label = { Text(destination.label) },
                                )
                            }
                        }
                    }
                },
                content = content,
            )
        }
    }
}
