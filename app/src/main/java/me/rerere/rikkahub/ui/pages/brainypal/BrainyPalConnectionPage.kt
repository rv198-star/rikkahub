package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Lock
import me.rerere.hugeicons.stroke.ServerStack01
import me.rerere.rikkahub.brainypal.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.BrainyPalChildModePolicy
import me.rerere.rikkahub.brainypal.BrainyPalManagementPin
import me.rerere.rikkahub.brainypal.BrainyPalPinAttemptGate
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.pages.setting.SettingVM
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrainyPalConnectionPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val savedPin = settings.brainyPalManagementPin
    val gate = remember(savedPin?.hash) { BrainyPalPinAttemptGate() }

    var unlocked by remember(savedPin?.hash) { mutableStateOf(savedPin == null) }
    var pinCandidate by remember(savedPin?.hash) { mutableStateOf("") }
    var newPin by remember(savedPin?.hash) { mutableStateOf("") }
    var baseUrl by remember(settings.brainyPalChildConnection.baseUrl) {
        mutableStateOf(settings.brainyPalChildConnection.baseUrl)
    }
    var apiKey by remember(settings.brainyPalChildConnection.apiKey) {
        mutableStateOf(settings.brainyPalChildConnection.apiKey)
    }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("BrainyPal 连接") },
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
            if (!unlocked && savedPin != null) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(HugeIcons.Lock, null)
                            Text("输入管理 PIN", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = pinCandidate,
                                onValueChange = { pinCandidate = it },
                                label = { Text("管理 PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            Button(
                                onClick = {
                                    when {
                                        gate.isCoolingDown() -> {
                                            message = "PIN 尝试过多，请稍后再试"
                                        }

                                        gate.verify(savedPin, pinCandidate) -> {
                                            unlocked = true
                                            message = null
                                        }

                                        else -> {
                                            message = "PIN 不正确"
                                        }
                                    }
                                }
                            ) {
                                Text("解锁")
                            }
                        }
                    }
                }
            } else {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(HugeIcons.ServerStack01, null)
                            Text("BrainyPal 服务", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = baseUrl,
                                onValueChange = { baseUrl = it },
                                label = { Text("Base URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                label = { Text("API Key") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (savedPin == null) {
                                OutlinedTextField(
                                    value = newPin,
                                    onValueChange = { newPin = it },
                                    label = { Text("设置管理 PIN") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Text(
                                text = "当前连接：${
                                    BrainyPalChildModePolicy.connectionSummary(
                                        BrainyPalChildConnectionConfig(
                                            baseUrl = baseUrl,
                                            apiKey = apiKey,
                                        )
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            message?.let {
                                Text(
                                    text = it,
                                    color = if (it == "已保存") {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                )
                            }
                            Button(
                                onClick = {
                                    val config = BrainyPalChildConnectionConfig(
                                        baseUrl = baseUrl.trim(),
                                        apiKey = apiKey,
                                    )
                                    val pin = savedPin ?: newPin
                                        .takeIf { it.length >= 4 }
                                        ?.let(BrainyPalChildModePolicy::createManagementPin)

                                    if (!config.isConfigured()) {
                                        message = "请填写 Base URL 和 API Key"
                                        return@Button
                                    }
                                    if (pin == null) {
                                        message = "请设置至少 4 位管理 PIN"
                                        return@Button
                                    }

                                    vm.updateSettings(settings.withBrainyPalConnection(config, pin))
                                    message = "已保存"
                                }
                            ) {
                                Text("保存")
                            }
                            if (savedPin != null) {
                                TextButton(
                                    onClick = {
                                        unlocked = false
                                        pinCandidate = ""
                                        message = null
                                    }
                                ) {
                                    Text("锁定")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Settings.withBrainyPalConnection(
    config: BrainyPalChildConnectionConfig,
    pin: BrainyPalManagementPin,
): Settings {
    val provider = BrainyPalChildModePolicy.brainyPalProvider(config)
    val modelId = provider.models.single().id

    return copy(
        brainyPalChildConnection = config,
        brainyPalManagementPin = pin,
        providers = listOf(provider) + providers.filterNot { it.name == provider.name },
        chatModelId = modelId,
        fastModelId = modelId,
        translateModeId = modelId,
        compressModelId = modelId,
        enableWebSearch = false,
        mcpServers = emptyList(),
        modeInjections = emptyList(),
        lorebooks = emptyList(),
        quickMessages = emptyList(),
    )
}
