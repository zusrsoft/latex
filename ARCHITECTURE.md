# 项目架构文档（Architecture）

> Kotlin Multiplatform LaTeX 解析与渲染库 —— 架构说明
> 适用版本：1.5.x ｜ 更新日期：2026-09

## 1. 项目概述

本项目是一个基于 **Kotlin Multiplatform (KMP) + Compose Multiplatform** 的 LaTeX 数学公式解析与渲染库，支持 450+ LaTeX 命令，在 Android、iOS、Desktop (JVM)、Web (Wasm/JS) 上提供一致的渲染效果。

**核心管线：**

```
String → Tokenizer → Parser → AST(LatexNode) → Visitors / Measurer → Compose UI / Export
```

**技术栈：**

| 项 | 版本/说明 |
|---|---|
| Kotlin | 2.3.10 |
| Compose Multiplatform | 1.10.3 |
| Android minSdk | 23+ |
| JVM 工具链 | 21 |
| 目标平台 | androidTarget / iosArm64 / iosSimulatorArm64 / jvm / js(browser) / wasmJs(browser) |
| 发布 | Maven Central，组 `io.github.zusrsoft`，版本取 `gradle.properties` 的 `VERSION` |
| 字体体系 | KaTeX 字体（`KATEX_FONT_VERSION=0.16.11`），含完整字体度量 |

## 2. 模块架构与依赖方向

```
latex-base（零依赖底座：日志、常量、SDK 入口）
   ↑ api
latex-parser（解析核心：tokenizer / parser / AST / 增量解析 / 诊断 / visitors）
   ↑ api                        ← parser 禁止依赖 renderer（单向依赖，明文规则）
latex-renderer（测量与渲染引擎，Compose Multiplatform）
   ↑ implementation             ↑ implementation
latex-preview                latex-benchmark（仅 JVM，kotlinx-benchmark + JMH）
   ↑ implementation
composeApp（多平台 Demo 壳）
   ↑ implementation
androidapp（Android 原生壳）    iosApp（Xcode 壳 → ComposeApp framework）
```

要点：

- `latex-renderer` 通过 `api(projects.latexParser)` 向下游暴露完整解析栈，业务方**单依赖 renderer 即可获得解析+渲染能力**。
- `latex-preview` 与 `latex-benchmark` 定位为本地验证/测试模块，**不参与 SDK 打包**（见 `settings.gradle.kts` 注释）。
- `latex-base` / `latex-parser` / `latex-renderer` 三个模块独立发布到 Maven Central。

## 3. 各模块详解

### 3.1 latex-base —— 最小基础层

仅 4 个文件，`commonMain.dependencies` 为空，真正的零依赖底座。

| 文件 | 内容 |
|---|---|
| `base/LatexSDK.kt` | `object LatexSDK`：SDK 统一入口，`initialize(Config)` 注入日志实现 |
| `base/LatexConstants.kt` | 已弃用的旧增量解析常量（排版常量已迁往 renderer 的 `MathConstants`） |
| `base/log/ILogger.kt` | 日志接口 |
| `base/log/HLog.kt` | 日志门面（静态注入，全仓库通用） |

### 3.2 latex-parser —— 解析核心

#### 入口与顶层类型

- `LatexParser.kt`：`class LatexParser`（`parse()` / `parseWithDiagnostics()`）+ `internal ParseSession`（聚合 EnvironmentParser / ChemicalParser / CommandParser）
- `ParseDiagnostic.kt`：结构化诊断，`Severity` 3 级 + `Category` 8 类（UNKNOWN_COMMAND / MISSING_BRACE / MISMATCHED_ENVIRONMENT / MACRO_ERROR 等），支持按类过滤
- `SymbolMap.kt`：约 **661 条** 命令→Unicode 符号映射
- `SourceMapper.kt`：源位置映射

#### tokenizer/

- `LatexToken.kt`：`sealed class LatexToken`，**16 种 token**，均带 `SourceRange`
- `LatexTokenizer.kt`：分词器实现

#### model/ —— AST

- `LatexNode.kt`（1413 行）：`sealed class LatexNode` + **68 个 data class 节点类型**（Document / Text / Command / Environment / Fraction / Root / Matrix / Delimited / Accent / ExtensibleArrow / BigOperator / Aligned / Cases / Split / Boxed / Enclose / Phantom / SideSet / Tensor / SectionHeading / TextDirection 等），配套约 25 个 enum
- **AST 自描述协议**：每个节点实现 `children() / withSourceRange() / withChildren() / accept(visitor)`；基类提供 `fold() / mapNodes()` 函数式遍历，消除外部重复 when

#### incremental/ —— tree-sitter 风格增量解析

`IncrementalLatexParser.kt`（736 行）三层策略：

1. **增量分词**：token 复用 + 偏移平移（`IncrementalTokenizer.kt`）
2. **AST 子树复用**：prefix / dirty / suffix 划分（`TreeReuser.kt`）
3. **容错解析**：结构边界截断，而非逐字符回退

支持 `append()` 快速路径。

#### visitor/ —— 3 个 visitor + Printer

| Visitor | 用途 |
|---|---|
| `LatexVisitor.kt`（67 个 visit 方法） | 双分派核心接口 + `BaseLatexVisitor` / `SimpleLatexVisitor` 抽象基类 |
| `MathMLVisitor.kt` | AST → Presentation MathML |
| `AccessibilityVisitor.kt` | AST → MathSpeak 风格屏幕阅读器文本 |
| `util/LatexPrinter.kt` | AST → 规范化 LaTeX 文本 |

#### component/ 与 handler/ —— 注册表模式

- `component/`：`LatexTokenStream`、`LatexParserContext`、`CommandParser`（静态共享 `CommandRegistry`，分发优先级：自定义命令 → 注册表 → 化学公式 → 符号/通用回退）、`EnvironmentParser`（align / alignat / array / cases / split / multline / tabular 等）、`ChemicalParser`（`\ce` / `\cf`）
- `component/handler/`：**23 个文件 = 21 个领域 handler + CommandRegistry + ParseUtils**
- `CommandRegistry`：`fun interface CommandHandler` + register / dispatch / installModule；20 个 `installXxxHandlers()` 扩展模块在 CommandParser 中装配
- **注册命令名总计约 271 个**，分布：DelimiterHandlers 131、PackageCommandHandlers 72、StyleHandlers 51（字体/样式别名**集中地**）、AccentHandlers 32、BigOperatorHandlers 30、MacroHandlers 28、SpaceHandlers 22、SpecialEffectHandlers 21、FractionHandlers 10、ReferenceHandlers 9 等

### 3.3 latex-renderer —— 测量与渲染引擎

依赖：`api(projects.latexParser)` + Compose runtime/foundation/material3/ui + coroutines（JVM 侧另加 coroutines-swing）。

#### Compose 入口（commonMain 根）

- `Latex.kt`：`@Composable fun Latex(latex, modifier, config, isDarkTheme)` —— 内部持有 `IncrementalLatexParser`（remember 复用）、`Dispatchers.Default` 异步增量解析、防抖、Accessibility 语义、Canvas 绘制
- `LatexEditorCanvas.kt`、`AnimatedLatex.kt`（公式过渡动画）

#### layout/ —— 测量与绘制

- `LatexRenderer.kt`：**「测量+绘制」单一共享内核**，Composable 屏显与导出两条路径复用同一 `LatexRenderResult`，保证一致
- `LatexMeasurer.kt`：`MeasurerRegistry` 按各测量器声明的 `handledNodeTypes: Set<KClass<LatexNode>>` 自动建查找表（重复注册 require 失败），共 **17 个测量器**
- `layout/measurer/`：`NodeMeasurer` 接口 + 16 个实现（TextContent / Fraction / Root / Script / BigOperator / Binomial / Matrix / Accent / Delimiter / ExtensibleArrow / Stack / BoxedPhantom / Negation / Tag / Substack / Ref+SideSetTensor）
- 辅助：`NodeLayout`（尺寸+基线+draw lambda）、`LayoutMap`（编辑器用节点位置映射）、`LayoutCache`（AST+RenderContext 键缓存）、`LineBreaker`（智能断行）、`HighlightCalculator`、`GroupLayoutPostProcessor`、`EquationNumbering`（label→编号）

#### editor/ —— WYSIWYG 公式编辑器（实验性）

`LatexEditor` / `EditorState` / `CursorCalculator`（804 行，最大文件）/ `SlotNavigator` / `LatexTemplate`。

#### export/ —— 导出

- `LatexExporter.kt`：`ImageFormat`(PNG/JPEG/WEBP)、`SvgTextMode`(PATH/TEXT)、`ExportConfig` 等
- `LatexExporterComposable.kt`、`SvgCanvas.kt`（expect）；jvm/android/js/wasmJs/ios 各有 actual 实现

#### 其他包

- `font/`：`MathFontProvider`、`KaTeXFontMetrics`、`TtfFontSetProvider`（KaTeX 字体体系）
- `model/`：`RenderStyle.kt`（含 `LatexConfig` / `LatexTheme` / `RenderContext`）、`LatexFontFamily`
- `measure/LatexMeasure.kt`：Compose inline content 集成（`InlineTextContent`）
- `utils/`：`MathConstants`（排版常量）、`MathSpacing`、`FontResolver`、`DelimiterRenderer`、`GlyphBoundsProvider`(expect) 等

### 3.4 辅助模块

| 模块 | 定位 | 要点 |
|---|---|---|
| `latex-preview` | 样本驱动的可视验证层（12 文件，依赖仅 renderer） | `BasicLatexPreview.kt`（1346 行，**26 个 PreviewGroup**）为核心数据集；另有 Chemical / Editor / Export / Incremental / InlineMath / LineBreaking 专题预览屏。不参与 SDK 打包 |
| `latex-benchmark` | 性能基准（7 文件，纯 JVM） | `TokenizerBenchmark` / `ParserBenchmark` / `IncrementalParserBenchmark` / `RendererMeasureBenchmark` / `IncrementalMeasureBenchmark` / `CacheBenchmark`；经 `-Xfriend-paths` 访问 renderer internal API |
| `composeApp` | 多平台 Demo 壳（12 文件） | `App.kt` 承载 `LatexPreview()`；jvm / web / ios 各有入口 |
| `androidapp` | Android 原生壳 | `applicationId com.hrm.latex.demo`，依赖 composeApp + latex-base |
| `iosApp` | iOS 原生壳 | Xcode 工程（Swift），链接 ComposeApp framework |

## 4. 关键架构特征

1. **双注册表镜像**：parser 侧 `CommandRegistry`（命令名→handler）与 renderer 侧 `MeasurerRegistry`（KClass→Measurer）采用同一「声明式能力 + 自动分发」模式，两侧扩展均不需修改分发逻辑 —— 与「保持管线对齐」规则呼应。
2. **AST 自描述协议 + 双分派**：`children / withChildren / withSourceRange / accept` 让增量解析的子树重建与所有 visitor 免去巨型 when。
3. **单一渲染内核**：`LatexRenderer` 同时服务 Composable 与 SVG/位图导出，保证屏显与导出一致。
4. **增量管线贯穿**：IncrementalLatexParser（tree-sitter 风格）→ `Latex` Composable 的异步增量解析 → AnimatedLatex / 编辑器场景。
5. **严格的依赖单向性**：base 零依赖；parser 不依赖 renderer；preview/benchmark 定位为非发布模块。

## 5. 新增 LaTeX 特性的改动路径

新命令/新节点通常需要贯穿整条管线：

```
handler（component/handler/ 下对应 install 模块注册）
  → AST 节点（LatexNode.kt + LatexVisitor / MathMLVisitor / AccessibilityVisitor / LatexPrinter）
  → 测量器（renderer layout/measurer/，并接入 MeasurerRegistry）
  → 预览样本（latex-preview 对应 PreviewGroup）
  → 测试（latex-parser / latex-renderer 的 commonTest）
  → 文档（README / PARSER_COVERAGE_ANALYSIS 等）
```

注意事项：

- 字体/样式别名必须集中在 `StyleHandlers.kt`，不得散落。
- 不允许「有 AST 节点但缺 visitor 支持」「有可见特性但无预览覆盖」。
- 共享逻辑保持在 `commonMain`，禁止移入平台源集。

## 6. 测试组织

| 模块 | 数量 | 形式 |
|---|---|---|
| latex-parser | 52 个 commonTest | 单包平铺，按特性命名（MathMLVisitorTest / IncrementalLatexParserTest / ChemicalParserTest / DelimiterTest / SpecialEffectTest 等） |
| latex-renderer | 16 commonTest + 1 jvmTest | 按包分目录：layout / editor / export / font / measure / model / utils；jvmTest 仅 LatexSvgExporterTest |
| composeApp | 1 commonTest | 冒烟 |
| latex-base / latex-preview | 0 | — |

## 7. 关键数字速查

| 指标 | 数值 |
|---|---|
| AST 节点类型 | 68（+约 25 enum） |
| LatexVisitor visit 方法 | 67 |
| Token 类型 | 16 |
| handler 文件 / 注册命令名 | 23 / 约 271 |
| SymbolMap 符号条目 | ~661 |
| MeasurerRegistry 测量器 | 17 |
| Preview 预览分组 | 26 |
| Benchmark 基准类 | 6 |
| 测试文件总数 | 70 |

## 8. 构建与验证命令

| 场景 | 命令 |
|---|---|
| Android 构建 | `./gradlew :composeApp:assembleDebug` |
| Desktop 演示 | `./gradlew :composeApp:run` |
| 预览模块 | `./gradlew :latex-preview:run` |
| Web (Wasm) | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| Web (JS) | `./gradlew :composeApp:jsBrowserDevelopmentRun` |
| 解析器测试 | `./run_parser_tests.sh` |
| 单个测试 | `./gradlew :latex-parser:jvmTest --tests "*SpecialEffectTest"` |
| 渲染器编译 | `./gradlew :latex-renderer:compileKotlinJvm` |
| 全量检查 | `./gradlew check` |

推荐验证顺序：聚焦测试 → 模块编译/测试 → 预览/Demo 运行 → 必要时 `check`。
