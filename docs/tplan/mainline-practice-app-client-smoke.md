# BrainyPal Mainline Practice App Client Smoke

## Scope

This checklist records the RikkaHub client-side acceptance path for BrainyPal Agent
Service #98-#103 and RikkaHub #18-#22.

## Agent Contract Coverage

- Child task list/detail decodes the unified Agent Service practice-task contract.
- Child answer, help, accept, and submit requests include `attempt_session_id`.
- Child submitted task result decodes item-level feedback and learning-record payloads.
- Child submitted task result shows review blocks, keeps original answers read-only, and opens a separate immediate correction area.
- Parent practice task creation encodes `mode`, `source_refs`, `activate`, and task items.
- Parent practice task summary decodes draft, active, completed, total counts, and latest tasks.
- Parent entry design is dual-entry: structured import is primary; simple chat is secondary fallback.
- Voice command requests call `/api/v1/voice/commands` with transcript, context, locale, and provider metadata when available.
- Voice command responses map standard Agent intents to local dictation session commands and expose provider failure fallback.

## Automated Evidence

Run from `RikkaHub/`:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:testDebugUnitTest --tests 'me.rerere.rikkahub.brainypal.*'
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:testDebugUnitTest
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:assembleDebug
```

Run from `BrainyPal/` before client integration:

```bash
uv run ruff check src/brainypal tests scripts/smoke_child_web_practice_desk.py
uv run mypy
uv run pytest -q
```

## Local Agent Smoke

1. Start Agent Service on a LAN-reachable host and port.
2. Configure the App BrainyPal service root URL with the same local IP used for packaging.
3. Verify parent creates an active practice task.
4. Verify child task list shows the new active task.
5. Open the task; verify `accept` returns an `attempt_session_id`.
6. Save answers for at least two items; verify a later save does not clear earlier local drafts.
7. Request a hint; verify waiting copy appears before the hint response and the returned hint is visible.
8. Submit; verify incomplete or low-effort attempts are blocked with guidance.
9. Submit a meaningful attempt; verify item-level feedback, review blocks, parent summary, and immediate correction area are visible.
10. Send voice transcripts for repeat, next, dont know, pause, resume, and help; verify safe intent mapping.
11. Send a simulated provider failure; verify the UI does not block and the child can continue with buttons.

## Emulator Smoke

Use Android Studio Device Manager or command line:

```bash
emulator -list-avds
adb devices
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:installDebug
```

Manual UI path:

- Launch App, enter child mode, and confirm child home is not the parent home.
- Open today's task and confirm the top-left navigation returns to the expected previous screen.
- Complete the answer save, hint, submit, result, and voice-control paths above.
- Enter parent PIN, open parent mode, and verify structured import is the primary entry while simple chat remains available.
- Import pasted material, confirm it, dispatch it, and verify task creation/summary surfaces match Agent data.
- Verify OCR/needs_review cards show a source-evidence confirmation path instead of directly blaming the child.

## Evidence Artifacts

For issue comments or docs evidence, store artifacts under:

```text
docs/tplan/evidence/YYYYMMDD-HHMM-<issue-or-flow>/
  emulator.txt
  app-build.txt
  screenshots/
    01-child-home.png
    02-task-detail.png
    03-result-correction.png
    04-parent-workbench.png
  ui-dump/
    01-child-home.xml
    02-parent-workbench.xml
```

Each evidence note should include:

- device or AVD name, Android API level, and APK path;
- Agent Service base URL and whether fake or real provider mode was used;
- exact Gradle command and result;
- paths to screenshots and UI dumps;
- any skipped step with reason, especially real camera, microphone, ASR, or TTS limitations.

If no emulator/device is available, record the missing device condition in final evidence instead of claiming UI smoke.
