# POM 元数据清理（zusrsoft 身份统一 + 1.5.5）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 latex 仓库 3 个发布模块的 POM developers 从 `huarangmeng` 统一为 `zusrsoft`，同步 LICENSE/README 版权行与作者/维护者署名，并把 VERSION bump 到 1.5.5。

**Architecture:** 纯构建元数据与文档变更，无运行时影响。逐文件直接修改（方案 A），不抽取 convention plugin。规格见 `docs/superpowers/specs/2026-09-08-pom-developer-metadata-design.md`。

**Tech Stack:** Kotlin Gradle (`com.vanniktech.maven.publish`)，Markdown 文档。验证用 `generatePomFileForKotlinMultiplatformPublication` 任务 + ripgrep。

**仓库根：** `D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\latex`（独立 git 仓库，Windows / PowerShell）

---

### Task 1: 替换 3 个模块 POM developers 块

**Files:**
- Modify: `latex-base/build.gradle.kts:91-96`
- Modify: `latex-parser/build.gradle.kts:100-105`
- Modify: `latex-renderer/build.gradle.kts:116-121`

- [ ] **Step 1: 修改 `latex-base/build.gradle.kts`**

先 Read 该文件（Edit 前置要求），然后将：

```kotlin
        developers {
            developer {
                id.set("huarangmeng")
                name.set("Kotlin Multiplatform Specialist")
                url.set("https://github.com/huarangmeng/")
            }
        }
```

替换为：

```kotlin
        developers {
            developer {
                id.set("zusrsoft")
                name.set("Kotlin Multiplatform Specialist")
                url.set("https://github.com/zusrsoft/")
            }
        }
```

- [ ] **Step 2: 对 `latex-parser/build.gradle.kts` 做相同替换**（先 Read，再 Edit，oldString/newString 同 Step 1）

- [ ] **Step 3: 对 `latex-renderer/build.gradle.kts` 做相同替换**（先 Read，再 Edit，oldString/newString 同 Step 1）

- [ ] **Step 4: 验证无遗漏**

Run: `rg -n "huarangmeng" latex-base/build.gradle.kts latex-parser/build.gradle.kts latex-renderer/build.gradle.kts`
Expected: 无输出（退出码 1）

- [ ] **Step 5: Commit**

```powershell
git add latex-base/build.gradle.kts latex-parser/build.gradle.kts latex-renderer/build.gradle.kts
git commit -m "build: replace POM developer identity with zusrsoft across published modules"
```

---

### Task 2: LICENSE 与两个 README 页脚版权行

**Files:**
- Modify: `LICENSE:3`
- Modify: `README.md:533`
- Modify: `README_zh.md:531`

- [ ] **Step 1: LICENSE 第 3 行**

先 Read `LICENSE`，将 `Copyright (c) 2026 huarangmeng` 替换为 `Copyright (c) 2026 zusrsoft`

- [ ] **Step 2: README.md License 代码块内版权行**

先 Read `README.md`（530-535 行区域），将 `Copyright (c) 2026 huarangmeng` 替换为 `Copyright (c) 2026 zusrsoft`

- [ ] **Step 3: README_zh.md 同样处理**

先 Read `README_zh.md`（528-533 行区域），将 `Copyright (c) 2026 huarangmeng` 替换为 `Copyright (c) 2026 zusrsoft`

- [ ] **Step 4: Commit**

```powershell
git add LICENSE README.md README_zh.md
git commit -m "docs: update copyright holder to zusrsoft in license and README footers"
```

（注意：此提交只包含版权行；致谢署名在 Task 3 编辑同一批文件，分开提交需注意暂存时机——若 Task 3 紧接着执行，也可合并为一个提交 `docs: update copyright and add author/maintainer attribution`。）

---

### Task 3: README 致谢章节添加作者/维护者署名

**Files:**
- Modify: `README.md:518-521`（Acknowledgements 章节）
- Modify: `README_zh.md:516-519`（致谢章节）

- [ ] **Step 1: README.md 在 KaTeX 条目后追加署名行**

先 Read `README.md`（515-525 行区域），将该段落：

```markdown
## 🙏 Acknowledgements

- [KaTeX](https://github.com/KaTeX/KaTeX) — This project uses the KaTeX v0.16.11 font files for mathematical formula rendering. See [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for attribution and license details.
```

替换为：

```markdown
## 🙏 Acknowledgements

- [KaTeX](https://github.com/KaTeX/KaTeX) — This project uses the KaTeX v0.16.11 font files for mathematical formula rendering. See [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for attribution and license details.
- Author: [huarangmeng](https://github.com/huarangmeng/) — this project is now maintained by [zusrsoft](https://github.com/zusrsoft/).
```

- [ ] **Step 2: README_zh.md 对应追加**

先 Read `README_zh.md`（514-520 行区域），将该段落：

```markdown
## 🙏 致谢

- [KaTeX](https://github.com/KaTeX/KaTeX) — 本项目使用 KaTeX v0.16.11 字体文件进行数学公式渲染；来源、署名和许可证详情见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
```

替换为：

```markdown
## 🙏 致谢

- [KaTeX](https://github.com/KaTeX/KaTeX) — 本项目使用 KaTeX v0.16.11 字体文件进行数学公式渲染；来源、署名和许可证详情见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
- 作者：[huarangmeng](https://github.com/huarangmeng/) —— 本项目现由 [zusrsoft](https://github.com/zusrsoft/) 维护。
```

- [ ] **Step 3: Commit（若与 Task 2 分开）**

```powershell
git add README.md README_zh.md
git commit -m "docs: add author/maintainer attribution in README acknowledgements"
```

---

### Task 4: VERSION bump 1.5.5

**Files:**
- Modify: `gradle.properties:14`

- [ ] **Step 1: 修改版本号**

先 Read `gradle.properties`，将 `VERSION=1.5.4` 替换为 `VERSION=1.5.5`

- [ ] **Step 2: Commit**

```powershell
git add gradle.properties
git commit -m "build: bump VERSION to 1.5.5"
```

---

### Task 5: 端到端验证

**Files:** 无新改动（只读验证）

- [ ] **Step 1: 生成三模块 POM**

Run: `.\gradlew.bat :latex-base:generatePomFileForKotlinMultiplatformPublication :latex-parser:generatePomFileForKotlinMultiplatformPublication :latex-renderer:generatePomFileForKotlinMultiplatformPublication`
Expected: BUILD SUCCESSFUL。若任务名不存在，用 `.\gradlew.bat :latex-base:tasks --all | rg "generatePomFile"` 找到实际任务名后重试。

- [ ] **Step 2: 检查生成的 POM 内容**

Run: `rg -n "zusrsoft|huarangmeng|1\.5\.5" latex-base/build/publications/KotlinMultiplatform/pom-default.xml latex-parser/build/publications/KotlinMultiplatform/pom-default.xml latex-renderer/build/publications/KotlinMultiplatform/pom-default.xml`
Expected: 每个文件含 `<id>zusrsoft</id>`、`<url>https://github.com/zusrsoft/</url>`、`<version>1.5.5</version>`，且不出现 `huarangmeng`

- [ ] **Step 3: 改动范围终检**

Run: `rg -n "huarangmeng" LICENSE README.md README_zh.md gradle.properties latex-base/build.gradle.kts latex-parser/build.gradle.kts latex-renderer/build.gradle.kts`
Expected: 仅 README.md / README_zh.md 各剩 1 处（致谢署名行，刻意保留）；其余文件无匹配

- [ ] **Step 4: 确认工作区状态**

Run: `git status --short; git log --oneline -5`
Expected: 改动文件均已提交；本次新增 3-4 个提交；`gradle/wrapper/gradle-wrapper.properties` 的既有改动与 `docs/maven-central/`、`p0-repo-out/` 未跟踪目录保持原样（不属于本任务，勿动）
