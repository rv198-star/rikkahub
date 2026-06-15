# Parent Achievement Weekly Summary Design

## Goal

Implement RikkaHub issue #31 as a parent-side, low-anxiety weekly summary for
BrainyPal's achievement and habit system.

The feature should help parents notice effort patterns and choose better wording.
It must not become a monitoring dashboard, score ledger, or child-facing
"Bravery Station" module view.

## Product Decision

Child-side 勇气号 language is intentionally not shown verbatim on the parent side.

Child side may use:

- 勇气号空间站
- 技能天梯
- module repair, signal, orbit, station maintenance language

Parent side should translate those signals into plain parenting language:

- 开始意愿
- 遇难反应
- 订正收尾
- 口头表达 or 练习收尾 when oral tasks are involved

Parent side must not show these child-world terms:

- 勇气核心
- 修复模块
- 沟通模块
- 技能天梯
- 点亮模块
- 等级, 积分, 金币, 排行, 连续打卡, 惩罚 streak

The parent should see "what to notice and what to say next", not the internal
game-like structure that gives the child emotional value.

## Selected Approach

Use a parent workbench card plus a detail panel.

The workbench card is a compact weekly trend card:

- title: `最近 7 天 · 温和周总结`
- headline: `3 次值得看见的努力`
- subtitle: `愿意开始 · 提示后又试 · 完成一次订正`
- trend rows: `开始意愿`, `遇难反应`, `订正收尾`
- privacy line: `这里只看趋势，不展示实时记录`
- primary action: `查看建议话术`

The detail panel focuses on wording:

- `先肯定：今天愿意开始就很好`
- `再缩小：下一步只画一条线`
- `最后确认：需要我帮你把第一步拆小吗？`
- strategy note: `连续畏难时：先降低任务颗粒度，不急着追问原因`
- primary action: `保存为引导策略候选`
- secondary action: `稍后再看`

This keeps the parent experience operational and calm while still making the
achievement system useful for family behavior.

## Alternatives Considered

1. **Expose child module summaries directly**
   - Faster to implement because the API already returns module IDs and labels.
   - Rejected because `勇气核心` and `修复模块` are child-side world-building terms.
     Showing them to parents makes the system feel like a game dashboard and
     invites over-interpretation.

2. **Only show suggested wording, no trend summary**
   - Very safe from a surveillance perspective.
   - Rejected because parents need light context to trust why a wording suggestion
     appears.

3. **Selected: translated trend plus suggested wording**
   - Gives parents enough context without raw event logs.
   - Preserves the child's world as the child's world.
   - Creates a clean path from weekly observation to parent-confirmed strategy.

## Data Flow

Agent Service already exposes:

`GET /api/v1/parent/achievement-habits/weekly-summary`

The Android client should add a Retrofit method and DTOs for:

- `period_label`
- `visible_acknowledgements`
- `module_summaries`
- `parent_suggested_wording`
- `strategy_notes`
- `realtime_event_feed`

The Android UI model must translate service categories before rendering.
Even if the API returns module IDs or module labels, the parent UI should render
parent-readable categories only.

Required translation layer:

- `bravery_core` -> `开始意愿`
- `repair` -> `订正收尾`
- `communication` -> `口头表达`
- `navigation` -> `遇难反应`
- `modeling` -> `解题过程`
- unknown -> `学习过程`

This translation belongs in a pure Kotlin UI model helper so it can be unit
tested without Compose.

## UI Placement

Parent workbench should show the weekly summary only after the connection is
configured and the PIN gate is unlocked.

Recommended placement:

1. Parent workbench overview card remains first.
2. Connection status card remains compact.
3. Weekly summary card appears near the status/strategy area, not above homework
   import, because homework import is still the most frequent parent workflow.
4. The strategy tab or detail panel can show full wording suggestions and the
   `保存为引导策略候选` action.

The card should not interrupt homework import.

## States

### Empty State

When `visible_acknowledgements == 0` and no summaries exist:

- title: `最近 7 天 · 温和周总结`
- headline: `还没有形成稳定趋势`
- body: `先完成几次任务后，我会只汇总值得被看见的努力。`
- no strategy button

### Loaded State

Show:

- total visible efforts
- up to three translated trend rows
- up to three suggested wording cards
- at most one strategy note

### Loading State

Use existing parent workbench busy treatment. The card can show a small
`正在整理最近 7 天` line.

### Error State

Do not alarm the parent. Show:

`暂时没有读到周总结，可以稍后刷新。`

No red warning dashboard.

## Copy Rules

Parent copy must:

- say `趋势`, `建议`, `可以这样说`, `候选策略`
- avoid `失败`, `放弃`, `又不会`, `拖拉`, `偷懒`, `实时`, `监控`
- avoid direct child quotes unless the child explicitly shared them through a
  parent-visible task result
- keep strategy wording as a recommendation that requires parent confirmation

The UI should explicitly state:

`这里只看趋势，不展示实时记录`

## Privacy And Pressure Boundaries

Do not render `realtime_event_feed`, even if the API returns data in the future.

Do not show:

- raw event timestamps
- per-attempt failure count
- child chat transcript
- exact "don't know" count
- internal achievement module labels

The parent-facing summary is intentionally delayed and softened.

## Testing

Add or update Android tests for:

- DTO decoding of `ParentAchievementWeeklySummaryResponse`.
- UI model translation from module IDs to parent-readable categories.
- Guards that parent cards do not contain child-world terms such as `勇气核心`,
  `修复模块`, `沟通模块`, `技能天梯`.
- Empty state copy.
- Suggested wording rendering.
- Strategy note action copy.
- `realtime_event_feed` ignored by UI model.

Run:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.*'

JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:assembleDebug
```

Before closing #31, run emulator smoke unless the Android environment is
unavailable. If the emulator is unavailable, record the exact blocker in the
issue and keep the issue open.

Emulator smoke should cover:

- unlock parent workbench with PIN `123456`
- open parent workbench
- refresh weekly summary
- view wording suggestions
- save or prefill a strategy candidate

## Acceptance Criteria

- Parent workbench can fetch and render the weekly achievement summary.
- Parent copy contains translated categories, not child-side module labels.
- Parent sees suggested wording before any strategy action.
- Saving a strategy remains a parent-confirmed candidate flow.
- No real-time feed, score ledger, coins, rankings, streak punishments, or raw
  child transcript appears in this surface.
- Unit tests cover the UI model translation and forbidden-copy guard.
- Build and BrainyPal focused tests pass.
