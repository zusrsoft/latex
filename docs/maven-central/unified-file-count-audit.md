# Maven Central File Count 联合审计

> 审计日期：2026-09-08 · 只读审计，未修改任何项目文件
> 范围：zusrsoft/latex + zusrsoft/codehigh + zusrsoft/diagram（+ 补充发现并审计 zusrsoft/Markdown）
> Namespace：`io.github.zusrsoft`

## 1. Executive Summary

- Usage Center 显示 File Count ≈ **4,720**。本审计以 repo1.maven.org 实测目录清单 + 本地 Gradle
  staging 上传包复现，**精确**（非估算）反推出：
  **4,720 = 472 个 primary 文件 × 10 个文件/primary**。
- 10 倍放大系数 = primary 本体(1) + `.asc` 签名(1) + 本体×4 校验和(md5/sha1/sha256/sha512)
  + **`.asc`×4 校验和**。其中 `.asc` 的 4 个 checksum（共 1,888 个文件，占 40%）是
  Sonatype 官方文档明确"通常不需要"的冗余文件，由 **Gradle maven-publish 在 staging/上传阶段生成**。
- 4,720 来自 2026-09-06 同一天的 4 个 Release：
  latex 1.5.4（14:04）、diagram 1.0.4（14:41-49）、codehighlight 1.1.2（14:48-55）、Markdown 1.5.2（15:02-09）。
- 三个项目（+Markdown）结构同构：6 个 KMP target（android/jvm/js/wasmJs/iosArm64/iosSimulatorArm64）
  → 每模块 7 个 publication；**preview/benchmark/demo 模块均未参与发布（0 个 publish task，已用 Gradle 验证）**。
- 最大的可压缩项（不动任何平台/功能）：`.asc` checksum（−1,888，−40%），
  但 Gradle 侧**没有公开的官方开关**，需要进一步验证可行机制后才可实施。
- 即使优化 40%，单次 latex release（1,180）仍然 > 月度上限 1,000，
  且 4 个兄弟项目共享 namespace → **必须申请 OSS publishing limit adjustment**（Sonatype 政策明确支持）。

## 2. Current Central Usage

```
Release Size  ≈ 69.66 MB / 80 MB
File Count    ≈ 4,720 / 1,000
Release Count = 4 / 7
```

Sonatype 官方口径（来源：《Reducing Publishing Usage》，central.sonatype.org/publish/reducing-publishing-usage/）：

- "Every published file contributes to your organization's **monthly file count**."
- "the value evaluated each month is a **rolling three-month average** of your organization's usage,
  not the raw count from a single month alone."
- 本审计实测验证的计数口径：**每个 primary 文件按 10 个文件计**（含 .asc 及其全部 checksum），
  不含 maven-metadata.xml 及其 checksum（repo1 实测 5,140 个文件 = 4,720 + 84×5 metadata 文件）。

## 3. Projects

四个项目为同一作者家族、同一 namespace、AGP 9.1.1 工具链（审计时均为 Gradle 9.3.1；latex 2026-09-08 升至 9.7.1），且存在依赖链：
`latex ← Markdown`（Markdown 依赖 `io.github.zusrsoft:latex:1.5.4` 与 `codehighlight:1.1.2`）。

| Project | 路径 | SDK 模块 | Central 版本 | 本地版本 | 发布时间(repo1) |
| --- | --- | --- | --- | --- | --- |
| latex | `D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\latex` | latex-base / latex-parser / latex-renderer | 1.5.4 | 1.5.4 | 2026-09-06 14:04 |
| diagram | `...\diagram` | diagram-core / -layout / -parser / -render | 1.0.4 | 1.0.4 | 2026-09-06 14:41 |
| codehigh | `...\codehigh` | codehighlight-parser / -render | 1.1.2 | 2.0.0（未发布） | 2026-09-06 14:48 |
| Markdown | `...\Markdown` | markdown-parser / -renderer / -runtime | 1.5.2 | 1.5.2 | 2026-09-06 15:02 |

> 任务书列出的三个项目均已审计；**Markdown 是任务书未列出但实际贡献 1,180 个文件（25%）的第四个项目**，
> 一并纳入审计（见 markdown-audit.md）。

### 3.0 Git 状态（审计时）

| Project | Branch | HEAD | Remote | Working Tree |
| --- | --- | --- | --- | --- |
| latex | master | `ee421eb` build: migrate maven namespace... | github.com/zusrsoft/latex.git | clean |
| codehigh | master | `4c8b8e9` docs: README 增加已知限制章节 | github.com/zusrsoft/codehigh.git | clean |
| diagram | master | `7d248cf` chore: 发布坐标与文档链接迁移... | github.com/zusrsoft/diagram.git | 1 个未跟踪文件（MAVEN_CENTRAL_PUBLISH.md，用户笔记） |
| Markdown | master | `9e581e3` docs: 以 MAVEN_CENTRAL_PUBLISH.md 为发布指南 | github.com/zusrsoft/Markdown.git | clean |

## 4. Gradle / Kotlin / Publishing Versions

| Project | Gradle | AGP | Kotlin | Compose MP | Vanniktech Maven Publish | 其他发布相关 |
| --- | --- | --- | --- | --- | --- | --- |
| latex | 9.7.1 | 9.1.1 | 2.3.10 | 1.10.3 | **0.35.0**（唯一旧版） | Gradle Signing（in-memory GPG，CI 注入） |
| codehigh | 9.3.1 | 9.1.1 | 2.3.20 | 1.10.3 | **0.36.0** | 同上 |
| diagram | 9.3.1 | 9.1.1 | 2.3.20 | 1.10.3 | **0.36.0** | 同上 + `-Psigning.skip` 逃生开关 |
| Markdown | 9.3.1 | 9.1.1 | 2.4.0 | 1.11.1 | **0.36.0** | 同上 + `RELEASE_TO_CENTRAL` 环境变量门控 |

> 注：1.5.4/1.0.4/1.1.2/1.5.2 发布时的文件结构与当前本地代码实测一致；latex 的 0.35.0 与其余
> 三项目的 0.36.0 在"每 primary 10 文件"行为上无差异（staging 实验基于 0.35.0，repo1 远程取证
> 同时覆盖了 0.35.0 与 0.36.0 的产物，结构相同）。

## 5. KMP Targets

四个项目**完全同构**，每个 SDK 模块声明的 6 个 target（grep 全量确认，无 macos/linux/mingw/iosX64/wasmWasi）：

```kotlin
android { ... }   // com.android.kotlin.multiplatform.library（AGP KMP）
jvm { }           // diagram 用块形式，其余 jvm()
js { browser() }  // codehigh/diagram 另有 nodejs()
wasmJs { browser() }
iosArm64(); iosSimulatorArm64()
```

**声明 ≠ 发布**——以下为"是否产生 Maven publication"的实证：

| Project | Target | Declared | Published | Publication 名（Gradle task 实测） | 证据 |
| --- | --- | --- | --- | --- | --- |
| 4 项目 ×12 模块 | android | Yes | **Yes** | `android` | `signAndroidPublication` / `publishAndroidPublicationToMavenCentralRepository`；repo1 有 `-android` 目录(AAR) |
| 同上 | jvm | Yes | **Yes** | `jvm` | 同上；repo1 有 `-jvm` 目录(JAR) |
| 同上 | js | Yes | **Yes** | `js` | 同上；repo1 有 `-js` 目录(klib) |
| 同上 | wasmJs | Yes | **Yes** | `wasmJs` | 同上；repo1 有 `-wasm-js` 目录(klib) |
| 同上 | iosArm64 | Yes | **Yes** | `iosArm64` | 同上；repo1 有 `-iosarm64` 目录(klib+metadata.jar) |
| 同上 | iosSimulatorArm64 | Yes | **Yes** | `iosSimulatorArm64` | 同上；repo1 有 `-iossimulatorarm64` 目录 |
| 同上 | （root） | — | **Yes** | `kotlinMultiplatform` | `signKotlinMultiplatformPublication`；repo1 根目录(.module/.pom/空 jar) |

## 6. Maven Publications

每模块 **7 个 publication**（6 target + 1 root metadata）：
`kotlinMultiplatform`、`android`、`jvm`、`js`、`wasmJs`、`iosArm64`、`iosSimulatorArm64`。

Gradle 实测证据（`gradlew tasks --all`，每模块 19 个 publish task、7 个 sign task）：

```
publish<Name>PublicationToMavenCentralRepository   × 7
publish<Name>PublicationToMavenLocal               × 7
sign<Name>Publication                              × 7
generatePomFileFor<Name>Publication                × 7
generateMetadataFileFor<Name>Publication           × 7
checkPomFileFor<Name>Publication                   × 7
```

Vanniktech 自动附加的 per-target 附件 task（publishToMavenLocal 实际执行清单）：

```
<target>SourcesJar            → -sources.jar
<target>EmptyJavadocJar       → -javadoc.jar（261 字节空桩，Central 要求 javadoc）
buildKotlinToolingMetadata    → -kotlin-tooling-metadata.json（仅 root）
<target>Klib                  → .klib（js/wasm/ios 主产物）
<target>MetadataElements      → -metadata.jar（仅 iOS target）
<target>ZipMultiplatformResourcesForPublication → -kotlin_resources.kotlin_resources.zip
                                              （仅含 Compose resources 的模块的 js/wasm/ios target）
```

### 各项目 mavenPublishing 配置差异

| Project | DSL 配置 | CI 发布命令 | 特殊点 |
| --- | --- | --- | --- |
| latex | `publishToMavenCentral(true)` + `signAllPublications()`（build 文件显式） | `publishAllPublicationsToMavenCentralRepository` | **一个 Release 触发两个 Job：正常版 + `-kt2.1.0` 别名版（依赖降级重编译）→ 一旦别名 Job 成功，File Count 直接 ×2**（1.5.4 本次别名未出现在 Central，仅正常版发布） |
| codehigh | **无显式 DSL**（仅 `coordinates`），Central/签名由凭据属性自动激活 | `publishToMavenCentral` | vanniktech 0.36.0 凭据存在即启用 |
| diagram | `publishToMavenCentral(true)` + `if (!hasProperty("signing.skip")) signAllPublications()` | `publishToMavenCentral` | 有 signing.skip 开关 |
| Markdown | `if (env RELEASE_TO_CENTRAL present) { publishToMavenCentral(true); signAllPublications() }` | `publishAllPublicationsToMavenCentralRepository` | 环境变量门控，本地默认不启用 |

## 7. Publication Matrix

见 [publication-matrix.md](publication-matrix.md)（12 模块 × 7 publication 全量表）。

## 8. File Type Statistics

### 8.1 每个 primary 文件在 Central 上的 10 倍放大（repo1 实测 + staging 复现）

```
latex-base-1.5.4.jar            ← primary（Gradle: jvmJar/androidSourcesJar/... + generatePomFile...）
latex-base-1.5.4.jar.asc        ← Gradle Signing（signJvmPublication 等，由 signAllPublications() 注册）
latex-base-1.5.4.jar.md5        ← Gradle maven-publish 上传时生成
latex-base-1.5.4.jar.sha1       ← Gradle maven-publish 上传时生成
latex-base-1.5.4.jar.sha256     ← Gradle maven-publish 上传时生成
latex-base-1.5.4.jar.sha512     ← Gradle maven-publish 上传时生成
latex-base-1.5.4.jar.asc.md5    ← Gradle maven-publish 上传时生成（Sonatype：通常不需要）
latex-base-1.5.4.jar.asc.sha1   ← 同上
latex-base-1.5.4.jar.asc.sha256 ← 同上
latex-base-1.5.4.jar.asc.sha512 ← 同上
```

另有每个 artifactId 的 `maven-metadata.xml`（repo1 时间戳晚于版本文件 ~5 分钟，由 Central 在
release 处理时重写）+ 4 个 checksum。**Usage Center 不把 metadata 计入 File Count**（见 §16 反推）。

### 8.2 全 namespace 文件类型统计（4 个 Release 合计，Central 口径 4,720）

| 类型 | 数量 | 占比 | 说明 |
| --- | --- | --- | --- |
| primary 本体 | 472 | 10.0% | 构成见 §8.3 |
| `.asc` 签名 | 472 | 10.0% | Central 必需 |
| primary checksum（md5+sha1+sha256+sha512） | 1,888 | 40.0% | md5/sha1 为行业惯例，sha256/512 为 Gradle 自动附加 |
| `.asc` checksum（.asc.md5/.asc.sha1/.asc.sha256/.asc.sha512） | 1,888 | 40.0% | **Sonatype 明确"通常不需要"** |
| **合计（Usage Center 口径）** | **4,720** | 100% | |
| （maven-metadata.xml × 84 + 其 checksum × 336，不计入） | 420 | — | repo1 实际存 5,140 文件 |

### 8.3 472 个 primary 的构成

| primary 类型 | 数量 | 来源 task | 可否裁剪 |
| --- | --- | --- | --- |
| 主产物（jar/aar/klib + root jar） | 84（12 模块×7 pub） | `jvmJar`/`packageDebugAar`…/`<t>Klib`/root `jar` | 否（核心产物） |
| `-sources.jar` | 84 | `<t>SourcesJar`/`sourcesJar` | 否（Central 要求） |
| `-javadoc.jar` | 84 | `<t>EmptyJavadocJar`（**全部为 261B 空桩**） | 否（Central 要求） |
| `.pom` | 84 | `generatePomFileFor<P>Publication` | 否 |
| `.module` | 84 | `generateMetadataFileFor<P>Publication` | 否（KMP variant 解析必需，见 §10） |
| `-metadata.jar`（iOS×2 target × 12 模块） | 24 | `<t>MetadataElements` | 不建议（层级源集元数据） |
| `-kotlin-tooling-metadata.json`（root） | 12 | `buildKotlinToolingMetadata` | 不建议（Kotlin 工具链识别） |
| `-kotlin_resources.kotlin_resources.zip`（4 个 renderer 模块 × js/wasm/iosArm64/iosSim） | 16 | `<t>ZipMultiplatformResourcesForPublication` | 否（Compose resources 运行时必需） |

## 9. Sources / Javadoc

- **sources**：每个 publication（含 root）都有 `-sources.jar`（vanniktech 自动，KMP 各 target 的
  expect/actual 源集）。实测大小合理（latex-renderer sources 188KB、diagram-render 279KB）。
- **javadoc**：所有 84 个 `-javadoc.jar` 均为 **261 字节空壳**（`<t>EmptyJavadocJar` task），
  是为满足 Central "必须提供 javadoc" 要求的标准做法（KMP 非 JVM target 无法生成真 javadoc）。
- 结论：**均为 Central 要求项，不能删除**；它们不是膨胀来源（数量固定 = publication 数）。

## 10. `.module` Metadata

- 哪些 publication 产生：**全部 7×12=84 个**（root + 每 target）。
- 是否 KMP 正常需要：**是**。`.module`（Gradle Module Metadata）承载 KMP variant/availability
  属性，是 Android/JVM/iOS/JS/Wasm 消费方解析 `latex-*` 依赖树的依据。
- 关闭后果：KMP 消费方 variant resolution 直接破坏（Android/iOS/Compose 全部受影响），POM 降级
  解析会丢失平台归因 → **绝对不能删除**。
- 预计减少文件：不建议评估（0）。

## 11. GPG / ASC

生成链（全部实证）：

```
vanniktech mavenPublishing { signAllPublications() }     ← 配置层（latex/diagram/Markdown 显式；codehigh 凭据自动）
        ↓ 注册
Gradle Signing plugin: sign<Publication>Publication task ← task 层（每模块 7 个）
        ↓ 产出
<file>.asc                                               ← 文件层（每 primary 1 个，共 472 个）
```

`.asc` 是 Central 硬性要求（每个文件必须签名）→ **不能删除**。

## 12. Checksum

- `md5`/`sha1`：Maven 生态惯例要求。
- `sha256`/`sha512`：Gradle maven-publish 对 Maven 仓库自动附加（非 Central 要求，但被广泛接受）。
- 生成者：**不是独立 Gradle task**（`gradlew tasks --all` 中无任何 checksum task），由
  `publish*PublicationTo*` task 在写入 staging 仓库时生成 —— staging 目录复现实验证实（见 §13）。

## 13. ASC Checksum Investigation（最高优先级项）

**问题**：`xxx.jar.asc.md5/.asc.sha1/.asc.sha256/.asc.sha512` 共 4×472=1,888 个文件（占 File Count 40%）由谁生成？

**实证**（本地安全复现，未上传任何内容）：

1. 以临时 GPG 密钥 + 假 Central 凭据执行
   `:latex-base:publishAllPublicationsToMavenCentralRepository`；
2. 构建在上传步骤以 `Upload failed: {"error":{"message":"Invalid token"}}` 失败；
3. 项目根目录出现 staging 目录 `./mavenCentral/io/github/zusrsoft/latex-base/...`，
   其中**每个 primary 已带齐 10 个文件（含 4 个 .asc checksum）与 maven-metadata.xml+4 checksum**；
4. 结论链：

```
publishAllPublicationsToMavenCentralRepository (Gradle)
        ↓ 写入本地 staging 仓库（./mavenCentral/）
Gradle maven-publish 生成：primary + .asc + 每文件×4 checksum（含 .asc 的 4 个） + maven-metadata.xml×5
        ↓ vanniktech central-portal 插件打包上传（本实验在此步 401 终止）
Central Portal 校验入库 → repo1 上每个 primary 恰好 10 个文件 ✓（与远程取证一致）
```

**判断**：是 **Gradle（maven-publish + signing 组合）** 层导致，不是 Vanniktech 独有行为、不是
Central 生成。Sonatype 官方文档（Reducing Publishing Usage → Gradle 小节）原文确认：
"Certain Gradle publishing configurations generate checksum files for signature (`.asc`) artifacts.
These additional checksum files are **generally not required** by Maven Central and can
**substantially increase file counts**"——但该文档**未给出 Gradle 侧的具体关闭开关**，
Gradle 官方 maven-publish/signing 文档亦无公开参数。因此"删除 .asc checksum（−1,888）"列为
**需进一步验证机制的候选优化**，本审计不贸然断言配置方法（详见 §18-20）。

## 14. Preview / Benchmark / Demo

Gradle 实测（`tasks --all` 解析，见 evidence/latex-publish-sign-tasks-filtered.txt）：

| Project | Module | mavenPublish 插件 | publish task 数 | sign task 数 | 实际发布 |
| --- | --- | --- | --- | --- | --- |
| latex | latex-preview | 无 | **0** | **0** | 否 ✓ |
| latex | latex-benchmark | 无 | **0** | **0** | 否 ✓ |
| latex | composeApp / androidapp | 无 | 0 | 0 | 否 ✓ |
| codehigh | codehighlight-preview / androidApp / composeApp | 无 | 0 | 0 | 否 ✓（repo1 无对应 artifactId） |
| diagram | diagram-bench / androidApp / composeApp | 无 | 0 | 0 | 否 ✓ |
| Markdown | markdown-preview / markdown-benchmark / macrobenchmark / composeApp / androidapp | 无 | 0 | 0 | 否 ✓ |

repo1 的 84 个 artifactId 与 12 个 SDK 模块 × 7 完全一一对应，**无任何多余 artifact 发布**。
结论：不存在"错误发布的 preview/benchmark/demo"。

## 15. Per-Release File Count

模块分型（Type B = 含 Compose resources 的 renderer 模块，在 js/wasm/iosArm64/iosSim 各多 1 个
`-kotlin_resources` primary；Type A = 其余）：

- Type A：38 primary → **380 Central 文件**（+35 个不计入的 metadata）
- Type B：42 primary → **420 Central 文件**（+35 同上）

| Project | 模块分型 | Primary | **Files/Release（Central 口径）** | Size/Release |
| --- | --- | --- | --- | --- |
| latex | A(base) + A(parser) + B(renderer) | 118 | **1,180** | **15.04 MB**（.m2 实测） |
| diagram | A×3 + B(render) | 156 | **1,560** | ≈ 21±4 MB（残差估计） |
| codehigh | A(parser) + B(render) | 80 | **800** | ≈ 17±3 MB（parser 1.61MB 实测 + render 按已见 klib 推算） |
| Markdown | A(parser) + B(renderer) + A(runtime) | 118 | **1,180** | **16.10 MB**（.m2 实测） |
| **合计（=当月 4 Release）** | 12 模块 | **472** | **4,720** | **69.66 MB**（Usage Center） |

## 16. Central 4,720 File Count Reconstruction（精确）

```
4,720 = 472 primary × 10 文件/primary
      = (8 × Type A 38 + 4 × Type B 42) × (1 + 1 + 4 + 4)
      = (304 + 168) × 10
      = 472 × 10                                    ← 与 Usage Center 完全一致，差 0
```

| 项目 | primary | ×10 | 占比 |
| --- | --- | --- | --- |
| latex | 118 | 1,180 | 25.0% |
| Markdown | 118 | 1,180 | 25.0% |
| diagram | 156 | 1,560 | 33.1% |
| codehigh | 80 | 800 | 16.9% |
| **合计** | **472** | **4,720** | 100% |

交叉验证：
- repo1 实际存储 = 4,720（版本目录）+ 84×5（maven-metadata.xml 及 checksum）= **5,140 文件**；
- Usage Center 4,720 恰好等于"primary×10"，证明其口径 = 发布的版本文件（不含 Central 侧 metadata）；
- Release Count 4 = 4 个项目各一次 `publishToMavenCentral` 系列命令（每命令 1 个 deployment）。

**已解释：4,720 / 4,720（100%，含口径验证）。未解释：0。**

注意（口径区分，任务书 §23）：这是**当月发布活动**统计。repo1 上该 namespace 当前总文件数为
5,140；"下次 latex 单独 release"将再 +1,180（若别名 Job `1.5.4-kt2.1.0` 生效则 +2,360）。

## 17. File Count Waterfall

```
Central File Count ≈ 4720
        │
        ├── latex        1180 (25.0%)
        │     ├── latex-base        380
        │     ├── latex-parser      380
        │     └── latex-renderer    420
        │
        ├── Markdown     1180 (25.0%)
        │     ├── markdown-parser    380
        │     ├── markdown-renderer  420
        │     └── markdown-runtime   380
        │
        ├── diagram      1560 (33.1%)
        │     ├── diagram-core      380
        │     ├── diagram-layout    380
        │     ├── diagram-parser    380
        │     └── diagram-render    420
        │
        └── codehigh      800 (16.9%)
              ├── codehighlight-parser  380
              └── codehighlight-render  420

latex-renderer (420) 拆解：
        ├── KMP publications        7 个（kotlinMultiplatform/android/jvm/js/wasmJs/iosArm64/iosSim）
        ├── primary 文件            42
        │     ├── 主产物             7（jar, aar, jar, klib×3…root jar）
        │     ├── sources            7
        │     ├── javadoc(空桩)      7
        │     ├── .module            7
        │     ├── .pom               7
        │     ├── metadata.jar       2（iosArm64/iosSim）
        │     ├── tooling-metadata   1（root）
        │     └── kotlin_resources   4（js/wasm/ios×2）
        ├── 签名 (.asc)             42
        ├── primary checksum       168（42×4）
        └── .asc checksum          168（42×4）  ← 冗余候选
```

## 18. Optimization A（保守）

约束：不减 target、不删 sources/javadoc/.module、不破坏 Central 要求。

| 项 | 收益 | 状态 |
| --- | --- | --- |
| 删除 preview/benchmark/demo 发布 | 0（本就不发布，已验证） | 已是最优 |
| 停用 latex 别名发布 Job（`-kt2.1.0`） | **避免未来 ×2**（每次 latex release 少 +1,180） | 配置层可做，不影响任何消费者 |
| 每月合并小版本、控制 Release Count | 降低三月滚动平均 | 流程措施 |

计算：当前 4,720 → **4,720（本月不变）**；若别名 Job 曾生效则会是 5,900+，
即保守方案实际"避免了 +1,180"。
减少比例：本月 0%；对未来月份最高避免 +100% 增量（别名）。

## 19. Optimization B（推荐）

在 A 基础上：

1. **验证并启用 .asc checksum 抑制**（Sonatype 明示其非必需）：若找到受支持机制
   （候选方向：vanniktech staging 仓库自定义 / 发布后 Central 校验兼容性回归测试），
   收益 −1,888（−40%）→ 单次 latex release 1,180 → 708。
2. 评估 sha256/sha512 是否可同时抑制（−944，−20%）：风险更低（md5/sha1 保留仍满足惯例）。
3. 三个 SDK 模块 target 保持 6 平台不变；`.module`/sources/javadoc/metadata.jar/
   kotlin_resources 全部保留 → **KMP/Android/iOS/Compose 消费体验零变化**。
4. 统一 4 项目到 vanniktech 最新版（0.36.0+）以跟进官方对 Central 配额的适配。

计算（若 1+2 均落地）：4,720 → 4,720 − (1,888+944) = **1,888（−60%）**；
仅落地 1：**2,832（−40%）**。

风险说明：.asc checksum 抑制机制的可行性**未经本审计验证**，必须先在 staging 复现环境
（本审计已提供方法）验证 Central 校验通过后再启用。

## 20. Optimization C（激进）

| 项 | File Count 收益 | 损失 |
| --- | --- | --- |
| 合并 latex 三模块为单 artifact（base 仅 4.5KB，边界极薄） | latex 每次 release 1,180 → 420（−64%） | **坐标破坏性变更**（io.github.zusrsoft:latex 取代三件套），所有下游需迁移；仅适合大版本切换 |
| 去掉 iosSimulatorArm64 发布 | −60/release（6 primary×10） | iOS 模拟器开发/调试不可用（Compose iOS 开发者重度依赖）→ 不推荐 |
| 去掉 js 或 wasmJs | −50/模块 | Web/Wasm 消费者失去支持 → 不推荐 |
| 删除 sources/javadoc/.module | 不允许 | 违反 Central 要求 / 破坏 KMP 解析 |

激进方案合计：最高 −64%（仅 latex，且为破坏性坐标变更）。
**不推荐为数字好看而采用**；若未来做 2.0 大版本可一并考虑模块合并。

## 21. Risk Matrix

| 优化项 | File Count 收益 | Size 收益 | KMP 影响 | Android 影响 | iOS 影响 | 推荐 |
| --- | --- | --- | --- | --- | --- | --- |
| 停用 latex 别名发布 Job | 高（避免未来×2） | 高 | 无 | 无 | 无 | ★★★★★ |
| 删除 .asc checksum | 高（−1,888，−40%） | 低 | 无 | 无 | 无 | ★★★★（需先验证机制） |
| 删除 sha256/512 checksum | 中（−944，−20%） | 低 | 无 | 无 | 无 | ★★★☆（同上） |
| 合并 SDK 模块 | 高（latex −64%） | 中 | 坐标变更 | 坐标变更 | 坐标变更 | ★★（仅限大版本） |
| 删除 -metadata.jar | 低（−240） | 低 | 潜在层级解析退化 | 无 | 潜在 | ★ |
| 删除 kotlin-tooling-metadata.json | 低（−120） | 低 | 工具链识别退化 | 同 | 同 | ★ |
| 删除 sources | 中 | 中 | 违反 Central 要求 | 同 | 同 | ✗ |
| 删除 javadoc（空桩） | 中 | 无 | 违反 Central 要求 | 同 | 同 | ✗ |
| 删除 .module | 高 | 低 | **破坏 variant 解析** | **高** | **高** | ✗ |
| 减少 KMP target | 高 | 高 | 平台覆盖损失 | — | 高（若删 iOS 系） | ★★ |

## 22. OSS Limit Adjustment Assessment

- 月度 File Count 上限 1,000，按**三个月滚动平均**评估；单次 latex release = 1,180。
- 即使推荐方案全部落地（−60%），4 项目合计仍为 1,888 > 1,000；若当月仅 latex 一个 release
  则 708 < 1,000 勉强达标——但 4 个活跃兄弟项目共享 namespace，不现实。
- 本 namespace 文件构成全部为标准 KMP 多平台发布产物 + Gradle 自动签名/校验，无任何
  违规或冗余发布（preview/benchmark 均未发布）→ 属于 Sonatype 政策中"合法社区开源项目的
  正常发布模式"。
- **结论：情况 B —— 优化之外，必须申请 OSS publishing limit adjustment**
  （central-support@sonatype.com，附本审计与 Usage Center 截图）。

## 23. Final Recommendation（Q8 八问 + §34 十五问合并）

1. **4,720 的第一大来源**：checksum 放大——primary checksum（1,888，40%）与 .asc checksum
   （1,888，40%）合计 80%；其中 .asc checksum 为官方明示的非必需项。
2. **第二大来源**：KMP 多平台 publication 本体（472 primary：主产物+sources+javadoc+pom+module
   等结构文件，10%…按文件数计 primary 仅 10%，但它是触发 10 倍放大的基数）。
3. **第三大来源**：多项目共享 namespace——diagram 4 模块（1,560）> latex/Markdown（各 1,180）
   > codehigh（800）。
4. **latex 占**：1,180（25.0%）。 5. **codehigh 占**：800（16.9%）。 6. **diagram 占**：1,560（33.1%）。
   （Markdown 1,180，25.0%；other：0）
7. **每项目一次 release 的文件数**：latex 1,180 / diagram 1,560 / codehigh 800 / Markdown 1,180。
8. **Maven Central 必需**：primary、.pom、.module（KMP 必需）、-sources.jar、-javadoc.jar、.asc、
   md5/sha1。
9. **可安全减少**：`.asc` 的 4 类 checksum（−1,888）、可选评估 sha256/512（−944）——机制待验证；
   未来增量的别名双发（避免 ×2）。
10. **不应删除**：.module、sources、javadoc、metadata.jar、kotlin_resources、tooling-metadata、
    6 个平台 target。
11. **是否存在 ASC checksum 膨胀**：**是**（1,888 个，占 40%，Gradle 生成，Sonatype 明示非必需）。
12. **是否存在多余 KMP publication**：否（6 target 全部有真实产物且互相不可替代）。
13. **是否存在错误发布的 preview/benchmark/demo**：否（Gradle task 数=0，repo1 无对应 artifact）。
14. **推荐方案可降到**：保守 4,720（本月）/ 避免 +1,180（别名）；推荐 2,832（−40%）~1,888（−60%）；
    激进（合并模块）latex 单项目 420。
15. **是否需要申请 OSS limit adjustment**：**需要**（情况 B），与优化并行（选项 D 的"申请+优化"
    组合，但先申请、后择机优化）。

## 24. Evidence / Commands

全部原始证据见 [evidence/](evidence/)：

| 文件 | 内容 |
| --- | --- |
| `latex-gradle-tasks-root.txt` | `gradlew tasks --all`（root，182KB 完整输出） |
| `latex-gradle-tasks-modules.txt` | 7 个模块 `tasks --all` 合并（152KB） |
| `latex-publish-sign-tasks-filtered.txt` | publish/sign/publication/metadata 相关 task 过滤视图 |
| `latex-publishToMavenLocal-console.txt` | 本地发布（真实签名链）完整控制台输出 |
| `latex-publish-executed-tasks.txt` | 实际执行的 task 全集（含 sign*/EmptyJavadoc*/SourcesJar/Klib…） |
| `m2-before.txt` / `m2-after.txt` / `latex-m2-new-files.txt` | `~/.m2` 前后快照与 latex 257 个新文件清单 |
| `latex-central-staging-upload-bundle.txt` | 假凭据 401 实验捕获的 Central 上传 staging 包结构 |
| `latex-central-staging-console.txt` | 该实验控制台输出（含 "Upload failed: Invalid token"） |
| `central-repo1-remote-evidence.md` | repo1.maven.org 远程取证摘要（目录清单/时间戳/字节数） |

关键命令记录：

```powershell
# 1. 枚举 publish/signing task（每模块 19 publish + 7 sign）
.\gradlew :latex-base:tasks --all :latex-parser:tasks --all :latex-renderer:tasks --all

# 2. 本地发布（真实 GPG 签名链复现）
.\gradlew :latex-base:publishToMavenLocal :latex-parser:publishToMavenLocal :latex-renderer:publishToMavenLocal `
    -Psigning.keyId=<临时密钥> -Psigning.secretKeyRingFile=<临时keyring> -Psigning.password=

# 3. 捕获 Central 上传包（假凭据，401 终止，绝不真实上传）
$env:ORG_GRADLE_PROJECT_mavenCentralUsername='fake-user'
.\gradlew :latex-base:publishAllPublicationsToMavenCentralRepository   # → 检查 ./mavenCentral/
```
