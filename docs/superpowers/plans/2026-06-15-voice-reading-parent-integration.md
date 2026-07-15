# Voice Reading Parent Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the remaining RikkaHub #21 and #24 mainline gaps by routing child voice commands through the Agent Service contract, submitting reading/recitation oral evidence, and showing parent-safe summaries and strategy confirmation UI.

**Architecture:** Keep Agent Service as the intent and evidence source of truth. Add small Kotlin DTO/domain adapters in `shared`, keep task-specific playback and form state in the child page, and add the minimum parent-side API/UI state inside the existing parent workbench page without a broad ViewModel refactor. Oral submissions use the Agent `PracticeAttemptSessionResponse` shape through a dedicated Kotlin response DTO, then the VM refreshes or adapts the child detail state.

**Tech Stack:** Kotlin, Retrofit, kotlinx.serialization, Jetpack Compose Material3, existing RikkaHub ASR/TTS hooks, BrainyPal Agent Service APIs, JUnit unit tests, Android emulator smoke testing.

---

## Scope Decisions

- Do not split `BrainyPalConnectionPage.kt` into a parent ViewModel in this stage. It is worthwhile later, but it would mix a refactor with #24 acceptance.
- Do not make ASR scoring mandatory for reading/recitation. First version records self-rating, reread count, stuck points, optional audio/transcript refs, and whether recitation text was hidden.
- Do not expose raw ASR transcript in parent summaries. Parent result/detail views use `oral_evidence` metadata only.
- Keep buttons as first-class fallback whenever microphone permission, ASR provider, Agent voice command, or network intent parsing fails.
- Use `rv198-star/rikkahub` issue semantics, not upstream `rikkahub/rikkahub` issue numbers.
- Do not declare `/oral-submissions` as returning `BrainyPalChildPracticeTaskDetail`; the backend returns `PracticeAttemptSessionResponse` with `oral_evidence_by_item`.

## Task 1: Generic Voice Action Contract

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalVoiceApi.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalDictationSession.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/shared/BrainyPalVoiceCommandApiTest.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/shared/BrainyPalVoiceControlShellTest.kt`

- [x] Add a failing test that `ask_help` maps to a generic task action while remaining `UNKNOWN` for dictation session mutation.
- [x] Add a failing test that provider failure returns `showButtonFallback=true`, preserves child-facing fallback copy, and does not create a mutating action.
- [x] Add `BrainyPalVoiceAction` enum-like model with `REPEAT`, `NEXT`, `DONT_KNOW`, `PAUSE`, `RESUME`, `ASK_HELP`, `UNKNOWN`.
- [x] Update `BrainyPalVoiceCommandResponse` with `toVoiceAction()` and keep `toDictationCommand()` as an adapter over the generic action.
- [x] Update `BrainyPalVoiceControlState` to carry `action: BrainyPalVoiceAction` while keeping `dictationCommand` for existing callers until task pages migrate.
- [x] Run `:app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.shared.BrainyPalVoice*'`.

## Task 2: Agent-First Voice Interpretation In Child Task Page

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalHomeVM.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalPracticePage.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/di/ViewModelModule.kt` if constructor injection needs explicit wiring
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalHomeStateLoaderTest.kt` or a new focused VM/domain test if easier

- [x] Add a failing test around a fake voice API proving transcript `"给我一个提示"` in `practice` context returns an `ASK_HELP` action instead of a dictation command.
- [x] Inject `BrainyPalVoiceApiFactory` into `BrainyPalHomeVM`.
- [x] Add `interpretVoiceCommand(context, transcript, fallback)` in the VM. It calls `/api/v1/voice/commands` using the configured Agent root URL and falls back to the supplied local action when the call fails.
- [x] In `PracticeTaskDetailContent`, send ASR transcripts to the VM for dictation, reading/recitation, and normal practice contexts.
- [x] Dictation keeps local matcher fallback and applies `toDictationCommand()`.
- [x] Practice maps `ASK_HELP` to `onRequestHelp`, `NEXT` to local focus/message only, `DONT_KNOW` to a non-judgmental message, and unknown/provider failure to button fallback copy.
- [x] Run the focused child/voice tests.

## Task 3: Child Oral Submission API And Reading/Recitation Flow

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalChildApi.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalHomeVM.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalPracticePage.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/shared/BrainyPalChildApiTest.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalChildTaskInteractionTest.kt` if copy/state helpers change

- [x] Add failing serialization tests for `BrainyPalSubmitOralEvidenceRequest` with `attempt_session_id`, `items`, `self_rating`, `reread_count`, `stuck_points`, `audio_ref`, `transcript`, and `text_hidden_during_attempt`.
- [x] Add `@POST("/api/v1/child/practice-tasks/{task_id}/oral-submissions")` to `BrainyPalChildApi`.
- [x] Add `BrainyPalPracticeAttemptSessionResponse` decode support for `task`, `answers`, `evidence_by_item`, `oral_evidence_by_item`, and `result`.
- [x] Add VM method `submitOralEvidence(taskId, request)` with pending/success/error copy.
- [x] Generalize `RecitationFlowCard` into a reading/recitation oral task card or add a reading branch that reuses the same component with labels.
- [x] Track reread count locally when child taps listen/repeat.
- [x] Submit oral evidence from the oral task card instead of using generic `submitPracticeTask`.
- [x] Hide recitation source text during attempt when task type is `recitation`; allow reading text to remain visible.
- [x] Show returned result review after oral submission.
- [x] Run focused shared/child tests.

## Task 4: Parent Result Detail And Learning Summary

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalParentApi.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUi.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalConnectionPage.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/shared/BrainyPalParentApiTest.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUiTest.kt`

- [x] Add failing decode tests for `/parent/practice-tasks/{task_id}/result`, including oral evidence without transcript.
- [x] Add failing decode tests for `/parent/learning-records/summary`, including record type counts and latest record summaries.
- [x] Add Retrofit methods `getPracticeTaskResult(taskId)` and `getLearningRecordsSummary(limit)`.
- [x] Add parent UI model helpers for learning summary chips and parent-safe oral evidence cards.
- [x] In parent status/strategy sections, load learning records summary on refresh and display task completion, help/reread/stuck-point summary, and latest learning record cards.
- [x] Add a result-detail action for completed tasks that loads the parent-safe result detail and shows OCR/oral evidence without raw transcript.
- [x] Run focused parent API/UI tests.

## Task 5: Parent Strategy Candidate Confirmation

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalParentApi.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUi.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalConnectionPage.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/shared/BrainyPalParentApiTest.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUiTest.kt`

- [x] Add failing decode tests for `POST /parent/strategies`, `GET /parent/strategies`, `activate`, and `pause`.
- [x] Add DTOs for strategy request, create response, list response, strategy version, and child plan.
- [x] Add Retrofit methods for create/list/activate/pause strategy.
- [x] In the strategy section, allow parent to type a goal such as “这周朗读多鼓励”, create a draft, review child-facing wording, then explicitly confirm activation.
- [x] Wire chat `strategy_proposal` primary action to prefill the strategy section instead of silently applying anything.
- [x] Show active/paused strategy status and pause action.
- [x] Run focused parent strategy tests.

## Task 6: Integrated Verification, Issue Comments, Commit, Push

**Files:**
- Modify if needed: `docs/tplan/mainline-practice-app-client-smoke.md`
- GitHub issues: `rv198-star/rikkahub#21`, `rv198-star/rikkahub#24`

- [x] Run `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:testDebugUnitTest --no-daemon --tests 'me.rerere.rikkahub.brainypal.*'`.
- [x] Run `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:testDebugUnitTest --no-daemon`.
- [x] Run `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug --no-daemon`.
- [x] Start or reuse Android emulator, install debug APK, and smoke child task loading/detail, parent PIN/workbench, Agent import/send, parent result detail, and parent strategy confirmation.
- [x] If real microphone ASR cannot be completed, record exact device/provider blocker and keep #21 open with evidence.
- [x] Comment issue #21 and #24 with completed work, verification commands, emulator evidence, and remaining gaps.
- [ ] Commit and push the RikkaHub branch.

## Verification Notes

- Focused BrainyPal unit tests, full `:app:testDebugUnitTest`, and `:app:assembleDebug` were run successfully before emulator smoke.
- Emulator `BrainyPal_API37` installed `app-arm64-v8a-debug.apk`; app launched as `me.rerere.rikkahub.debug/me.rerere.rikkahub.RouteActivity`.
- Local Agent Service ran at `http://192.168.5.80:8000`; child task list and review offer returned HTTP 200 from the emulator.
- Parent PIN `123456` unlocked the workbench; parent workbench, structured import entry, connection card, and strategy tab rendered.
- Agent API smoke: parent import session -> pending task -> send -> child list visibility passed; parent result detail and strategy create/activate/list passed.
- Real microphone ASR was not exercised in this emulator pass. Voice command safety is covered by focused unit tests; the emulator verified fallback controls and permission-gated voice UI.
