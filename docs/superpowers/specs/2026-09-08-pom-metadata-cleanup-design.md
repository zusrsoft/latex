# 设计：latex POM 元数据清理与 fork 身份声明（1.5.5）

日期：2026-09-08
状态：已确认（用户批准）
范围：`D:\dev-java-learn-2026-ai\huarangmeng-zusrsoft\latex`（`github.com/zusrsoft/latex`）

## 1. 背景与问题

仓库已从 `io.github.huarangmeng` 迁移坐标至 `io.github.zusrsoft`（commit `ee421eb`），但三个发布模块（`latex-base` / `latex-parser` / `latex-renderer`）各自 `build.gradle.kts` 中的 POM `<developers>` 仍为原作者 `huarangmeng`，与 `io.github.zusrsoft` 命名空间、`zusrsoft/latex` SCM 形成公开的身份错配（Maven Central / MvnRepository 可见）。

根因：POM 配置在三个模块中复制粘贴，命名空间迁移时只改了 `coordinates()` / `url` / `scm`，漏改 `developers`。

对照：`codehigh` 项目已在根 `build.gradle.kts` 用 `subprojects { plugins.withId("com.vanniktech.maven.publish") {...} }` 收口公共 POM 块（commit `c6c8224`），无此问题。`diagram/`、`Markdown/` 的 developers 也已是 `zusrsoft`，仅 latex 有残留。

## 2. 目标

1. 下一版本（1.5.5）发布时 POM 公开元数据干净：developers 仅 `zusrsoft`，无 `huarangmeng`
2. 在 README / POM / LICENSE 中明确 fork 生态关系（Original project: huarangmeng/latex；Maintained fork: zusrsoft/latex）
3. 消除三处重复的 POM 配置，防止同类漏改再次发生

## 3. 非目标

- 不修改 `docs/maven-central/` 下任何审计证据文档（它们是 1.5.4 已发布事实的历史记录）
- 不改动 `.github/workflows/publish.yml` 发布流程（不含 developer 信息；实现时复核）
- 不处理其他三个项目（diagram / Markdown / codehigh 已干净）
- 不修改约 200 个 Kotlin 源文件头部的 `* Copyright (c) 2026 huarangmeng` 注释与 `com.hrm.latex.*` 包名（fork 保留文件级原始版权声明属 MIT 合规良好实践；改包名是破坏性 API 变更）
- 不修改 `MAVEN_CENTRAL_PUBLISH.md` 6.5 节的 `io.github.huarangmeng` 历史事故记录（文档记录事实）
- 不在本设计内执行实际发布（发布仍走 MAVEN_CENTRAL_PUBLISH.md 手册 / Release CI 流程）

## 4. 已确认的决策

| 决策点 | 结论 |
|---|---|
| POM developers | 仅 `zusrsoft`（id=zusrsoft, name=zusrsoft, url=https://github.com/zusrsoft/） |
| 版本号 | `1.5.4 → 1.5.5`（patch；Central 不允许覆盖已发布版本） |
| LICENSE 版权行 | 双行追加：保留 `Copyright (c) 2026 huarangmeng`，新增 `Copyright (c) 2026 zusrsoft`（MIT 合规要求保留原声明） |
| 署名叙事 | 「维护者 zusrsoft、作者 huarangmeng」：README 致谢与 LICENSE 采用角色化表述；POM developers 仅 zusrsoft（不进双条目） |
| POM 配置结构 | 方案 A：公共块上移根 `build.gradle.kts`（仿 codehigh 模式） |

## 5. 设计详情

### 5.1 POM 公共块上移

根 `build.gradle.kts` 新增：

```kotlin
import com.vanniktech.maven.publish.MavenPublishBaseExtension

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(true)
            signAllPublications()
            pom {
                inceptionYear.set("2026")
                url.set("https://github.com/zusrsoft/latex")
                licenses { /* MIT License, https://opensource.org/licenses/MIT, repo */ }
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

三个库模块的 `mavenPublishing` 块删除 `publishToMavenCentral(true)`、`signAllPublications()` 及 pom 公共字段（inceptionYear / url / licenses / developers / scm），仅保留：

```kotlin
mavenPublishing {
    coordinates("io.github.zusrsoft", "<artifactId>", rootProject.property("VERSION").toString())
    pom {
        name.set("<模块名>")
        description.set("""<原描述>
Maintained fork of huarangmeng/latex.""")
    }
}
```

插件版本：vanniktech 0.35.0（`gradle/libs.versions.toml`），与 codehigh 根脚本所用 API 相同，`MavenPublishBaseExtension` 配置方式可直接复用。

### 5.2 版本与依赖示例同步

- `gradle.properties`：`VERSION=1.5.4 → 1.5.5`
- `README.md` / `README_zh.md`：版本矩阵表格（约 443-446 行）与 version catalog 示例（约 454-470 行）中 `1.5.4 → 1.5.5`、`1.5.4-kt2.1.0 → 1.5.5-kt2.1.0`
- `docs/maven-central/**`、`p0-repo-out/**` 不动

### 5.3 Fork 归属声明

1. `README.md` Acknowledgements 章节（约 518-520 行）新增：
   `- [huarangmeng/latex](https://github.com/huarangmeng/latex) — Original project, author: huarangmeng. This repository is its maintained fork, maintainer: [zusrsoft](https://github.com/zusrsoft).`
2. `README_zh.md` 致谢章节（约 516 行起）新增：
   `- [huarangmeng/latex](https://github.com/huarangmeng/latex) —— 原始项目，作者：huarangmeng；本仓库为其维护版 fork，维护者：[zusrsoft](https://github.com/zusrsoft)。`
3. `LICENSE`：版权行双行（见第 4 节决策）
4. 两份 README 的 License 区块（README.md 约 533 行 / README_zh.md 约 531 行）同步双版权行
5. 三个模块 POM `description` 尾部统一追加 `Maintained fork of huarangmeng/latex.`（见 5.1）

### 5.4 发布手册同步

`MAVEN_CENTRAL_PUBLISH.md`：

- 4.2 节：`mavenPublishing` 配置模式描述改为"公共块在根 build.gradle.kts，模块仅 coordinates + name/description"
- 4.3 节：当前版本 `1.5.4 → 1.5.5`
- 7.4 节：依赖验证示例 `1.5.4 → 1.5.5`

AGENTS.md 无 POM 相关内容（已检索确认），不动。

## 6. 验证

1. `.\gradlew.bat :latex-renderer:publishToMavenLocal --no-configuration-cache`，检查 `%GRADLE_USER_HOME%\m2` 生成的各 publication POM：
   - `<developers>` 仅含 zusrsoft
   - `<url>` / `<scm>` 指向 zusrsoft/latex
   - `<description>` 含 `Maintained fork of huarangmeng/latex.`
2. 全仓 grep `huarangmeng` 白名单校验（排除 `docs/`、`p0-repo-out/` 后，仅允许出现于）：
   - `LICENSE` 版权行
   - 两份 README（致谢 + License 区块）
   - 三个模块 `build.gradle.kts`（POM description 尾注 `Maintained fork of huarangmeng/latex.`）
   - `MAVEN_CENTRAL_PUBLISH.md` 6.5 节历史事故记录
   - Kotlin 源文件头部版权注释（且所有 `*.kt` 命中必须匹配 `^.*Copyright \(c\) 2026 huarangmeng$` 模式）
3. Gradle 配置期全量通过（根脚本 subprojects 块影响所有子项目配置，需完整配置验证）

## 7. 风险与注意事项

- 根脚本 `subprojects` 配置块与 `org.gradle.configuration-cache=true` 兼容性：codehigh 同模式已验证可行；发布命令仍按手册加 `--no-configuration-cache`
- `-kt2.1.0` 别名版本由 CI 临时改 `VERSION` 生成，不受本次结构调整影响（其 POM 同样继承根公共块，developers 自动干净）
- 发布仍需人工触发（Release 流程），本设计只保证仓库内配置与文档就绪
