# Parent Achievement Weekly Summary App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement RikkaHub #31 in the parent-side App experience by showing a calm weekly achievement/habit summary, parent wording suggestions, and a strategy confirmation path without exposing the child-facing achievement world or creating a surveillance dashboard.

**Architecture:** Keep Agent Service as the source of truth for weekly achievement summaries. Add Retrofit DTOs in `shared`, pure parent-safe display mapping in `BrainyPalParentWorkbenchUi`, a small Compose card file for the summary/detail UI, and minimal state/fetch wiring in `BrainyPalConnectionPage`.

**Tech Stack:** Kotlin, Retrofit, kotlinx.serialization, Jetpack Compose Material3, existing RikkaHub parent workbench, BrainyPal Agent Service APIs, JUnit unit tests, Android emulator/browser visual smoke when available.

---

## Scope Decisions

- Parent-side wording must describe weekly trends and next parent sentence, not child module mechanics.
- Do not show realtime feeds, timestamps, raw child logs, failed counts, rankings, points, or child achievement labels.
- Do not auto-activate guidance strategy from the weekly card. The card may prefill the strategy tab so the parent confirms.
- Keep child-side App flow unchanged in this task. The child achievement/raising system remains a separate surface.
- Keep the existing parent workbench architecture; avoid introducing a ViewModel refactor during this issue.

## Task 1: Parent Achievement Weekly API Contract

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalParentApi.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/shared/BrainyPalParentApiTest.kt`

- [x] Add a failing decode test for `ParentAchievementWeeklySummaryResponse` with module summaries, suggested wording, strategy notes, and `realtime_event_feed`.
- [x] Add `GET /api/v1/parent/achievement-habits/weekly-summary` to `BrainyPalParentApi`.
- [x] Add serializable DTOs for weekly summary and module summary.
- [x] Explicitly decode `realtime_event_feed` but keep it out of parent UI display models.
- [x] Run the focused parent API test.

## Task 2: Parent-Safe Weekly Summary Display Model

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUi.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUiTest.kt`

- [x] Add failing tests for module-id label mapping: `bravery_core`, `repair`, `communication`, `navigation`, `modeling`, and unknown fallback.
- [x] Add failing tests for forbidden parent copy guard.
- [x] Add failing tests for empty, loaded, wording, strategy candidate, and ignored realtime feed states.
- [x] Add parent-safe display models for weekly summary, trend rows, suggested wording, and strategy candidate text.
- [x] Ensure display output uses at most 3 trend rows and at most 3 suggested wording lines.
- [x] Run the focused parent UI model test.

## Task 3: Compose Weekly Summary Card And Detail

**Files:**
- Add: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWeeklySummaryCard.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalConnectionPage.kt`

- [x] Add weekly summary state, loading, and error handling to the parent workbench.
- [x] Fetch weekly summary on parent refresh and status refresh.
- [x] Place the weekly summary card in the parent `status` section, not above the work-import primary flow.
- [x] Open a detail panel/sheet for suggested wording.
- [x] Wire the strategy action to prefill the strategy tab for parent confirmation.
- [x] Keep copy calm: `最近 7 天 · 温和周总结`, `查看建议话术`, `带入策略页确认`, `这里只看周级趋势，不展开每一步`.

## Task 4: Verification, Issue Update, Commit, Push

**Files:**
- Modify if needed: `docs/superpowers/specs/2026-06-16-parent-achievement-weekly-summary-design.md`
- GitHub issue: `rv198-star/rikkahub#31`

- [x] Run focused tests for parent API and UI model.
- [x] Run broader BrainyPal parent tests if focused tests pass.
- [x] Build or run `:app:assembleDebug` if code compiles need confirmation.
- [x] Use browser/emulator where practical to visually inspect the parent workbench card and detail.
- [x] Comment #31 with implementation evidence and remaining true-device gaps.
- [ ] Commit and push the RikkaHub branch.
