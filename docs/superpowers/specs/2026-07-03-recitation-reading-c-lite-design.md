# BrainyPal Child Recitation/Reading C-Lite Design

## Context

The current child oral-task flow in `BrainyPalPracticePage.kt` treats recitation and reading like a process/form card:

- recitation hides the source text from the start
- listening, completion markers, self-rating, evidence, and save are stacked into one card
- the first screen emphasizes workflow instead of the text itself

That creates unnecessary cognitive load for a child who already has low frustration tolerance and tends to freeze when an activity feels like a test.

## Goal

Make recitation and reading feel like a natural article page first, and a lightweight practice flow second.

The child should feel:

- "I am looking at the text"
- "I can try one small thing"
- "Only when I am ready do I enter the actual recitation check"

## Non-Goals

- No new backend APIs
- No new task types
- No full-screen wizard or extra route
- No rich gamification inside the oral-task detail page
- No mandatory recording flow for this change

## Primary UX Decision

Use an article-first layout for oral tasks.

Instead of a single workflow card, the page should move through three lightweight states inside the same detail surface:

1. Reading state
2. Sentence practice state
3. Recitation check state

## State Design

### 1. Reading State

Default state for both reading and recitation.

Behavior:

- the text content is the main visual focus
- the child can read the full material normally
- the bottom action area stays small and stable

Primary actions:

- `听一遍`
- `逐句练`
- `开始背诵` or `开始朗读`

Explicitly avoid:

- process steps list
- large instructional card copy
- self-rating fields
- evidence fields

### 2. Sentence Practice State

Entered only after tapping `逐句练`.

Behavior:

- stay on the same page
- keep the full text visible
- only the current sentence is hidden or masked
- surrounding sentences remain visible as context
- the current sentence is visually highlighted

Primary actions:

- `看这句`
- `下一句`
- `退出逐句练`

Design intent:

- practice should feel like "peek at one sentence, then try it"
- not like a quiz engine

### 3. Recitation Check State

Entered only after tapping `开始背诵` / `开始朗读`.

Behavior:

- collapse the source text into a compact folded strip
- do not show the full text by default
- provide a lightweight escape hatch: `看一眼原文`
- after previewing the text, the child should be able to return to the hidden state easily

Primary actions for recitation:

- `听一遍`
- `我背完了`
- `看一眼原文`

Primary actions for reading:

- `听一遍`
- `我读完了`
- `看一眼原文`

## Completion / Reflection

Reflection appears only after the child marks the oral attempt done.

Keep it lightweight:

- one simple self-rating field
- one short "where did I get stuck" field
- one save/submit action

Do not front-load these fields before the child has attempted the task.

## Copy Principles

- calm and short
- no teacher-like step narration
- no "process training" language
- no repeated reminders that feel like scolding or testing

Examples:

- good: `先看一遍，再试一句`
- good: `卡住也没关系，先记下来`
- avoid: `请先完成步骤 1，再进行步骤 2`

## UI Structure

Within the existing oral-task area:

1. Header with task title + short status hint
2. Article block as the main surface
3. Compact bottom action area
4. Reflection area that appears only after oral attempt completion

The article block should remain the dominant visual element across all three states.

## Engineering Direction

- keep this within the existing child practice page
- replace the current monolithic `RecitationFlowCard` behavior with a smaller oral-task presentation model
- prefer extracting oral-task UI policy/state helpers into focused functions or files that can be unit-tested
- keep the existing submission path (`oralEvidenceRequest` and submit action) intact, changing only when fields become visible and how the child reaches them

## Acceptance Criteria

1. Recitation no longer hides the material immediately on page open.
2. Oral-task detail first renders as a text-first reading surface.
3. `逐句练` uses same-page state switching, not route navigation.
4. Sentence practice keeps context visible while masking only one sentence at a time.
5. `开始背诵` / `开始朗读` enters a hidden-text check state with a compact `看一眼原文` affordance.
6. Self-rating and stuck-note inputs do not appear until after the child marks the oral attempt done.
7. Existing oral submit logic still works once reflection fields are completed.
8. Reading and recitation share the same lightweight structure, with only mode-specific labels differing.
