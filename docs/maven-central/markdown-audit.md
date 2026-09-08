# Markdown 项目审计（zusrsoft/Markdown）——任务书外补充

> 只读审计 · 2026-09-08 · **任务书只列了 latex/codehigh/diagram 三个项目，但本审计发现
> `io.github.zusrsoft` namespace 还有第四个发布者：zusrsoft/Markdown。它在 2026-09-06 的
> 4 个 Release 中占一个，File Count 1,180 = 总量的 25%。** 若不算它，4,720 无法被解释
> （3 项目合计仅 3,540）。因此一并纳入审计，详见联合报告 [unified-file-count-audit.md](unified-file-count-audit.md)。

## 1. 基本信息

- 路径：`D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\Markdown`（master @ `9e581e3`，clean）
- 工具链：Gradle 9.3.1 / AGP 9.1.1 / **Kotlin 2.4.0**（唯一用 2.4.0 的项目）/ CMP **1.11.1** /
  **vanniktech 0.36.0**
- 版本：`gradle.properties` `VERSION=1.5.2`（与 Central 一致）
- SDK 模块（3 个）：markdown-parser（A）、markdown-renderer（B，Compose resources）、
  markdown-runtime（A，含 KMP hierarchy 源集）
- 非 SDK 模块：markdown-preview、markdown-benchmark、macrobenchmark、composeApp、androidapp
  （均无 mavenPublish，实测 0 publish task）
- **依赖关系**：依赖 `io.github.zusrsoft:latex:1.5.4` 与 `io.github.zusrsoft:codehighlight:1.1.2`
  ——四项目发布顺序（latex 14:04 → diagram 14:41 → codehigh 14:48 → Markdown 15:02）与之吻合。

## 2. KMP Targets（3 个 SDK 模块）

```kotlin
android { ... }
jvm()
js { browser() }
wasmJs { browser() }
iosArm64(); iosSimulatorArm64()   // 经 Markdown 发布笔记 MAVEN_CENTRAL_PUBLISH.md 确认为 6 平台
```

## 3. Publishing 配置（模块同构，多一个环境变量门控）

```kotlin
mavenPublishing {
    if (providers.environmentVariable("RELEASE_TO_CENTRAL").isPresent) {
        publishToMavenCentral(true)
        signAllPublications()
    }
    coordinates("io.github.zusrsoft", "markdown-<m>", rootProject.property("VERSION"))
    pom { ... }
}
```

CI（`.github/workflows/publish.yml`）：
`env: RELEASE_TO_CENTRAL: true` + `./gradlew publishAllPublicationsToMavenCentralRepository`
（release 分支 = `1.x.x` 标签触发，代码在 `markdown/` 目录）。

## 4. Central 实测结构

| Artifact | Primary | Central 文件(×10) |
| --- | --- | --- |
| markdown-parser / markdown-runtime（各 38） | 38×2 | 380×2 = 760 |
| markdown-renderer（Type B，42） | 42 | 420 |

**Markdown 单次 release：118 primary → 1,180 Central 文件（25.0%）。**

## 5. 体积

本地 .m2 实测全量 1.5.2 产物 = **16.10 MB**（略大于 latex 的 15.04 MB）。

## 6. 特有发现

1. **共享 namespace 的第四发布者**：这意味着任何 OSS limit adjustment 申请与发布节奏规划
   都必须以"4 个项目合计"为口径（示例：若每月每项目各发一次，当月 = 4,720；若错峰到不同月，
   各月平均仍 ≈ 1,180+ 需评估滚动三月平均）。
2. Kotlin 2.4.0 + CMP 1.11.1 组合说明其仍在积极升级——**未来 release 频率可能最高**。
3. `RELEASE_TO_CENTRAL` 门控是四项目中最安全的发布模式（本地/CI 默认不发布）。
4. repo1 时间戳 15:02-15:09 为四项目当日最后一发，确认其依赖前三个项目先完成发布。
