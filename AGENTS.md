# Repository Guidelines

本文档面向贡献者，概述本仓库的模块结构、开发流程，便于快速上手并保持一致的协作质量。

## Issue-First Workflow

本仓库后续强制采用 issue-first 项目管理方式。除非是纯本地一次性验证、构建缓存清理或用户明确要求的紧急热修，否则任何开发、重构、设计调整、测试补强、打包发布和缺陷修复都必须先有可引用的 GitHub Issue。

- 开工前先确认是否已有对应 issue；没有则先创建 issue，再写代码。
- issue 标题要描述可交付结果，不使用“优化一下”“处理一下”这类不可验收表达。
- issue 正文至少包含：背景、目标、范围、验收标准、测试/验证方式、风险或不做内容。
- 分支、提交、PR、最终汇报都要引用 issue 编号，例如 `#12`。
- 一个 issue 应尽量对应一个可独立验收的变化；如果范围变大，先拆 issue。
- 若开发中发现新问题，不直接顺手扩 scope；先记录到当前 issue 的后续项，或新开 issue。
- 合并或关闭 issue 前，必须写明实际验证证据，例如单测、构建、模拟器/真机验收或后端 API 测试。

## Multi-Agent Trial Workflow

复杂或高风险任务可采用多 agent 协作；小型文档、纯格式、一次性验证、明确的小 bug 可由主线程直接完成，避免流程过重。无论是否启用多 agent，仍必须遵守 issue-first 规则。

推荐角色分工：

- PD / 主协调 agent：负责需求澄清、issue/范围确认、任务拆分、过程跟踪和最终决策。当前推荐使用 `gpt-5.5`。
- 架构师 / 规划审核 agent：负责方案、架构取舍、风险清单、验收矩阵和补丁审查，不直接编写代码、不提交。当前推荐使用 `gpt-5.5` high reasoning。
- 高级开发 / 执行 agent：负责实现、测试、修复，并输出变更摘要、测试证据和未解风险。当前推荐使用 `gpt-5.3-codex-spark` high reasoning。

适合启用的场景：

- 跨模块或跨仓库联动，例如 RikkaHub UI 与 BrainyPal API/schema 成对变化。
- 影响父母端/儿童端主链路的 UI、状态流、导航、权限或数据契约。
- OCR/视觉、ASR/TTS、模型路由、任务下发等高风险能力。
- 大范围重构、测试失败清理、模拟器/真机验收任务。

执行要求：

- PD 先明确任务边界、验收标准、可写文件范围和需要等待的子 agent 输出。
- 架构师只给出方案、风险和审核结论；发现 scope creep、仓库边界错误或测试证据不足时必须指出。
- 高级开发不自行宣布最终通过；完成后必须列出修改文件、实际运行的测试命令与结果、未运行项及原因。
- PD 等待执行结果和审核意见后，再决定继续修复、接受、拆 issue、回滚或关闭 issue。
- 若子 agent 超时、阻塞或意见冲突，主协调 agent 记录原因并做裁决；不得为了赶进度跳过关键审核。
- 执行中发现的新问题不直接扩大当前 scope；记录到当前 issue 的后续项，或新开 issue。

### BrainyPal Workspace Issue Boundary

本地 BrainyPal 工作区由两个独立 Git 仓库组成，issue 体系也分别归属到各自仓库：

- `RikkaHub/`: Android fork / 客户端仓库，负责儿童端 App、父母端 App、Compose UI、导航、设备能力、打包、模拟器/真机验收。
- `BrainyPal/`: 核心 Agent Service 仓库，负责 PRD、Agent 策略、任务编排、API、OCR/ASR/TTS 服务接入、WIKI/记忆、服务端测试和部署。

跨仓库规则：

- 不用 RikkaHub issue 直接管理 Agent Service 实现，也不用 BrainyPal issue 直接管理 Android UI 实现。
- 一个端到端能力若同时涉及服务端和客户端，必须拆成两个 repo 各自的 issue，并在正文互相链接。
- 分支名、提交信息、PR 和最终汇报必须引用本仓库自己的 issue；跨仓库依赖只作为 linked issue / blocked by / requires 说明。
- 开工前先判断变更归属：API/schema/策略/任务下发属于 `BrainyPal`；页面、交互、设备权限、App 打包属于 `RikkaHub`；共享契约变化需要两个 issue 成对推进。
- 验收证据也按仓库分开：`BrainyPal` 用 pytest/API smoke/服务日志；`RikkaHub` 用 JVM 单测、Gradle build、模拟器/真机 UI 验收。

## BrainyPal App Structure

BrainyPal 功能在同一个 Android App / 同一个 repo 内演进，不先拆独立父母端 App。代码必须按角色和共享能力拆分，避免把父母端逻辑混进儿童端页面。

目标分层：

- `shared`: BrainyPal 共享能力，包括 API models、网络客户端、任务/OCR/语音 domain model、共享 UI 组件、权限与配置桥接。
- `child`: 儿童端体验，包括儿童首页、聊天入口、今日任务、听写、背诵、练习、OCR 批改确认等。
- `parent`: 父母端体验，包括连接管理、孩子任务创建、听写/错题/背诵材料管理、OCR 证据确认、学习摘要和策略设置。

落地规则：

- 新增 BrainyPal 页面时，先判断属于 `child`、`parent` 还是 `shared`，再放置文件。
- 共享模型和 API 不依赖具体页面；页面只消费清晰的 state/model。
- 儿童端不得默认暴露父母端管理入口；父母端入口需要权限或家长确认门槛。
- 同一能力若儿童端和父母端都用到，先抽到 `shared`，不要复制两套。
- 大规模移动文件或包名重构必须单独开 issue，并附迁移验收清单。

## Build, Test, and Development Commands

使用 Android Studio 或命令行 Gradle：

```bash
./gradlew assembleDebug          # 构建 Debug APK
./gradlew test                   # 运行所有模块的 JVM 单元测试
./gradlew connectedDebugAndroidTest  # 运行设备/模拟器上的仪器测试
./gradlew lint                   # 运行 Android Lint
```

构建应用需要在 `app/` 下提供 `google-services.json`（用于 Firebase）。
`web` 模块会在 `preBuild` 阶段构建 `web-ui/` 并复制静态资源，需要本地可用 `pnpm`。

## Coding Style & Naming Conventions

本仓库使用 `.editorconfig` 统一格式：

- Kotlin/Gradle 脚本：4 空格缩进，最大行长 120。
- XML/JSON：2 空格缩进。
- Markdown/YAML：2 空格缩进，允许尾随空格（用于对齐）。

命名习惯：模块名为小写目录（如 `ai/`、`speech/`），Kotlin 类遵循 PascalCase，测试类以 `*Test` 结尾。

## Testing Guidelines

测试框架以 JUnit/AndroidX Test 为主。未设定强制覆盖率门槛，但新逻辑应配套新增/更新测试。测试文件命名建议：

- 单元测试：`FooTest.kt`
- 仪器测试：`FooInstrumentedTest.kt` 或 `*Test.kt`

## Module Structure

- **app**: Main application module with UI, ViewModels, and core logic
- **ai**: AI SDK abstraction layer for different providers (OpenAI, Google, Anthropic)
- **common**: Common utilities and extensions
- **document**: Document parsing module for handling PDF, DOCX, PPTX, and EPUB files
- **highlight**: Code syntax highlighting implementation
- **material3**: Material color utility extensions used by the app UI
- **search**: Search functionality SDK for multiple providers (Exa, Tavily, Zhipu, Bing, Brave, SearXNG, and others)
- **speech**: Speech module for TTS and ASR implementations
- **web**: Embedded web server module that provides Ktor server startup function and hosts static frontend build files (
  built from web-ui/ React project)

## Concepts

- **Assistant**: An assistant configuration with system prompts, model parameters, and conversation isolation. Each
  assistant maintains its own settings including temperature, context size, custom headers, tools, memory options, regex
  transformations, and prompt injections (mode/lorebook). Assistants provide isolated chat environments with specific
  behaviors and capabilities. (app/src/main/java/me/rerere/rikkahub/data/model/Assistant.kt)

- **Conversation**: A persistent conversation thread between the user and an assistant. Each conversation maintains a
  list of MessageNodes in a tree structure to support message branching, along with metadata like title, creation time,
  update time, pin status, chat suggestions, optional conversation-level system prompt, and prompt injection bindings. (
  app/src/main/java/me/rerere/rikkahub/data/model/Conversation.kt)

- **UIMessage**: A platform-agnostic message abstraction that encapsulates chat messages with different types of content
  parts (text, images, documents, reasoning, tool calls/results, etc.). Each message has a role (USER, ASSISTANT,
  SYSTEM, TOOL), creation timestamp, model ID, token usage information, and optional annotations. UIMessages support
  streaming updates through chunk merging. (ai/src/main/java/me/rerere/ai/ui/Message.kt)

- **MessageNode**: A container holding one or more UIMessages to implement message branching functionality. Each node
  maintains a list of alternative messages and tracks which message is currently selected (selectIndex). This enables
  users to regenerate responses and switch between different conversation branches, creating a tree-like conversation
  structure. (app/src/main/java/me/rerere/rikkahub/data/model/Conversation.kt)

- **Message Transformer**: A pipeline mechanism for transforming messages before sending to AI providers (
  InputMessageTransformer) or after receiving responses (OutputMessageTransformer). Transformers can modify message
  content, add metadata, apply templates, handle special tags, convert formats, and perform OCR. Common transformers
  include:
  - TemplateTransformer: Apply Pebble templates to user messages with variables like time/date
  - ThinkTagTransformer: Extract `<think>` tags and convert to reasoning parts
  - RegexOutputTransformer: Apply regex replacements to assistant responses
  - DocumentAsPromptTransformer: Convert document attachments to text prompts
  - Base64ImageToLocalFileTransformer: Convert base64 images to local file references
  - OcrTransformer: Perform OCR on images to extract text

  Output transformers support `visualTransform()` for UI display during streaming and `onGenerationFinish()` for final
  processing after generation completes.
  (app/src/main/java/me/rerere/rikkahub/data/ai/transformers/Transformer.kt)

## Internationalization

- String resources are usually located in `app/src/main/res/values*/strings.xml`; feature modules such as `search`
  may also maintain their own `values*/strings.xml`
- Use `stringResource(R.string.key_name)` in Compose
- Page-specific strings should use page prefix (e.g., `setting_page_`)
- If the user does not explicitly request localization, prioritize implementing functionality without considering
  localization. (e.g `Text("Hello world")`)
- For `locale-tui` operations, use the `locale-tui-localization` skill.
