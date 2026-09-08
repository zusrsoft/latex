# latex 项目审计（zusrsoft/latex）

> 只读审计 · 2026-09-08 · 详见联合报告 [unified-file-count-audit.md](unified-file-count-audit.md)

## 1. 基本信息

- 路径：`D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\latex`（master @ `ee421eb`，clean）
- 工具链：Gradle 9.7.1（2026-09-08 由 9.3.1 升级）/ AGP 9.1.1 / Kotlin 2.3.10 / CMP 1.10.3 / **vanniktech 0.35.0**
- 版本：`gradle.properties` `VERSION=1.5.4`（与 Central 一致）；另有 `KATEZ_FONT_VERSION=0.16.11`
- SDK 模块：latex-base、latex-parser、latex-renderer（均 apply `com.vanniktech.maven.publish`）
- 非 SDK 模块：composeApp、androidapp、latex-preview、latex-benchmark（均无 mavenPublish）

## 2. KMP Targets（3 个 SDK 模块一致）

```kotlin
android { withJava(); optimization { consumerKeepRules.publish = true } }
jvm()
js { browser(); binaries.executable() }
wasmJs { browser(); binaries.executable() }
iosArm64(); iosSimulatorArm64()   // 各自 binaries.framework { isStatic = true }
```

## 3. Publishing 配置（3 模块同构）

```kotlin
mavenPublishing {
    publishToMavenCentral(true)     // 自动 release
    signAllPublications()           // 7 个 sign*Publication task
    coordinates("io.github.zusrsoft", "latex-<m>", rootProject.property("VERSION"))
    pom { ... MIT / zusrsoft/latex ... }
}
```

CI（`.github/workflows/publish.yml`，macOS-latest）：

- Job `publish`：`./gradlew publishAllPublicationsToMavenCentralRepository` → 1.5.4 ✓
- Job `publish-alias`：临时改 `VERSION=1.5.4-kt2.1.0` + 降级 Kotlin 2.1.0/CMP 1.9.3 后再发布一次
  → **本次未出现在 Central**（metadata 仅 1.5.4），但设计上会把 latex 每次 release 的 File Count ×2
  （+1,180）。这是四项目中唯一的双发机制，**保守方案首选停用项**。

## 4. Gradle 实测（本机执行，Windows）

- `tasks --all`：每 SDK 模块 **19 个 publish task / 7 个 sign task**；
  latex-preview、latex-benchmark、composeApp、androidapp 均为 **0/0** → 不参与发布。
- `publishToMavenLocal`（真实 GPG 签名）：3 模块共产出 **257 个本地文件**
  （83 + 83 + 91，= 118 primary + 118 .asc + 21 maven-metadata-local.xml）。
- staging 上传包复现（假凭据 401）：每 primary 10 文件 + 每 artifactId 5 个 metadata 文件。

## 5. 每模块文件结构（Central 实测 = 本地复现一致）

| Artifact | Primary | Central 文件(×10) | 备注 |
| --- | --- | --- | --- |
| latex-base（root） | 6 | 60 | 含 kotlin-tooling-metadata.json |
| latex-base-{android,jvm,js,wasm-js} | 5×4 | 50×4 | aar/jar/klib/klib |
| latex-base-{iosarm64,iossimulatorarm64} | 6×2 | 60×2 | + metadata.jar |
| latex-parser（同 base 结构） | 38 | 380 | — |
| latex-renderer | 42 | 420 | Type B：js/wasm/ios×2 各 +1 `kotlin_resources` zip |

> 注：此处 Central 文件数不含每 artifactId 的 maven-metadata.xml(+4 checksum)——
> 该 5 个文件不计入 Usage Center File Count（见联合报告 §16）。

**latex 单次 release：118 primary → 1,180 Central 文件 + 15.04 MB**（.m2 实测合计）。
占 namespace 当月 4,720 的 **25.0%**。

## 6. 特有发现

1. **别名双发 Job** 是最大隐患（未来 ×2）。
2. latex-renderer 的 Compose resources → 4 个额外 `-kotlin_resources.kotlin_resources.zip`
   primary（KaTeX 字体等运行时资源，**不可删**）。
3. 84 个 javadoc 全为 261B 空桩（`<t>EmptyJavadocJar`），符合 Central 要求。
4. `libs.versions.toml` 中仍保留 `io.github.zusrsoft:latex-*:1.4.8` 自引用库声明（当前未被
   androidapp 使用，已注释）——不影响发布，仅提示清理。
