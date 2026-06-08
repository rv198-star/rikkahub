package me.rerere.rikkahub.brainypal

import me.rerere.rikkahub.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildModePolicyTest {
    @Test
    fun `child mode exposes only safe child entries`() {
        val policy = BrainyPalChildModePolicy.enabled()

        assertTrue(policy.isScreenAllowed(Screen.Chat("chat-id")))
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
