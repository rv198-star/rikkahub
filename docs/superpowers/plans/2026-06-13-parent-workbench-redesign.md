# Parent Workbench Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement RikkaHub issues #14, #15, and #16 so the BrainyPal parent side becomes a task-first workbench with import modes and OCR evidence confirmation cards.

**Architecture:** Add a small pure Kotlin parent workbench UI model for entry modes, summary chips, task grouping, and OCR evidence copy. Refactor the existing Compose page to render a task-first workbench, keep connection settings behind a compact status card, and reuse existing BrainyPal Agent APIs.

**Tech Stack:** Kotlin, Jetpack Compose Material3, existing BrainyPal shared API models, JUnit unit tests, Android emulator smoke testing.

---

## Tasks

### Task 1: Parent Workbench UI Model

**Files:**
- Create: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUi.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUiTest.kt`

- [ ] Write failing tests for supply entries, summary chips, task grouping, and OCR evidence copy.
- [ ] Run the new test class and verify unresolved model failures.
- [ ] Implement the minimal pure Kotlin model and helper functions.
- [ ] Re-run the new test class and verify green.

### Task 2: Workbench First Screen And Import Modes

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalConnectionPage.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/parent/BrainyPalParentWorkbenchUiTest.kt`

- [ ] Move configured connection details behind a compact status card.
- [ ] Place the parent workbench card first when configured.
- [ ] Add section buttons for `供给 / 待确认 / 状态 / 策略`.
- [ ] Add supply entry cards for photo, paste, wrong-question review, and AI material search.
- [ ] Keep paste material and quick dictation as available flows; show dependency copy for unavailable photo/search entries.

### Task 3: OCR Evidence Confirmation Cards

**Files:**
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/parent/BrainyPalConnectionPage.kt`
- Reuse: `app/src/main/java/me/rerere/rikkahub/brainypal/shared/BrainyPalDictationOcrReview.kt`

- [ ] Replace text-only OCR rows with evidence cards.
- [ ] Show recognized text, confidence, source region label, guidance, and preview action.
- [ ] Add image/crop preview dialog with bounding-box overlay when available.
- [ ] Keep confirmation actions after evidence and collapse already-confirmed rows.

### Task 4: Verification

**Files:**
- Android test suite and debug APK.

- [ ] Run `./gradlew --no-daemon :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.*'`.
- [ ] Run `./gradlew --no-daemon :app:assembleDebug`.
- [ ] Install debug APK in emulator and capture parent workbench screenshots for #14-#16.
- [ ] Stop emulator and Gradle daemon after verification.
