package me.rerere.rikkahub.brainypal

import kotlinx.serialization.Serializable
import me.rerere.ai.provider.Model
import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.Screen
import java.security.MessageDigest
import kotlin.math.max
import kotlin.text.Charsets.UTF_8
import kotlin.uuid.Uuid

@Serializable
data class BrainyPalChildConnectionConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
) {
    fun isConfigured(): Boolean = baseUrl.isNotBlank() && apiKey.isNotBlank()
}

@Serializable
data class BrainyPalManagementPin(
    val salt: String,
    val hash: String,
) {
    fun verify(pin: String): Boolean = hash == BrainyPalChildModePolicy.hashPin(pin, salt)
}

enum class BrainyPalChildNavigationReason {
    ALLOWED,
    CHILD_MODE_BLOCKED,
}

data class BrainyPalChildNavigationDecision(
    val allowed: Boolean,
    val fallbackScreen: Screen?,
    val reason: BrainyPalChildNavigationReason,
)

data class BrainyPalPracticeEntry(
    val configured: Boolean,
    val primaryLabel: String,
    val supportingText: String,
    val targetScreen: Screen,
)

class BrainyPalPinAttemptGate(
    maxFailures: Int = 5,
    private val cooldownMillis: Long = 60_000,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val maxFailures = max(1, maxFailures)
    private var failedAttempts = 0
    private var cooldownUntilMillis = 0L

    fun verify(pin: BrainyPalManagementPin, candidate: String): Boolean {
        if (isCoolingDown()) {
            return false
        }

        if (pin.verify(candidate)) {
            failedAttempts = 0
            cooldownUntilMillis = 0
            return true
        }

        failedAttempts += 1
        if (failedAttempts >= maxFailures) {
            failedAttempts = 0
            cooldownUntilMillis = nowMillis() + cooldownMillis
        }
        return false
    }

    fun isCoolingDown(): Boolean = nowMillis() < cooldownUntilMillis
}

class BrainyPalChildModePolicy private constructor(
    val active: Boolean,
) {
    fun isScreenAllowed(screen: Screen): Boolean {
        return evaluateScreen(screen).allowed
    }

    fun evaluateScreen(screen: Screen): BrainyPalChildNavigationDecision {
        if (!active) {
            return BrainyPalChildNavigationDecision(
                allowed = true,
                fallbackScreen = null,
                reason = BrainyPalChildNavigationReason.ALLOWED,
            )
        }

        val allowed = when (screen) {
            is Screen.Chat,
            Screen.BrainyPalHome,
            Screen.Setting,
            Screen.BrainyPalPractice,
            Screen.BrainyPalConnection,
            Screen.SettingAbout,
            Screen.SettingTheme,
            Screen.SettingPreferences,
            Screen.SettingPreferencesTheme,
            Screen.SettingPreferencesNotification,
            Screen.SettingPreferencesGeneral,
            Screen.SettingPreferencesUI -> true

            is Screen.WebView -> screen.content.isBlank() && isBrainyPalPracticeUrl(screen.url)

            else -> false
        }
        if (allowed) {
            return BrainyPalChildNavigationDecision(
                allowed = true,
                fallbackScreen = null,
                reason = BrainyPalChildNavigationReason.ALLOWED,
            )
        }
        return BrainyPalChildNavigationDecision(
            allowed = false,
            fallbackScreen = Screen.BrainyPalHome,
            reason = BrainyPalChildNavigationReason.CHILD_MODE_BLOCKED,
        )
    }

    companion object {
        private val BRAINYPAL_PROVIDER_ID = Uuid.parse("19bb07a7-bb11-4fb3-9bf7-7f9f3278e109")
        val BRAINYPAL_CHILD_MODEL_ID: Uuid = Uuid.parse("179e5543-df10-43d4-9a68-a1a609f8e7ea")
        const val BRAINYPAL_CHILD_MODEL_NAME = "brainypal-child"
        const val DEVELOPMENT_DEFAULT_BASE_URL = "http://192.168.5.104:8000/rikka/v1"
        const val DEVELOPMENT_DEFAULT_API_KEY = "brainypal-local"
        private const val DEVELOPMENT_DEFAULT_MANAGEMENT_PIN = "123456"
        private const val DEVELOPMENT_DEFAULT_MANAGEMENT_PIN_SALT = "brainypal-dev-management-pin"

        fun enabled(): BrainyPalChildModePolicy = BrainyPalChildModePolicy(active = true)

        fun disabled(): BrainyPalChildModePolicy = BrainyPalChildModePolicy(active = false)

        fun developmentDefaultConnection(): BrainyPalChildConnectionConfig {
            return BrainyPalChildConnectionConfig(
                baseUrl = DEVELOPMENT_DEFAULT_BASE_URL,
                apiKey = DEVELOPMENT_DEFAULT_API_KEY,
            )
        }

        fun developmentDefaultManagementPin(): BrainyPalManagementPin {
            return createManagementPin(
                DEVELOPMENT_DEFAULT_MANAGEMENT_PIN,
                salt = DEVELOPMENT_DEFAULT_MANAGEMENT_PIN_SALT,
            )
        }

        @Suppress("UNUSED_PARAMETER")
        fun developmentConnectionOverride(
            persistedConfig: BrainyPalChildConnectionConfig?,
        ): BrainyPalChildConnectionConfig {
            return developmentDefaultConnection()
        }

        @Suppress("UNUSED_PARAMETER")
        fun developmentManagementPinOverride(persistedPin: BrainyPalManagementPin?): BrainyPalManagementPin {
            return developmentDefaultManagementPin()
        }

        fun brainyPalProvider(config: BrainyPalChildConnectionConfig): ProviderSetting.OpenAI {
            return ProviderSetting.OpenAI(
                id = BRAINYPAL_PROVIDER_ID,
                enabled = true,
                name = "BrainyPal",
                baseUrl = config.baseUrl.trim().removeSuffix("/"),
                apiKey = config.apiKey,
                models = listOf(
                    Model(
                        id = BRAINYPAL_CHILD_MODEL_ID,
                        modelId = BRAINYPAL_CHILD_MODEL_NAME,
                        displayName = "BrainyPal Child",
                    )
                )
            )
        }

        fun createManagementPin(pin: String, salt: String = Uuid.random().toString()): BrainyPalManagementPin {
            return BrainyPalManagementPin(
                salt = salt,
                hash = hashPin(pin, salt),
            )
        }

        fun practiceWebUrl(config: BrainyPalChildConnectionConfig): String {
            return "${agentServiceRootUrl(config)}/child"
        }

        fun agentServiceRootUrl(config: BrainyPalChildConnectionConfig): String {
            val baseUrl = config.baseUrl.trim().removeSuffix("/")
            return baseUrl
                .removeSuffix("/rikka/v1")
                .removeSuffix("/v1")
                .removeSuffix("/api")
                .removeSuffix("/")
        }

        fun practiceEntry(config: BrainyPalChildConnectionConfig): BrainyPalPracticeEntry {
            if (!config.isConfigured()) {
                return BrainyPalPracticeEntry(
                    configured = false,
                    primaryLabel = "配置连接",
                    supportingText = "需要家长先配置 BrainyPal",
                    targetScreen = Screen.BrainyPalConnection,
                )
            }
            return BrainyPalPracticeEntry(
                configured = true,
                primaryLabel = "今日练习",
                supportingText = "打开今天的 BrainyPal 任务",
                targetScreen = Screen.BrainyPalPractice,
            )
        }

        fun connectionSummary(config: BrainyPalChildConnectionConfig): String {
            if (!config.isConfigured()) {
                return "待家长配置"
            }
            return config.baseUrl
                .trim()
                .removePrefix("https://")
                .removePrefix("http://")
                .removeSuffix("/")
        }

        internal fun hashPin(pin: String, salt: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("$salt:$pin".toByteArray(UTF_8))
            return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
        }

        private fun isBrainyPalPracticeUrl(url: String): Boolean {
            return url.isNotBlank() && Regex("""^https?://[^?#]+/child(?:[/?#].*)?$""").matches(url)
        }
    }
}
