# POM 开发者元数据统一为 zusrsoft — 设计文档

日期：2026-09-08
状态：已获用户批准（含本轮补充决策）
范围：仅本仓库（latex）；兄弟项目（codehigh / diagram / Markdown）不在范围内

## 背景

Maven Central 上 `io.github.zusrsoft:latex-base / latex-parser / latex-renderer:1.5.4` 的公开 POM 中，developers 块仍为 `huarangmeng`（id/url 均指向旧账号），与坐标 `io.github.zusrsoft`、POM url/scm（均已指向 `zusrsoft/latex`）不一致，造成维护身份混淆。

历史：2026-09 仓库从坐标 `io.github.huarangmeng` 全面切换到 `io.github.zusrsoft`（见 `MAVEN_CENTRAL_PUBLISH.md` 6.5 节），但 developers 块被遗漏。zusrsoft 与 huarangmeng 为同一人换账号；旧坐标当年发布被拒，从未成功发布。

## 本轮决策记录

1. POM developers 完全替换为 zusrsoft（不保留双条目）
2. README 致谢章节新增一行署名：作者 huarangmeng · 维护者 zusrsoft（不采用 fork 叙事，因实为同一人换账号）
3. LICENSE 与两个 README 页脚共 3 处版权行一并改为 zusrsoft
4. `VERSION` 本次一并 bump：1.5.4 → 1.5.5（修改自下个发布起公开生效）
5. 实现方式：逐文件直接修改（不抽取 convention plugin）

## 改动清单（8 处编辑）

| # | 文件 | 位置 | 改动 |
|---|------|------|------|
| 1 | `latex-base/build.gradle.kts` | developers 块（91-96 行） | `id=zusrsoft`、`name` 保持 "Kotlin Multiplatform Specialist"、`url=https://github.com/zusrsoft/` |
| 2 | `latex-parser/build.gradle.kts` | developers 块（100-105 行） | 同上 |
| 3 | `latex-renderer/build.gradle.kts` | developers 块（116-121 行） | 同上 |
| 4 | `LICENSE` | 第 3 行 | `Copyright (c) 2026 zusrsoft` |
| 5 | `README.md` | 第 533 行（License 代码块内页脚） | `Copyright (c) 2026 zusrsoft` |
| 6 | `README_zh.md` | 第 531 行（同上） | `Copyright (c) 2026 zusrsoft` |
| 7 | `README.md` | Acknowledgements 章节（518 行起） | 新增一行：Author: huarangmeng · Maintainer: zusrsoft（带链接） |
| 8 | `README_zh.md` | 致谢章节（516 行起） | 新增一行：作者：huarangmeng · 维护者：zusrsoft（带链接） |
| 9 | `gradle.properties` | 第 14 行 | `VERSION=1.5.5` |

注：developers 的 `name` 保持 "Kotlin Multiplatform Specialist"，与兄弟项目 Markdown 仓库现有模式一字不差。

## 明确不改

- 源文件头部 `Copyright (c) 2026 huarangmeng` 声明（约上百处，用户选择保留）
- `p0-repo-out/` 历史发布快照与 `docs/maven-central/` 审计证据（历史记录，不应修改）
- POM 坐标、url、scm、licenses（已是 zusrsoft/正确值，无需改动）
- `MAVEN_CENTRAL_PUBLISH.md` 等文档中提及 huarangmeng 的历史叙述（属历史记录）

## 验证方式

1. 运行 POM 生成任务（如 `generatePomFileFor*`），检查产物中 `<developers>` 为 `zusrsoft`；三模块各验一次
2. 改动范围内 grep 确认：3 个 build.gradle.kts、LICENSE、2 个 README 中 huarangmeng 仅剩致谢署名一处（刻意保留）
3. `gradle.properties` 版本号核对为 1.5.5
4. 纯构建元数据/文档变更，无运行时影响；配置缓存因脚本改动自动失效重算

## 错误处理

纯静态配置变更，无运行时错误路径。若本地签名配置缺失导致发布类任务失败，改用 `generatePomFileFor*` 任务仅生成 POM 校验。

## 实施方案选择

用户选定方案 A（逐文件最小修改）。方案 B（收敛公共 POM 配置到根构建脚本/convention plugin）、方案 C（gradle.properties 参数化）被否决，理由：仓库工作风格偏好小而连贯的编辑，避免投机性重构。
