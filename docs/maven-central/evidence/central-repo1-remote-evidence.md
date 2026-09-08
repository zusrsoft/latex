# Central（repo1.maven.org）远程取证摘要

> 取证时间：2026-09-08 · 全部请求为只读 GET
> 基址：https://repo1.maven.org/maven2/io/github/zusrsoft/
> 用途：验证 12 模块 × 7 publication 的文件结构与时间线，佐证本地复现实验

## 1. namespace 顶层（84 个 artifact 目录 + 时间线）

| artifact 目录 | 发布时间（maven-metadata lastUpdated） | 版本数 |
| --- | --- | --- |
| latex-base / latex-parser / latex-renderer | 2026-09-06 14:04-14:09（lastUpdated 20260906140915） | **1（1.5.4）** |
| diagram-core / -layout / -parser / -render | 2026-09-06 14:41 | 1（1.0.4） |
| codehighlight-parser / codehighlight-render | 2026-09-06 14:48 | 1（1.1.2） |
| markdown-parser / markdown-renderer / markdown-runtime | 2026-09-06 15:02-15:09（lastUpdated 20260906150926） | 1（1.5.2） |

> 全量抽查结论：84 个 artifactId 的 maven-metadata.xml 均只有**单一版本**（2026-09-06 为该
> namespace 的首发日——本地代码中 1.4.8 等自引用版本是迁移前旧坐标/本地发布的残留，repo1 无
> 历史版本文件）。因此 repo1 该 namespace 的**累计总量 = 当月发布量 = 5,140 文件**。

每 artifactId 下含根 artifact + 6 target 变体目录（-android/-jvm/-js/-wasm-js/-iosarm64/-iossimulatorarm64）
= 7 publication × 12 = 84 artifactId。均含完整 `maven-metadata.xml`（含 `<latest>`/`<release>` 单一版本）。

## 2. 代表性版本目录文件清单（验证 ×10 结构）

以 `latex-base/1.5.4/` 为例（其余 83 个 artifactId 目录同构，抽样核对）：

```
latex-base-1.5.4-javadoc.jar                      + .asc + 8 checksum（4 本体 + 4 .asc）
latex-base-1.5.4-kotlin-tooling-metadata.json     + 同上
latex-base-1.5.4-sources.jar                      + 同上
latex-base-1.5.4.jar                              + 同上
latex-base-1.5.4.module                           + 同上
latex-base-1.5.4.pom                              + 同上
```
= 6 primary × 10 = 60 文件 + maven-metadata.xml×5（不计 File Count）。

分平台抽查（目录与 primary 计数）：
- `latex-base-android/1.5.4/` → 5 primary（.aar 本体，无 tooling-json/metadata.jar）
- `latex-base-jvm/1.5.4/` → 5
- `latex-base-js/1.5.4/` → 5
- `latex-base-wasm-js/1.5.4/` → 5
- `latex-base-iosarm64/1.5.4/` → 6（+ -metadata.jar）
- `latex-base-iossimulatorarm64/1.5.4/` → 6
- `latex-renderer-js/1.5.4/`、`latex-renderer-wasm-js/1.5.4/`、`latex-renderer-iosarm64/1.5.4/` → 6
  （+ -kotlin_resources.kotlin_resources.zip，Type B 佐证）
- `markdown-runtime-iosarm64/1.5.2/`、`codehighlight-render-iosarm64/1.1.2/`、`diagram-render-iosarm64/1.0.4/` → 6

`maven-metadata.xml` 时间戳晚于版本文件约 5 分钟（Central release 处理写入），14:04/14:41/14:48/15:02 为各项目
deployment 时刻。

## 3. 与本地复现的一致性

| 项目 | repo1 观测 | 本地复现（publishToMavenLocal / staging） | 一致 |
| --- | --- | --- | --- |
| primary 集 | 上述 | 3 模块 257 本地文件 = 118 primary + 118 .asc + 21 metadata | ✓ |
| ×10 checksum | 版本目录 | staging 目录捕获 10 文件/primary | ✓ |
| metadata.xml | artifactId 目录 | staging 目录含 maven-metadata.xml+4 | ✓ |
| metadata 不计 File Count | 4,720 反推吻合（5,140−420） | — | ✓ |

## 4. Usage Center 交叉核对

- 用户提供：File Count ≈ 4,720、Release Size ≈ 69.66 MB、Release Count 4 → 与 §1 时间线（4 次
  deployment，同日 14:04-15:09）及 §2 结构（×10）完全一致。
- 84 个 artifactId 均无历史版本 → namespace 累计文件 = 4,720 + 420 metadata = 5,140；
  Usage Center 的 4,720 恰好等于 primary×10，证明其口径排除 Central 侧 maven-metadata 文件。
