# Recitation/Reading C-Lite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert child recitation/reading tasks from a workflow-card flow into a lightweight article-first experience with same-page sentence practice and delayed reflection.

**Architecture:** Keep the existing practice-detail route and submit plumbing, but extract oral-task UI policy into a focused helper and move the oral-task composable into its own file. The practice page stays responsible for task loading and submit actions, while the new oral-task layer owns phase switching, sentence masking, and reflection visibility.

**Tech Stack:** Kotlin, Jetpack Compose Material3, existing BrainyPal child theme, JUnit4 unit tests, Gradle Android test task

---

### Task 1: Extract oral-task UI policy and coverage

**Files:**
- Create: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalOralTaskUiModel.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalOralTaskUiModelTest.kt`

- [ ] **Step 1: Write the failing tests for sentence splitting, masking, and reflection gating**

```kotlin
class BrainyPalOralTaskUiModelTest {
    @Test
    fun `recitation opens in reading phase with full text visible`() {
        val model = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.Reading,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )

        assertEquals(BrainyPalOralTaskPhase.Reading, model.phase)
        assertTrue(model.lines.all { it.visibleText.isNotBlank() })
        assertFalse(model.showReflection)
        assertEquals("开始背诵", model.primaryActionLabel)
    }

    @Test
    fun `sentence practice masks only current sentence and keeps context visible`() {
        val model = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.SentencePractice,
            currentSentenceIndex = 1,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )

        assertTrue(model.lines[0].visibleText.isNotBlank())
        assertEquals("______", model.lines[1].visibleText)
        assertTrue(model.lines[1].isActive)
        assertTrue(model.lines[2].visibleText.isNotBlank())
    }

    @Test
    fun `recitation check hides source until peek is enabled`() {
        val hidden = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.Check,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )
        val peek = hidden.copy(showSourcePeek = true)

        assertTrue(hidden.lines.all { it.visibleText.isBlank() })
        assertTrue(peek.lines.any { it.visibleText.isNotBlank() })
    }

    @Test
    fun `reflection stays hidden until oral attempt is marked done`() {
        val hidden = BrainyPalOralTaskUiModel.build(
            detail = readingDetail(),
            phase = BrainyPalOralTaskPhase.Check,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )
        val shown = hidden.copy(showReflection = true)

        assertFalse(hidden.showReflection)
        assertTrue(shown.showReflection)
    }
}
```

- [ ] **Step 2: Run the new unit test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalOralTaskUiModelTest`

Expected: FAIL because `BrainyPalOralTaskUiModel` and `BrainyPalOralTaskPhase` do not exist yet.

- [ ] **Step 3: Implement the minimal oral-task UI model**

```kotlin
enum class BrainyPalOralTaskPhase {
    Reading,
    SentencePractice,
    Check,
}

data class BrainyPalOralTaskLine(
    val rawText: String,
    val visibleText: String,
    val isActive: Boolean,
)

data class BrainyPalOralTaskUiModel(
    val phase: BrainyPalOralTaskPhase,
    val title: String,
    val hint: String,
    val lines: List<BrainyPalOralTaskLine>,
    val primaryActionLabel: String,
    val completeActionLabel: String,
    val showSourcePeek: Boolean,
    val showReflection: Boolean,
) {
    companion object {
        fun build(
            detail: BrainyPalChildPracticeTaskDetail,
            phase: BrainyPalOralTaskPhase,
            currentSentenceIndex: Int,
            revealCurrentSentence: Boolean,
            showSourcePeek: Boolean,
            reflectionUnlocked: Boolean,
        ): BrainyPalOralTaskUiModel {
            val sentences = splitSentences(detail.items)
            val lines = sentences.mapIndexed { index, sentence ->
                val visibleText = when (phase) {
                    BrainyPalOralTaskPhase.Reading -> sentence
                    BrainyPalOralTaskPhase.SentencePractice ->
                        if (index == currentSentenceIndex && !revealCurrentSentence) "______" else sentence
                    BrainyPalOralTaskPhase.Check ->
                        if (showSourcePeek) sentence else ""
                }
                BrainyPalOralTaskLine(
                    rawText = sentence,
                    visibleText = visibleText,
                    isActive = phase == BrainyPalOralTaskPhase.SentencePractice && index == currentSentenceIndex,
                )
            }
            return BrainyPalOralTaskUiModel(
                phase = phase,
                title = detail.title,
                hint = defaultHint(detail.taskType, phase, reflectionUnlocked),
                lines = lines,
                primaryActionLabel = if (detail.taskType == "recitation") "开始背诵" else "开始朗读",
                completeActionLabel = if (detail.taskType == "recitation") "我背完了" else "我读完了",
                showSourcePeek = showSourcePeek,
                showReflection = reflectionUnlocked,
            )
        }
    }
}
```

- [ ] **Step 4: Run the unit test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalOralTaskUiModelTest`

Expected: PASS with 4 tests passing.

- [ ] **Step 5: Commit the isolated policy/model work**

```bash
git add app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalOralTaskUiModel.kt \
        app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalOralTaskUiModelTest.kt
git commit -m "feat: add BrainyPal oral task ui model"
```

### Task 2: Replace the workflow card with article-first oral-task UI

**Files:**
- Create: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalOralTaskCard.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalPracticePage.kt`
- Modify: `app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalChildTaskInteraction.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalChildTaskInteractionTest.kt`
- Test: `app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalPracticeDetailCopyTest.kt`

- [ ] **Step 1: Update the oral-task copy tests to reflect the lighter flow**

```kotlin
@Test
fun `recitation plan keeps the article first brief`() {
    val plan = BrainyPalChildTaskInteraction.plan(recitationDetail())

    assertEquals("开始背诵", plan.primaryActionLabel)
    assertTrue(plan.brief.contains("先看一遍"))
    assertFalse(plan.brief.contains("填一个 1-5 分自评"))
}
```

- [ ] **Step 2: Run the affected copy tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalChildTaskInteractionTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDetailCopyTest`

Expected: FAIL because the old recitation copy still describes the workflow-card flow.

- [ ] **Step 3: Implement the new oral-task composable and wire it into the practice page**

```kotlin
@Composable
fun BrainyPalOralTaskCard(
    detail: BrainyPalChildPracticeTaskDetail,
    interactionPlan: BrainyPalChildTaskInteractionPlan,
    draft: BrainyPalPracticeDraft?,
    actionInProgress: Boolean,
    message: String,
    onListen: () -> Unit,
    onEnterSentencePractice: () -> Unit,
    onRevealSentence: () -> Unit,
    onNextSentence: () -> Unit,
    onExitSentencePractice: () -> Unit,
    onEnterCheck: () -> Unit,
    onToggleSourcePeek: () -> Unit,
    onMarkDone: () -> Unit,
    onUpdateDraft: (String, String) -> Unit,
    onSave: (String, String) -> Unit,
)
```

Implementation notes:

- Move the oral-task UI out of `BrainyPalPracticePage.kt` into `BrainyPalOralTaskCard.kt`.
- Replace `RecitationFlowCard(...)` with `BrainyPalOralTaskCard(...)`.
- Use remembered local state in the practice page for:
  - `oralPhase`
  - `currentSentenceIndex`
  - `revealCurrentSentence`
  - `showSourcePeek`
  - `reflectionUnlocked`
- Keep `oralRereadCount`, TTS calls, `onSaveAnswer`, and `oralEvidenceRequest(...)` intact.
- When `onMarkDone` fires:
  - unlock reflection fields
  - prefill self-rating with `4` only if blank
  - leave evidence text editable
- Show the answer/evidence fields only when `reflectionUnlocked == true`.

- [ ] **Step 4: Run targeted tests and fix integration issues**

Run: `./gradlew :app:testDebugUnitTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalChildTaskInteractionTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDetailCopyTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalOralEvidenceSubmissionTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalOralTaskUiModelTest`

Expected: PASS with all targeted oral-task tests green.

- [ ] **Step 5: Commit the UI refactor**

```bash
git add app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalOralTaskCard.kt \
        app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalPracticePage.kt \
        app/src/main/java/me/rerere/rikkahub/brainypal/child/BrainyPalChildTaskInteraction.kt \
        app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalChildTaskInteractionTest.kt \
        app/src/test/java/me/rerere/rikkahub/brainypal/child/BrainyPalPracticeDetailCopyTest.kt
git commit -m "feat: simplify BrainyPal oral task practice flow"
```

### Task 3: Validate the oral-task slice end-to-end

**Files:**
- Modify: `docs/superpowers/specs/2026-07-03-recitation-reading-c-lite-design.md`
- Modify: `docs/superpowers/plans/2026-07-03-recitation-reading-c-lite.md`

- [ ] **Step 1: Run the broader child-practice regression slice**

Run: `./gradlew :app:testDebugUnitTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalPracticeTaskCopyTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDetailCopyTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalChildTaskInteractionTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalOralEvidenceSubmissionTest --tests me.rerere.rikkahub.brainypal.child.BrainyPalOralTaskUiModelTest`

Expected: PASS with no oral-task regressions.

- [ ] **Step 2: Build the debug app to catch Compose/compiler issues**

Run: `./gradlew :app:assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Update the plan/spec checkboxes or notes if implementation diverged**

```markdown
- Verified targeted oral-task regression suite on 2026-07-03
- Verified `:app:assembleDebug` after oral-task refactor
```

- [ ] **Step 4: Commit the verification/documentation updates**

```bash
git add docs/superpowers/specs/2026-07-03-recitation-reading-c-lite-design.md \
        docs/superpowers/plans/2026-07-03-recitation-reading-c-lite.md
git commit -m "docs: record oral task c-lite verification"
```
