# BrainyPal Child Client Stage 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first refined RikkaHub child-mode safety shell for BrainyPal with testable navigation decisions, safe connection summaries, clearer practice entry state, Agent Service contract alignment, and a new debug APK.

**Architecture:** Keep BrainyPal Agent Service as the learning runtime. In this completed safety-shell milestone, `/child` was used only as a temporary WebView bridge because it already existed as the early MVP/PWA. `/child` is not the current Android child-client target. The next child-client phase should replace this bridge with native RikkaHub UI that calls BrainyPal child APIs directly. Add pure Kotlin helpers in `BrainyPalChildModePolicy.kt` so behavior can be verified without GUI tests, then wire those helpers into `BrainyPalPracticePage`, `BrainyPalConnectionPage`, `SettingPage`, and `Navigator`.

**Tech Stack:** Kotlin, Jetpack Compose, RikkaHub existing settings store, JUnit unit tests, Gradle debug APK build.

---

### Task 1: Add Testable Child Client Contracts

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/BrainyPalChildModePolicy.kt`
- Modify: `app/src/test/java/me/rerere/rikkahub/brainypal/BrainyPalChildModePolicyTest.kt`

- [x] **Step 1: Write failing tests for navigation decisions, practice entry state, URL mapping, and safe connection summary**

Add tests that assert:

```kotlin
@Test
fun `child navigation decision reports blocked fallback reason`() {
    val decision = BrainyPalChildModePolicy.enabled().evaluateScreen(Screen.SettingProvider)

    assertFalse(decision.allowed)
    assertEquals(Screen.Setting, decision.fallbackScreen)
    assertEquals(BrainyPalChildNavigationReason.CHILD_MODE_BLOCKED, decision.reason)
}

@Test
fun `practice entry maps adapter base url to child page`() {
    val entry = BrainyPalChildModePolicy.practiceEntry(
        BrainyPalChildConnectionConfig(
            baseUrl = "http://192.168.1.20:8000/rikka/v1/",
            apiKey = "brainypal-local",
        )
    )

    assertTrue(entry.configured)
    assertEquals("打开练习", entry.primaryLabel)
    assertEquals(Screen.WebView(url = "http://192.168.1.20:8000/child"), entry.targetScreen)
}

@Test
fun `practice entry sends unconfigured child to connection page`() {
    val entry = BrainyPalChildModePolicy.practiceEntry(BrainyPalChildConnectionConfig())

    assertFalse(entry.configured)
    assertEquals("配置连接", entry.primaryLabel)
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
```

- [x] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.BrainyPalChildModePolicyTest'
```

Expected: fails because `evaluateScreen`, `practiceEntry`, `connectionSummary`, and related types do not exist.

- [x] **Step 3: Implement minimal pure Kotlin helpers**

Add:

```kotlin
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
```

Implement `evaluateScreen(screen)`, `practiceEntry(config)`, and `connectionSummary(config)`.

- [x] **Step 4: Run test to verify it passes**

Run the same Gradle unit test command.

Expected: `BUILD SUCCESSFUL`.

### Task 2: Wire Helpers Into Child Shell UI

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/context/NavContext.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/pages/brainypal/BrainyPalPracticePage.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/pages/brainypal/BrainyPalConnectionPage.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPage.kt`
- Modify: `app/src/test/java/me/rerere/rikkahub/brainypal/BrainyPalChildModePolicyTest.kt`
- Add: `app/src/test/java/me/rerere/rikkahub/ui/context/NavigatorTest.kt`

- [x] **Step 1: Add/extend failing tests for UI-facing helper labels**

Add tests that assert configured and unconfigured `BrainyPalPracticeEntry.supportingText` and `connectionSummary` copy match the stage 1 design.

- [x] **Step 2: Run test to verify it fails if labels are not wired or are wrong**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.BrainyPalChildModePolicyTest'
```

- [x] **Step 3: Update Compose pages to use helpers**

Use `BrainyPalChildModePolicy.practiceEntry(connection)` in `BrainyPalPracticePage`.
Use `BrainyPalChildModePolicy.connectionSummary(connection)` in `SettingPage`.
Use the navigation decision helper in `Navigator` so blocked routes use the policy fallback.
Keep `BrainyPalConnectionPage` from displaying the API key outside the password field.

- [x] **Step 4: Run unit test to verify it passes**

Run the same Gradle unit test command. Also run `NavigatorTest` to verify blocked
navigation and BrainyPal `/child` WebView routing without GUI automation.

### Task 3: Agent Interface Alignment And Package Build

**Files:**
- No expected source changes unless alignment check finds a contract drift.

**Boundary note:** These checks align the RikkaHub safety shell with BrainyPal Agent
Service. They do not validate a native Android child-client UX. The `/child` PWA is an
early MVP/interface reference, not the product target for the next UI phase.

- [x] **Step 1: Verify BrainyPal Agent Service contract**

Run in BrainyPal:

```bash
uv run pytest tests/test_rikka_adapter_api.py tests/test_child_practice_task_api.py tests/test_child_chat_api.py -q
```

Expected: relevant Agent Service adapter/chat/practice tests pass.

- [x] **Step 2: Verify RikkaHub child client unit tests**

Run in RikkaHub:

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.BrainyPalChildModePolicyTest'
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 3: Build debug APK**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 4: Record APK hashes**

Run:

```bash
sha256sum app/build/outputs/apk/debug/app-universal-debug.apk app/build/outputs/apk/debug/app-arm64-v8a-debug.apk app/build/outputs/apk/debug/app-x86_64-debug.apk
```

Expected: three SHA256 values for manual phone testing.

- [x] **Step 5: Commit implementation**

Commit the RikkaHub implementation on `codex/brainypal-child-client-stage-1`.
