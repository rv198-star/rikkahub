package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.ServerStack01
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildModePolicy
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.pages.setting.SettingVM
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrainyPalPracticePage(vm: SettingVM = koinViewModel()) {
    val navController = LocalNavController.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val connection = settings.brainyPalChildConnection
    val configured = connection.isConfigured()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("今日练习") },
                navigationIcon = { BackButton() },
                colors = CustomColors.topBarColors,
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            )
        },
        containerColor = CustomColors.topBarColors.containerColor,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CardGroup(
                    title = { Text("BrainyPal") },
                ) {
                    item(
                        leadingContent = { Icon(HugeIcons.Book03, null) },
                        headlineContent = { Text("今日练习") },
                        supportingContent = {
                            Text(if (configured) "从 BrainyPal 打开孩子今日任务" else "先配置 BrainyPal 连接")
                        },
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            if (configured) {
                                navController.navigate(
                                    Screen.WebView(
                                        url = BrainyPalChildModePolicy.practiceWebUrl(connection)
                                    )
                                )
                            } else {
                                navController.navigate(Screen.BrainyPalConnection)
                            }
                        }
                    ) {
                        Icon(if (configured) HugeIcons.Book03 else HugeIcons.ServerStack01, null)
                        Text(
                            text = if (configured) "打开练习" else "配置连接",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
