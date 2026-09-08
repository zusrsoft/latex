# diagram 项目审计（zusrsoft/diagram）

> 只读审计 · 2026-09-08 · 详见联合报告 [unified-file-count-audit.md](unified-file-count-audit.md)

## 1. 基本信息

- 路径：`D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\diagram`（master @ `7d248cf`；
  1 个未跟踪文件 `MAVEN_CENTRAL_PUBLISH.md`——用户发布笔记，非本次审计产物）
- 工具链：Gradle 9.3.1 / AGP 9.1.1 / Kotlin 2.3.20 / CMP 1.10.3 / **vanniktech 0.36.0**
- 版本：`gradle.properties` `VERSION=1.0.4`（与 Central 一致）
- SDK 模块（**4 个，为四项目最多**）：diagram-core、diagram-layout、diagram-parser（A 类）、
  diagram-render（B 类，含 Compose resources）
- 非 SDK 模块：diagram-bench、androidApp、composeApp（均无 mavenPublish，实测 0 publish task）

## 2. KMP Targets（4 个 SDK 模块一致）

```kotlin
android { withJava(); optimization { consumerKeepRules.publish = true } }
jvm { }               // 块形式声明
js { browser(); nodejs() }
wasmJs { browser() }
iosArm64(); iosSimulatorArm64()
```

## 3. Publishing 配置（4 模块同构）

```kotlin
mavenPublishing {
    publishToMavenCentral(true)
    if (!project.hasProperty("signing.skip")) { signAllPublications() }  // 本地可用 -Psigning.skip 逃生
    coordinates("io.github.zusrsoft", "diagram-<m>", rootProject.property("VERSION"))
    pom { ... Apache-2.0 / zusrsoft/diagram ... }
}
```

CI（`.github/workflows/publish.yml`）：`./gradlew publishToMavenCentral`

## 4. Central 实测结构

| Artifact | Primary | Central 文件(×10) |
| --- | --- | --- |
| diagram-core / -layout / -parser（各 38） | 38×3 | 380×3 = 1,140 |
| diagram-render（Type B，42） | 42 | 420 |

**diagram 单次 release：156 primary → 1,560 Central 文件（33.1%，四项目最大）。**

## 5. 特有发现

1. **模块数最多 → 贡献最大**：4 模块中有 3 个是 Type A（无 kotlin_resources），其 1,140 文件里
   每模块 30 primary（79%）是"平台最小集"（主产物×6 + sources×7 + javadoc×7 + module×7 + pom×7
   + tooling×1 + metadata×2），没有任何可删项——**结构本身即最优，膨胀全部来自 10 倍 checksum 放大**。
2. `diagram-render` 官方在 1.0.x 系列刚推出 Compose 渲染（README 注明），resources 相关
   4 个 kotlin_resources primary 将伴随所有后续 release。
3. `signing.skip` 属性是四项目中唯一存在的签名逃生开关（发布时不应启用，仅本地构建用）。
4. 1.0.4 是 diagram 的中央首发版本（repo1 metadata 仅含 1.0.4；用户 notes 记录早期发布失败历史：
   java17 工具链/NA 平台 klib/认证问题均已在 1.0.4 解决）。
