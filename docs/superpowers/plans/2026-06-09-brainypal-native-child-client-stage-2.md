# BrainyPal Native Child Client Stage 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the RikkaHub child-mode landing experience with a native BrainyPal child workbench and native child-practice list backed by BrainyPal Agent Service APIs.

**Architecture:** RikkaHub owns the Android child UI. BrainyPal owns Agent Service contracts. `/child` is only an early MVP/reference page and must not be the primary Android child-client target. Stage 2 adds pure Kotlin contracts for URL derivation and workbench state, a Retrofit API client for child practice/review endpoints, a child workbench ViewModel, and native Compose screens for the child home and practice list.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation3, Koin, Retrofit, kotlinx.serialization, JUnit, BrainyPal pytest contract tests.

---

### Task 1: Native Child Client Contracts

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/RouteActivity.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/BrainyPalChildModePolicy.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/BrainyPalChildModePolicyTest.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/ui/context/NavigatorTest.kt`

- [ ] **Step 1: Write failing tests for native child home routing and Agent root URL**

Add tests proving:
- child mode allows `Screen.BrainyPalHome`,
- blocked routes fall back to `Screen.BrainyPalHome`,
- child-mode startup can target `Screen.BrainyPalHome`,
- `agentServiceRootUrl()` maps `/rikka/v1`, `/v1`, and `/api` base URLs to the service root.

- [ ] **Step 2: Run tests and verify RED**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.BrainyPalChildModePolicyTest' --tests 'me.rerere.rikkahub.ui.context.NavigatorTest'
```

Expected: fails because `Screen.BrainyPalHome` and `agentServiceRootUrl()` do not exist.

- [ ] **Step 3: Implement minimal routing contract**

Add `Screen.BrainyPalHome`, allow it in `BrainyPalChildModePolicy`, change blocked fallback to `Screen.BrainyPalHome`, and add:

```kotlin
fun agentServiceRootUrl(config: BrainyPalChildConnectionConfig): String {
    val baseUrl = config.baseUrl.trim().removeSuffix("/")
    return baseUrl
        .removeSuffix("/rikka/v1")
        .removeSuffix("/v1")
        .removeSuffix("/api")
        .removeSuffix("/")
}
```

- [ ] **Step 4: Run tests and verify GREEN**

Run the same Gradle test command.

### Task 2: BrainyPal Child API Client Contracts

**Files:**
- Create: `app/src/main/java/me/rerere/rikkahub/brainypal/BrainyPalChildApi.kt`
- Create: `app/src/test/java/me/rerere/rikkahub/brainypal/BrainyPalChildApiTest.kt`

- [ ] **Step 1: Write failing DTO serialization tests**

Tests should decode:
- `{"items":[{"task_id":"task-1","title":"今日练习","task_type":"wrong_question_practice","status":"assigned","item_count":2,"help_limit":3,"help_used":1,"blank_or_low_effort":false}]}`
- `{"should_offer":true,"child_message":"要不要试一小步？","event":{"related_question_id":"wq_due_001","strategy_version_id":"strategy_review_prompt_1","evidence_refs":["wrong_question:wq_due_001"]}}`

Expected mapped values:
- `remainingHelp == 2`,
- `statusLabel == "待开始"`,
- review offer is actionable only when `shouldOffer=true` and `event != null`.

- [ ] **Step 2: Run tests and verify RED**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.BrainyPalChildApiTest'
```

Expected: fails because DTOs/helpers do not exist.

- [ ] **Step 3: Implement API DTOs and Retrofit interface**

Create serializable DTOs and:

```kotlin
interface BrainyPalChildApi {
    @GET("/api/v1/child/practice-tasks")
    suspend fun listPracticeTasks(): BrainyPalChildPracticeTaskListResponse

    @GET("/api/v1/child/review-offer")
    suspend fun getReviewOffer(
        @Query("remaining_minutes") remainingMinutes: Int? = null,
    ): BrainyPalReviewOfferResponse
}
```

Add pure helper properties/functions for labels and actionable review state.

- [ ] **Step 4: Run tests and verify GREEN**

Run the same Gradle test command.

### Task 3: Child Workbench State

**Files:**
- Create: `app/src/main/java/me/rerere/rikkahub/brainypal/BrainyPalChildWorkbench.kt`
- Create: `app/src/test/java/me/rerere/rikkahub/brainypal/BrainyPalChildWorkbenchTest.kt`

- [ ] **Step 1: Write failing workbench mapping tests**

Test configured and unconfigured states:
- unconfigured primary action routes to `Screen.BrainyPalConnection`,
- configured primary practice action routes to native `Screen.BrainyPalPractice`,
- chat action routes to `Screen.Chat("brainypal-child")` or a generated chat screen supplied by caller,
- review card is shown only for actionable offer.

- [ ] **Step 2: Run tests and verify RED**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.BrainyPalChildWorkbenchTest'
```

- [ ] **Step 3: Implement pure workbench state**

Create data classes for child home cards/actions using existing `Screen` routes. Do not fetch network data here.

- [ ] **Step 4: Run tests and verify GREEN**

Run the same Gradle test command.

### Task 4: ViewModel And Native UI

**Files:**
- Create: `app/src/main/java/me/rerere/rikkahub/ui/pages/brainypal/BrainyPalHomePage.kt`
- Create: `app/src/main/java/me/rerere/rikkahub/ui/pages/brainypal/BrainyPalHomeVM.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/pages/brainypal/BrainyPalPracticePage.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/di/ViewModelModule.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/di/DataSourceModule.kt`

- [ ] **Step 1: Implement ViewModel**

`BrainyPalHomeVM` reads settings, builds a Retrofit API from `agentServiceRootUrl()`, loads practice summaries and review offer, exposes loading/error/data state, and keeps connection API key out of visible state.

- [ ] **Step 2: Implement native child home UI**

Use a full-screen child workbench:
- top band with "BrainyPal" and safe connection summary,
- primary action row: "问问 BrainyPal", "今日练习", "复习一下",
- practice summary section,
- review offer section,
- parent/admin connection entry.

- [ ] **Step 3: Rework practice page into native list**

`BrainyPalPracticePage` should show native task summaries and no longer open `/child` by default. In this stage it may open a detail placeholder for task detail; task answering is deferred.

- [ ] **Step 4: Wire DI**

Register `BrainyPalHomeVM` and any needed factories in Koin.

### Task 5: Navigation Integration

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/RouteActivity.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawer.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPage.kt`

- [ ] **Step 1: Start child mode at `Screen.BrainyPalHome`**

When `BuildConfig.BRAINYPAL_CHILD_MODE` is true, app startup uses `Screen.BrainyPalHome`.

- [ ] **Step 2: Add route entry**

Register `entry<Screen.BrainyPalHome> { BrainyPalHomePage() }`.

- [ ] **Step 3: Redirect child drawer/settings BrainyPal entries**

Drawer and settings should route to native home/practice, not the `/child` WebView bridge.

### Task 6: Verification And Packaging

**Files:**
- No source changes expected unless verification finds drift.

- [ ] **Step 1: Run RikkaHub targeted JVM tests**

```bash
./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.*' --tests 'me.rerere.rikkahub.ui.context.NavigatorTest'
```

- [ ] **Step 2: Run BrainyPal Agent Service contract tests**

```bash
cd /root/brainypal-workspace/BrainyPal
uv run pytest tests/test_rikka_adapter_api.py tests/test_child_practice_task_api.py tests/test_child_chat_api.py tests/test_child_review_bridge_api.py -q
```

- [ ] **Step 3: Build debug APK**

```bash
./gradlew :app:assembleDebug
```

- [ ] **Step 4: Record APK hashes**

```bash
sha256sum app/build/outputs/apk/debug/app-universal-debug.apk app/build/outputs/apk/debug/app-arm64-v8a-debug.apk app/build/outputs/apk/debug/app-x86_64-debug.apk
```

- [ ] **Step 5: Commit**

Commit on `codex/brainypal-native-child-client`.
