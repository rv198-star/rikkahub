package me.rerere.rikkahub.brainypal

import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.datastore.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildModePolicyTest {
    @Test
    fun `new development installs start configured for current Agent Service`() {
        val settings = Settings()

        assertEquals("http://192.168.5.104:8000/rikka/v1", settings.brainyPalChildConnection.baseUrl)
        assertEquals("brainypal-local", settings.brainyPalChildConnection.apiKey)
        assertTrue(settings.brainyPalChildConnection.isConfigured())

        val managementPin = requireNotNull(settings.brainyPalManagementPin)
        assertTrue(managementPin.verify("123456"))
        assertFalse(managementPin.verify("000000"))
    }

    @Test
    fun `development defaults repair empty persisted child connection and missing pin`() {
        val connection = BrainyPalChildModePolicy.developmentConnectionOverride(
            BrainyPalChildConnectionConfig(baseUrl = "", apiKey = "")
        )
        val managementPin = BrainyPalChildModePolicy.developmentManagementPinOverride(null)

        assertEquals("http://192.168.5.104:8000/rikka/v1", connection.baseUrl)
        assertEquals("brainypal-local", connection.apiKey)
        assertTrue(managementPin.verify("123456"))
    }

    @Test
    fun `development defaults override persisted child connection and pin for packaged environment`() {
        val connection = BrainyPalChildModePolicy.developmentConnectionOverride(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "old-local-key",
            )
        )
        val managementPin = BrainyPalChildModePolicy.developmentManagementPinOverride(
            BrainyPalChildModePolicy.createManagementPin("654321", salt = "old-salt")
        )

        assertEquals("http://192.168.5.104:8000/rikka/v1", connection.baseUrl)
        assertEquals("brainypal-local", connection.apiKey)
        assertTrue(managementPin.verify("123456"))
        assertFalse(managementPin.verify("654321"))
    }

    @Test
    fun `child mode exposes only safe child entries`() {
        val policy = BrainyPalChildModePolicy.enabled()

        assertTrue(policy.isScreenAllowed(Screen.Chat("chat-id")))
        assertTrue(policy.isScreenAllowed(Screen.BrainyPalHome))
        assertTrue(policy.isScreenAllowed(Screen.Setting))
        assertTrue(policy.isScreenAllowed(Screen.BrainyPalPractice))
        assertTrue(policy.isScreenAllowed(Screen.BrainyPalConnection))
        assertTrue(policy.isScreenAllowed(Screen.SettingAbout))
        assertTrue(policy.isScreenAllowed(Screen.SettingPreferences))
        assertTrue(policy.isScreenAllowed(Screen.WebView(url = "http://127.0.0.1:8000/child")))

        assertFalse(policy.isScreenAllowed(Screen.SettingProvider))
        assertFalse(policy.isScreenAllowed(Screen.SettingProviderDetail("provider-id")))
        assertFalse(policy.isScreenAllowed(Screen.SettingModels))
        assertFalse(policy.isScreenAllowed(Screen.Assistant))
        assertFalse(policy.isScreenAllowed(Screen.AssistantPrompt("assistant-id")))
        assertFalse(policy.isScreenAllowed(Screen.SettingMcp))
        assertFalse(policy.isScreenAllowed(Screen.SettingSearch))
        assertFalse(policy.isScreenAllowed(Screen.Backup))
        assertFalse(policy.isScreenAllowed(Screen.Extensions))
        assertFalse(policy.isScreenAllowed(Screen.Developer))
        assertFalse(policy.isScreenAllowed(Screen.Log))
        assertFalse(policy.isScreenAllowed(Screen.History))
        assertFalse(policy.isScreenAllowed(Screen.MessageSearch))
        assertFalse(policy.isScreenAllowed(Screen.Stats))
        assertFalse(policy.isScreenAllowed(Screen.WebView(url = "https://example.com")))
    }

    @Test
    fun `child navigation decision reports blocked fallback reason`() {
        val decision = BrainyPalChildModePolicy.enabled().evaluateScreen(Screen.SettingProvider)

        assertFalse(decision.allowed)
        assertEquals(Screen.BrainyPalHome, decision.fallbackScreen)
        assertEquals(BrainyPalChildNavigationReason.CHILD_MODE_BLOCKED, decision.reason)
    }

    @Test
    fun `child navigation decision allows BrainyPal child webview`() {
        val decision = BrainyPalChildModePolicy.enabled().evaluateScreen(
            Screen.WebView(url = "http://127.0.0.1:8000/child")
        )

        assertTrue(decision.allowed)
        assertEquals(null, decision.fallbackScreen)
        assertEquals(BrainyPalChildNavigationReason.ALLOWED, decision.reason)
    }

    @Test
    fun `child mode fixes BrainyPal provider and model`() {
        val config = BrainyPalChildConnectionConfig(
            baseUrl = "http://192.168.1.20:8000/rikka/v1",
            apiKey = "brainypal-local",
        )

        val provider = BrainyPalChildModePolicy.brainyPalProvider(config)

        assertEquals("BrainyPal", provider.name)
        assertEquals("http://192.168.1.20:8000/rikka/v1", provider.baseUrl)
        assertEquals("brainypal-local", provider.apiKey)
        assertEquals(listOf("brainypal-child"), provider.models.map { it.modelId })
        assertFalse(provider.models.single().abilities.any { it.name.lowercase().contains("tool") })
    }

    @Test
    fun `practice entry maps configured child to native practice page`() {
        val entry = BrainyPalChildModePolicy.practiceEntry(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1/",
                apiKey = "brainypal-local",
            )
        )

        assertTrue(entry.configured)
        assertEquals("今日练习", entry.primaryLabel)
        assertEquals("打开今天的 BrainyPal 任务", entry.supportingText)
        assertEquals(Screen.BrainyPalPractice, entry.targetScreen)
    }

    @Test
    fun `practice web url remains available only as legacy child MVP bridge`() {
        val v1Entry = BrainyPalChildModePolicy.practiceEntry(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/v1",
                apiKey = "brainypal-local",
            )
        )
        val apiEntry = BrainyPalChildModePolicy.practiceEntry(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/api/",
                apiKey = "brainypal-local",
            )
        )

        assertEquals(Screen.BrainyPalPractice, v1Entry.targetScreen)
        assertEquals(Screen.BrainyPalPractice, apiEntry.targetScreen)
        assertEquals(
            "http://192.168.1.20:8000/child",
            BrainyPalChildModePolicy.practiceWebUrl(
                BrainyPalChildConnectionConfig(
                    baseUrl = "http://192.168.1.20:8000/rikka/v1",
                    apiKey = "brainypal-local",
                )
            )
        )
    }

    @Test
    fun `agent service root normalizes adapter and api suffixes`() {
        assertEquals(
            "http://192.168.1.20:8000",
            BrainyPalChildModePolicy.agentServiceRootUrl(
                BrainyPalChildConnectionConfig(
                    baseUrl = "http://192.168.1.20:8000/rikka/v1/",
                    apiKey = "brainypal-local",
                )
            )
        )
        assertEquals(
            "http://192.168.1.20:8000",
            BrainyPalChildModePolicy.agentServiceRootUrl(
                BrainyPalChildConnectionConfig(
                    baseUrl = "http://192.168.1.20:8000/v1",
                    apiKey = "brainypal-local",
                )
            )
        )
        assertEquals(
            "http://192.168.1.20:8000",
            BrainyPalChildModePolicy.agentServiceRootUrl(
                BrainyPalChildConnectionConfig(
                    baseUrl = "http://192.168.1.20:8000/api/",
                    apiKey = "brainypal-local",
                )
            )
        )
    }

    @Test
    fun `practice entry sends unconfigured child to connection page`() {
        val entry = BrainyPalChildModePolicy.practiceEntry(BrainyPalChildConnectionConfig())

        assertFalse(entry.configured)
        assertEquals("配置连接", entry.primaryLabel)
        assertEquals("需要家长先配置 BrainyPal", entry.supportingText)
        assertEquals(Screen.BrainyPalConnection, entry.targetScreen)
    }

    @Test
    fun `connection summary never exposes api key`() {
        val summary = BrainyPalChildModePolicy.connectionSummary(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1/",
                apiKey = "secret-key",
            )
        )

        assertEquals("192.168.1.20:8000/rikka/v1", summary)
        assertFalse(summary.contains("secret-key"))
    }

    @Test
    fun `connection summary reports pending configuration when missing`() {
        val summary = BrainyPalChildModePolicy.connectionSummary(
            BrainyPalChildConnectionConfig(baseUrl = "", apiKey = "secret-key")
        )

        assertEquals("待家长配置", summary)
        assertFalse(summary.contains("secret-key"))
    }

    @Test
    fun `management pin is distinct from api key and uses salted hash`() {
        val pin = BrainyPalChildModePolicy.createManagementPin("123456", salt = "family-salt")

        assertTrue(pin.verify("123456"))
        assertFalse(pin.verify("000000"))
        assertFalse(pin.hash.contains("123456"))
        assertFalse(pin.hash.contains("brainypal-local"))
    }

    @Test
    fun `management pin gate cools down after repeated failures`() {
        var nowMillis = 1_000L
        val pin = BrainyPalChildModePolicy.createManagementPin("123456", salt = "family-salt")
        val gate = BrainyPalPinAttemptGate(
            maxFailures = 2,
            cooldownMillis = 30_000,
            nowMillis = { nowMillis },
        )

        assertFalse(gate.verify(pin, "000000"))
        assertFalse(gate.verify(pin, "111111"))
        assertTrue(gate.isCoolingDown())
        assertFalse(gate.verify(pin, "123456"))

        nowMillis += 30_000

        assertFalse(gate.isCoolingDown())
        assertTrue(gate.verify(pin, "123456"))
    }
}
