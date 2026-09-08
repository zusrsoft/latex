# codehigh 项目审计（zusrsoft/codehigh）

> 只读审计 · 2026-09-08 · 详见联合报告 [unified-file-count-audit.md](unified-file-count-audit.md)

## 1. 基本信息

- 路径：`D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\codehigh`（master @ `4c8b8e9`，clean）
- 工具链：Gradle 9.3.1 / AGP 9.1.1 / Kotlin 2.3.20 / CMP 1.10.3 / **vanniktech 0.36.0**
- Central 版本：**1.1.2**（2026-09-06 14:48-55 发布）；本地 `gradle.properties` `VERSION=2.0.0`（未发布）
- SDK 模块：codehighlight-parser（A 类）、codehighlight-render（B 类，含 Compose resources）
- 非 SDK 模块：codehigh 根模块本身非 SDK；codehighlight-preview、androidApp、composeApp
  （均无 mavenPublish，`tasks --all` 实测 0 publish task）

> 名称备注：项目目录 codehigh、git 组织 codehigh、artifact 前缀 `codehighlight` 三个词同时存在，
> 全部代码文件中混用（含 `codehigh-base` 模块定义但**未在 Central 发布**——repo1 无对应 artifact）。

## 2. KMP Targets（2 个 SDK 模块）

```kotlin
android { withJava(); optimization { consumerKeepRules.publish = true } }
jvm()
js { browser(); nodejs(); binaries.executable() }      // nodejs 仅本地，不影响发布数
wasmJs { browser(); binaries.executable() }
iosArm64(); iosSimulatorArm64()
```

## 3. Publishing 配置（本模块最简）

```kotlin
mavenPublishing {
    // 无显式 publishToMavenCentral/signAllPublications！
    // 仅 coordinates(...)，Central 发布与签名由 CI 凭据/属性自动激活（vanniktech 0.36.0 行为）
    coordinates("io.github.zusrsoft", "codehighlight-parser|render", ...)
    pom { ... Apache-2.0 ... }
}
```

```kotlin
plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
    kotlin("multiplatform")
}
```

CI（`.github/workflows/publish.yml`）：

```yaml
- run: ./gradlew publishToMavenCentral   # vanniktech 复合任务 = upload + centralPortal 自动 release
  env: { ORG_GRADLE_PROJECT_mavenCentralUsername/Password, SIGNING_IN_MEMORY_KEY, ... }
```

## 4. Central 实测结构（与本地模块结构一致）

| Artifact | Primary | Central 文件(×10) |
| --- | --- | --- |
| codehighlight-parser（root+6 target） | 38 | 380 |
| codehighlight-render | 42 | 420 |

**codehighlight 单次 release：80 primary → 800 Central 文件（16.9%）。**

## 5. 体积

- parser 全量（本地 2.0.0 版本实测）：1.61 MB（1.1.2 无本地副本，按结构推算相当或略小）
- render：无本地 1.1.2；按已取证 klib 规模（js 3.5MB、iosarm64 ~2MB）推算全项目 ≈ 15±4 MB
- **该 Release 总上传（Usage Center）≈ 1.61 + 估算，本审计不给出精确 Size 值**（无本地产物可用）

> 说明：Usage Center Release Size 69.66 MB 为 4 项目合计。latex（15.04）与 Markdown（16.10）
> 为本地实测精确值；codehigh 与 diagram 的精确拆分需要各自的本地 .m2 产物或 Central 分 Release
> 统计，本审计只做文件数口径的精确拆分。

## 6. 特有发现

1. **本地 2.0.0 未发布**：本地 .m2 有 codehighlight-parser 2.0.0 的 publishToMavenLocal 产物，
   但 Central 停留在 1.1.2。若未来发布 2.0.0，会再贡献 800 文件——**建议与新功能合并为一个
   release 后择机发布**（与 latex/Markdown/diagram 错峰，控制滚动三月平均）。
2. 无显式 signAllPublications 却成功签名发布 → 说明 vanniktech 凭据存在时自动启用
   （与 latex 显式 DSL 行为一致，签名产物结构相同，repo1 实测佐证）。
3. 模块命名三词混用（codehigh/codehighlight/codehigh-base）为维护性债，不影响发布。
