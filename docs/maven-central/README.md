# Maven Central File Count Audit

Projects:
- zusrsoft/latex
- zusrsoft/codehigh
- zusrsoft/diagram
- zusrsoft/Markdown（任务书未列出，但实际参与该 namespace 发布，已一并审计）

Namespace:
`io.github.zusrsoft`

Purpose:
分析 Maven Central publishing File Count 的精确来源，并评估安全、合理的优化空间。
**只读审计，未修改任何项目文件。**

Current Central usage（Usage Center，2026-09 快照，用户提供）:

```
File Count    ≈ 4,720 / 1,000   （月度口径，按三个月滚动平均评估）
Release Size  ≈ 69.66 MB / 80 MB
Release Count = 4 / 7
```

## 核心结论（一句话）

4,720 = **472 个 primary 文件 × 10**（本体 + .asc + 本体×4 校验和 + .asc×4 校验和），
来自 2026-09-06 同日发布的 4 个 Release（latex 1.5.4 / diagram 1.0.4 / codehighlight 1.1.2 / Markdown 1.5.2），
其中 4 个 renderer 类模块因 Compose resources 每个多 4 个 primary。

## 文档索引

| 文件 | 内容 |
| --- | --- |
| [unified-file-count-audit.md](unified-file-count-audit.md) | 联合审计主报告（核心） |
| [latex-audit.md](latex-audit.md) | latex 项目审计 |
| [codehigh-audit.md](codehigh-audit.md) | codehigh 项目审计 |
| [diagram-audit.md](diagram-audit.md) | diagram 项目审计 |
| [markdown-audit.md](markdown-audit.md) | Markdown 项目审计（任务书外补充） |
| [publication-matrix.md](publication-matrix.md) | 全量 Publication Matrix（12 模块 × 7 publication） |
| [file-count-executive-summary.md](file-count-executive-summary.md) | 一页结论 |
| [evidence/](evidence/) | 原始证据：Gradle 任务输出、.m2 清单、staging 上传包、控制台日志 |

## 交付物定位（原任务要求）

- **① 审计报告** → 本文档 + 下列各篇（docs/maven-central/，任务书指定目录）。
- **② 三个项目的 build.gradle.kts**（发布相关）→ 原位文件，路径与发布配置节选如下：

| 项目 | SDK 模块 build 文件 | 发布 DSL 关键行 |
| --- | --- | --- |
| latex | `latex-base/latex-parser/latex-renderer/build.gradle.kts` | `publishToMavenCentral(true)` + `signAllPublications()` + `coordinates("io.github.zusrsoft", ...)`（VERSION=1.5.4） |
| codehigh | `codehighlight-parser/codehighlight-render/build.gradle.kts` | 仅 `coordinates(...)`，无显式 central/sign（凭据自动激活；VERSION 本地 2.0.0 vs Central 1.1.2） |
| diagram | `diagram-core/-layout/-parser/-render/build.gradle.kts` | `publishToMavenCentral(true)` + `if (!hasProperty("signing.skip")) signAllPublications()`（VERSION=1.0.4） |

  各 build 文件全文见各项目仓库（此处不复制，避免与源码漂移）；发布结构在
  [publication-matrix.md](publication-matrix.md) 与各项目 audit 中逐项引用。

- **③ 项目 AI 的实际统计结果 / Gradle 实际输出** → `evidence/` 目录保存了全部真实命令输出
  （`gradlew tasks --all`、publishToMavenLocal 控制台、staging 401 实验、.m2 前后快照），
  索引见 [unified-file-count-audit.md](unified-file-count-audit.md) §24 Evidence / Commands。

## 审计方法（证据优先）

1. **远程取证**：直接枚举 repo1.maven.org 上 `io/github/zusrsoft/` 全部 84 个 artifact 目录
   及代表性 version 目录的完整文件清单（精确到字节数与时间戳）。
2. **本地复现**：`publishToMavenLocal`（真实签名链）+ 假凭据触发
   `publishAllPublicationsToMavenCentralRepository` 在上传步骤 401 失败，
   捕获 Gradle 实际生成/上传的 staging 目录内容（证明 .asc checksum 由 Gradle 生成）。
3. **官方口径**：引用 Sonatype《Reducing Publishing Usage》《Maven Central Publishing Limits》。
