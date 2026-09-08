# latex POM 元数据清理 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 `io.github.zusrsoft` 坐标下 POM 仍显示开发者 `huarangmeng` 的身份错配：公共 POM 配置上移根脚本、developers 改为 zusrsoft、版本 1.5.5、README/LICENSE 声明 fork 关系（作者 huarangmeng / 维护者 zusrsoft）。

**Architecture:** 仿 codehigh 已验证模式——根 `build.gradle.kts` 用 `subprojects { plugins.withId("com.vanniktech.maven.publish") {...} }` 收口 `publishToMavenCentral`/`signAllPublications`/POM 公共字段；三个发布模块只留 `coordinates()` + `name`/`description`（description 尾注 fork 声明）。纯配置与文档变更，无代码/测试改动。

**Tech Stack:** Gradle Kotlin DSL、vanniktech `com.vanniktech.maven.publish` 0.35.0（API 与 codehigh 根脚本相同）、PowerShell 5.1。

**规格文档:** `docs/superpowers/specs/2026-09-08-pom-metadata-cleanup-design.md`

---

## 环境与全局注意事项

- **工作目录**：所有命令在 `D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\latex`（仓库根）执行；Shell 为 PowerShell 5.1
- **⚠️ 仓库已有无关未提交变更**：`gradle/wrapper/gradle-wrapper.properties`（已修改）、`docs/maven-central/`、`p0-repo-out/`（未跟踪）。**每次 commit 只 `git add` 任务中明确列出的文件，绝不用 `git add -A` / `git add .`**
- `GRADLE_USER_HOME` 本机为 `D:\Dev-tools\gradle-repository`（命令中用 `$env:GRADLE_USER_HOME` 引用），签名凭证已配置（`MAVEN_CENTRAL_PUBLISH.md` 第 3/5 节），`publishToMavenLocal` 本机可用
- Gradle 命令统一带 `--no-configuration-cache`（发布手册惯例）

---

### Task 1: POM 公共块上移（根脚本 + 三模块瘦身）

**Files:**
- Modify: `build.gradle.kts`（根）
- Modify: `latex-base/build.gradle.kts:65-104`
- Modify: `latex-parser/build.gradle.kts:74-113`
- Modify: `latex-renderer/build.gradle.kts:84-129`

⚠️ 本任务 4 个编辑必须作为整体完成（编辑到一半的中间态根/模块双重配置不可构建），最后一次性提交。

- [ ] **Step 1: 修改根 `build.gradle.kts`**

在现有 `plugins { ... }` 块之后追加 `subprojects` 块，并在文件顶部加 import。文件最终完整内容：

```kotlin
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.allopen) apply false
    alias(libs.plugins.kotlinxBenchmark) apply false
    alias(libs.plugins.mavenPublish) apply false
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(true)
            signAllPublications()
            pom {
                inceptionYear.set("2026")
                url.set("https://github.com/zusrsoft/latex")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("zusrsoft")
                        name.set("zusrsoft")
                        url.set("https://github.com/zusrsoft/")
                    }
                }
                scm {
                    url.set("https://github.com/zusrsoft/latex")
                    connection.set("scm:git:git://github.com/zusrsoft/latex.git")
                    developerConnection.set("scm:git:ssh://git@github.com/zusrsoft/latex.git")
                }
            }
        }
    }
}
```

- [ ] **Step 2: 瘦身 `latex-base/build.gradle.kts` 的 mavenPublishing 块**

将（第 65-104 行）：

```kotlin
mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    coordinates("io.github.zusrsoft", "latex-base", rootProject.property("VERSION").toString())

    pom {
        name.set("Kotlin Multiplatform LaTeX Rendering Engine")
        description.set("""
            Cross-platform LaTeX math rendering solution with:
            - Full LaTeX syntax support (math mode)
            - Custom command definitions
            - Chemical formula rendering
            - Compose Multiplatform UI integration
            - Multi-module architecture (base/parser/renderer)
        """.trimIndent())
        inceptionYear.set("2026")
        url.set("https://github.com/zusrsoft/latex")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("huarangmeng")
                name.set("Kotlin Multiplatform Specialist")
                url.set("https://github.com/huarangmeng/")
            }
        }
        scm {
            url.set("https://github.com/zusrsoft/latex")
            connection.set("scm:git:git://github.com/zusrsoft/latex.git")
            developerConnection.set("scm:git:ssh://git@github.com/zusrsoft/latex.git")
        }
    }
}
```

替换为：

```kotlin
mavenPublishing {
    coordinates("io.github.zusrsoft", "latex-base", rootProject.property("VERSION").toString())

    pom {
        name.set("Kotlin Multiplatform LaTeX Rendering Engine")
        description.set("""
            Cross-platform LaTeX math rendering solution with:
            - Full LaTeX syntax support (math mode)
            - Custom command definitions
            - Chemical formula rendering
            - Compose Multiplatform UI integration
            - Multi-module architecture (base/parser/renderer)

            Maintained fork of huarangmeng/latex.
        """.trimIndent())
    }
}
```

- [ ] **Step 3: 瘦身 `latex-parser/build.gradle.kts` 的 mavenPublishing 块**

将（第 74-113 行）：

```kotlin
mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    coordinates("io.github.zusrsoft", "latex-parser", rootProject.property("VERSION").toString())

    pom {
        name.set("Kotlin Multiplatform LaTeX Parser")
        description.set("""
            Cross-platform LaTeX math parsing solution with:
            - Full LaTeX syntax support (math mode)
            - Custom command definitions
            - Chemical formula rendering
            - Compose Multiplatform UI integration
            - Multi-module architecture (base/parser/renderer)
        """.trimIndent())
        inceptionYear.set("2026")
        url.set("https://github.com/zusrsoft/latex")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("huarangmeng")
                name.set("Kotlin Multiplatform Specialist")
                url.set("https://github.com/huarangmeng/")
            }
        }
        scm {
            url.set("https://github.com/zusrsoft/latex")
            connection.set("scm:git:git://github.com/zusrsoft/latex.git")
            developerConnection.set("scm:git:ssh://git@github.com/zusrsoft/latex.git")
        }
    }
}
```

替换为：

```kotlin
mavenPublishing {
    coordinates("io.github.zusrsoft", "latex-parser", rootProject.property("VERSION").toString())

    pom {
        name.set("Kotlin Multiplatform LaTeX Parser")
        description.set("""
            Cross-platform LaTeX math parsing solution with:
            - Full LaTeX syntax support (math mode)
            - Custom command definitions
            - Chemical formula rendering
            - Compose Multiplatform UI integration
            - Multi-module architecture (base/parser/renderer)

            Maintained fork of huarangmeng/latex.
        """.trimIndent())
    }
}
```

- [ ] **Step 4: 瘦身 `latex-renderer/build.gradle.kts` 的 mavenPublishing 块**

将（第 84-129 行）：

```kotlin
mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    coordinates(
        "io.github.zusrsoft",
        "latex-renderer",
        rootProject.property("VERSION").toString()
    )

    pom {
        name.set("Kotlin Multiplatform LaTeX Rendering Engine")
        description.set(
            """
            Cross-platform LaTeX math rendering solution with:
            - Full LaTeX syntax support (math mode)
            - Custom command definitions
            - Chemical formula rendering
            - Compose Multiplatform UI integration
            - Multi-module architecture (base/parser/renderer)
        """.trimIndent()
        )
        inceptionYear.set("2026")
        url.set("https://github.com/zusrsoft/latex")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("huarangmeng")
                name.set("Kotlin Multiplatform Specialist")
                url.set("https://github.com/huarangmeng/")
            }
        }
        scm {
            url.set("https://github.com/zusrsoft/latex")
            connection.set("scm:git:git://github.com/zusrsoft/latex.git")
            developerConnection.set("scm:git:ssh://git@github.com/zusrsoft/latex.git")
        }
    }
}
```

替换为：

```kotlin
mavenPublishing {
    coordinates(
        "io.github.zusrsoft",
        "latex-renderer",
        rootProject.property("VERSION").toString()
    )

    pom {
        name.set("Kotlin Multiplatform LaTeX Rendering Engine")
        description.set(
            """
            Cross-platform LaTeX math rendering solution with:
            - Full LaTeX syntax support (math mode)
            - Custom command definitions
            - Chemical formula rendering
            - Compose Multiplatform UI integration
            - Multi-module architecture (base/parser/renderer)

            Maintained fork of huarangmeng/latex.
        """.trimIndent()
        )
    }
}
```

- [ ] **Step 5: 生成三模块 POM 验证配置期可用**

Run:
```powershell
.\gradlew.bat :latex-base:generatePomFileForKotlinMultiplatformPublication :latex-parser:generatePomFileForKotlinMultiplatformPublication :latex-renderer:generatePomFileForKotlinMultiplatformPublication --no-configuration-cache
```
Expected: `BUILD SUCCESSFUL`（无配置期错误）

- [ ] **Step 6: 断言生成 POM 内容**

Run:
```powershell
$poms = @("latex-base\build\publications\kotlinMultiplatform\pom-default.xml","latex-parser\build\publications\kotlinMultiplatform\pom-default.xml","latex-renderer\build\publications\kotlinMultiplatform\pom-default.xml")
Select-String -Path $poms -Pattern "<id>huarangmeng</id>"
Select-String -Path $poms -Pattern "<id>zusrsoft</id>","Maintained fork of huarangmeng/latex\."
```
Expected: 第一条命令**无任何输出**；第二条命令每个文件各 2 条命中（`<id>zusrsoft</id>` + fork 尾注）

- [ ] **Step 7: Commit**

```powershell
git add build.gradle.kts latex-base/build.gradle.kts latex-parser/build.gradle.kts latex-renderer/build.gradle.kts
git commit -m "build: hoist shared POM config to root script; developers -> zusrsoft; fork note in descriptions"
```

---

### Task 2: 版本号 1.5.5 与 README 依赖示例同步

**Files:**
- Modify: `gradle.properties:14`
- Modify: `README.md:445-446,456,470`
- Modify: `README_zh.md:443-444,454,468`

- [ ] **Step 1: 修改 `gradle.properties`**

`VERSION=1.5.4` → `VERSION=1.5.5`

- [ ] **Step 2: 修改 `README.md`（4 处）**

1. `| **Standard** | 2.3.10 | 1.10.3 | \`1.5.4\`          |` → `| **Standard** | 2.3.10 | 1.10.3 | \`1.5.5\`          |`
2. `| **Kotlin 2.1.0** | 2.1.0 | 1.9.3 | \`1.5.4-kt2.1.0\`  |` → `| **Kotlin 2.1.0** | 2.1.0 | 1.9.3 | \`1.5.5-kt2.1.0\`  |`
3. `latex = "1.5.4"` → `latex = "1.5.5"`
4. `latex = "1.5.4-kt2.1.0"` → `latex = "1.5.5-kt2.1.0"`

- [ ] **Step 3: 修改 `README_zh.md`（4 处）**

1. `| **标准版** | 2.3.10 | 1.10.3 | \`1.5.4\` |` → `| **标准版** | 2.3.10 | 1.10.3 | \`1.5.5\` |`
2. `| **Kotlin 2.1.0 兼容版** | 2.1.0 | 1.9.3 | \`1.5.4-kt2.1.0\` |` → `| **Kotlin 2.1.0 兼容版** | 2.1.0 | 1.9.3 | \`1.5.5-kt2.1.0\` |`
3. `latex = "1.5.4"` → `latex = "1.5.5"`
4. `latex = "1.5.4-kt2.1.0"` → `latex = "1.5.5-kt2.1.0"`

- [ ] **Step 4: 校验**

Run:
```powershell
git grep -n "1\.5\.4" -- gradle.properties README.md README_zh.md
```
Expected: **无任何输出**

- [ ] **Step 5: Commit**

```powershell
git add gradle.properties README.md README_zh.md
git commit -m "chore: bump VERSION to 1.5.5 and sync README version examples"
```

---

### Task 3: Fork 归属声明（README×2 + LICENSE，角色叙事：作者 huarangmeng / 维护者 zusrsoft）

**Files:**
- Modify: `README.md:520,533`
- Modify: `README_zh.md:518,531`
- Modify: `LICENSE:3`

- [ ] **Step 1: `README.md` Acknowledgements 增加原项目条目**

将：
```markdown
- [KaTeX](https://github.com/KaTeX/KaTeX) — This project uses the KaTeX v0.16.11 font files for mathematical formula rendering. See [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for attribution and license details.
```
替换为：
```markdown
- [KaTeX](https://github.com/KaTeX/KaTeX) — This project uses the KaTeX v0.16.11 font files for mathematical formula rendering. See [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for attribution and license details.
- [huarangmeng/latex](https://github.com/huarangmeng/latex) — Original project, author: huarangmeng. This repository is its maintained fork, maintainer: [zusrsoft](https://github.com/zusrsoft).
```

- [ ] **Step 2: `README.md` License 区块双版权行**

将：
```
Copyright (c) 2026 huarangmeng
```
替换为：
```
Copyright (c) 2026 huarangmeng
Copyright (c) 2026 zusrsoft
```
（`README.md` 中该字符串唯一）

- [ ] **Step 3: `README_zh.md` 致谢增加原项目条目**

将：
```markdown
- [KaTeX](https://github.com/KaTeX/KaTeX) — 本项目使用 KaTeX v0.16.11 字体文件进行数学公式渲染；来源、署名和许可证详情见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
```
替换为：
```markdown
- [KaTeX](https://github.com/KaTeX/KaTeX) — 本项目使用 KaTeX v0.16.11 字体文件进行数学公式渲染；来源、署名和许可证详情见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
- [huarangmeng/latex](https://github.com/huarangmeng/latex) —— 原始项目，作者：huarangmeng；本仓库为其维护版 fork，维护者：[zusrsoft](https://github.com/zusrsoft)。
```

- [ ] **Step 4: `README_zh.md` License 区块双版权行**

将：
```
Copyright (c) 2026 huarangmeng
```
替换为：
```
Copyright (c) 2026 huarangmeng
Copyright (c) 2026 zusrsoft
```
（`README_zh.md` 中该字符串唯一）

- [ ] **Step 5: `LICENSE` 双版权行**

将：
```
Copyright (c) 2026 huarangmeng
```
替换为：
```
Copyright (c) 2026 huarangmeng
Copyright (c) 2026 zusrsoft
```
（`LICENSE` 中该字符串唯一；保留原行是 MIT 合规要求）

- [ ] **Step 6: 校验**

Run:
```powershell
git grep -n "Copyright (c) 2026 zusrsoft"
```
Expected: 恰好 3 条——`LICENSE`、`README.md`、`README_zh.md` 各 1

- [ ] **Step 7: Commit**

```powershell
git add LICENSE README.md README_zh.md
git commit -m "docs: declare original project and add zusrsoft copyright line"
```

---

### Task 4: 发布手册同步（MAVEN_CENTRAL_PUBLISH.md）

**Files:**
- Modify: `MAVEN_CENTRAL_PUBLISH.md:210-219,223,343,345`

- [ ] **Step 1: 重写 4.2 节配置模式描述**

将：
```markdown
### 4.2 mavenPublishing 配置（三个模块相同模式）

```kotlin
mavenPublishing {
    publishToMavenCentral(true)   // true = 校验通过后自动发布，无需手动点 Release
    signAllPublications()         // 使用 signingInMemory* 属性签名
    coordinates("io.github.zusrsoft", "latex-base", rootProject.property("VERSION").toString())
    pom { /* name/description/url/license/developer/scm 齐全，满足 Central 校验 */ }
}
```
```
替换为：
```markdown
### 4.2 mavenPublishing 配置（公共块在根 build.gradle.kts，模块仅保留差异项）

```kotlin
// 根 build.gradle.kts —— 公共配置（自动发布、签名、POM 公共字段）
subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(true)   // true = 校验通过后自动发布，无需手动点 Release
            signAllPublications()         // 使用 signingInMemory* 属性签名
            pom { /* MIT License / zusrsoft developer / zusrsoft latex scm 公共字段 */ }
        }
    }
}

// 模块（以 latex-base 为例）—— 仅保留坐标与模块级 name/description
mavenPublishing {
    coordinates("io.github.zusrsoft", "latex-base", rootProject.property("VERSION").toString())
    pom { /* name / description（description 尾注声明 fork 来源） */ }
}
```
```

- [ ] **Step 2: 更新 4.3 节当前版本**

`- 版本唯一来源：\`gradle.properties\` 的 \`VERSION\`（当前 1.5.4）` → `- 版本唯一来源：\`gradle.properties\` 的 \`VERSION\`（当前 1.5.5）`

- [ ] **Step 3: 更新 7.4 节依赖验证示例（2 行）**

1. `implementation("io.github.zusrsoft:latex-renderer:1.5.4")` → `implementation("io.github.zusrsoft:latex-renderer:1.5.5")`
2. `implementation("io.github.zusrsoft:latex-renderer:1.5.4-kt2.1.0")` → `implementation("io.github.zusrsoft:latex-renderer:1.5.5-kt2.1.0")`

- [ ] **Step 4: 校验**

Run:
```powershell
git grep -n "1\.5\.4" -- . ':!docs' ':!p0-repo-out'
```
Expected: 仅剩 1 条——`MAVEN_CENTRAL_PUBLISH.md:409:*最后更新：2026-09-06（基于 1.5.4 版本发布实战整理）*`（历史注脚，保留）

- [ ] **Step 5: Commit**

```powershell
git add MAVEN_CENTRAL_PUBLISH.md
git commit -m "docs: sync publish manual with centralized POM config and 1.5.5"
```

---

### Task 5: 端到端最终验证

**Files:** 无修改（仅验证）

- [ ] **Step 1: 全量本地发布（编译全平台 + 签名 + 装 m2）**

Run:
```powershell
.\gradlew.bat :latex-base:publishToMavenLocal :latex-parser:publishToMavenLocal :latex-renderer:publishToMavenLocal --no-configuration-cache
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: m2 产物 POM 断言**

Run:
```powershell
$m2 = Join-Path $env:GRADLE_USER_HOME "m2\io\github\zusrsoft"
$poms = @(
  "$m2\latex-base\1.5.5\latex-base-1.5.5.pom",
  "$m2\latex-parser\1.5.5\latex-parser-1.5.5.pom",
  "$m2\latex-renderer\1.5.5\latex-renderer-1.5.5.pom"
)
Select-String -Path $poms -Pattern "<id>huarangmeng</id>"
Select-String -Path $poms -Pattern "<id>zusrsoft</id>","<url>https://github.com/zusrsoft/latex</url>","Maintained fork of huarangmeng/latex\."
```
Expected: 第一条**无任何输出**；第二条每个文件各 3 条命中

- [ ] **Step 3: huarangmeng 白名单校验（非源码文件）**

Run:
```powershell
git grep -n "huarangmeng" -- . ':!docs' ':!p0-repo-out' ':!*.kt'
```
Expected: **恰好 10 条**——
- `LICENSE` 版权行 ×1
- `MAVEN_CENTRAL_PUBLISH.md:303`（6.5 节历史事故记录）×1
- `README.md` ×2（致谢条目 + License 版权行）
- `README_zh.md` ×2（致谢条目 + License 版权行）
- `latex-base/build.gradle.kts`、`latex-parser/build.gradle.kts`、`latex-renderer/build.gradle.kts` 各 ×1（description 尾注）

- [ ] **Step 4: 源码文件命中全部为版权头注释**

Run:
```powershell
$all = git grep -l "huarangmeng" -- '*.kt'
$hdr = git grep -l "Copyright (c) 2026 huarangmeng" -- '*.kt'
Compare-Object $all $hdr
```
Expected: **无任何输出**（差集为空，即所有 .kt 命中行都是版权头注释）

- [ ] **Step 5: 1.5.4 残留校验**

Run:
```powershell
git grep -n "1\.5\.4" -- . ':!docs' ':!p0-repo-out'
```
Expected: 仅剩 `MAVEN_CENTRAL_PUBLISH.md:409` 历史注脚 1 条

- [ ] **Step 6: 确认工作区只剩任务前已存在的无关变更**

Run:
```powershell
git status --short; git log --oneline -6
```
Expected: ` M gradle/wrapper/gradle-wrapper.properties` + 未跟踪 `docs/`、`p0-repo-out/`（与任务开始前一致）；本计划产生 4 个新 commit（Task 1-4）

---

## 计划自审记录

1. **Spec 覆盖**：5.1 POM 上移→Task 1；5.2 版本与示例→Task 2；5.3 归属声明→Task 3（含 POM description 尾注在 Task 1）；5.4 手册同步→Task 4；6 验证→Task 1 Step 5-6 + Task 5 全部。无遗漏。
2. **占位符扫描**：所有编辑步骤均给出精确旧/新内容；无 TBD/TODO。
3. **一致性**：fork 尾注文本 `Maintained fork of huarangmeng/latex.` 在 Task 1（三模块）与 Task 5 Step 2（断言）一致；版本 1.5.5 在 Task 2/4/5 一致；双版权行文本在 Task 3/5 一致；角色叙事文案（作者 huarangmeng / 维护者 zusrsoft）与规格 5.3 一致。

## 变更历史

- 本文件曾于 2026-09-08 被并行会话覆盖为内容相异的 189 行版本（commit `403fa26`），本版本为按已确认规格（`669a5e2`）恢复并纳入角色叙事文案的权威版本。
