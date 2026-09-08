# File Count 审计——一页结论

> zusrsoft 三（+1）项目 · Maven Central Publishing Usage 审计 · 2026-09-08

## 一句话

**4,720 已被精确解释（100%），非任何"意外或错误发布"导致；它 = 472 个 primary 文件 × 10
（本体 + .asc + 8 个 checksum），是 4 个 KMP 多平台项目在同一天各发布一次的固有结构产物。**

## 数字（全部精确到文件数）

```
4,720 = 472 × 10
      = latex 1,180(25%) + Markdown 1,180(25%) + diagram 1,560(33%) + codehigh 800(17%)
      = 8 个 Type A 模块 × 380 + 4 个 Type B 模块(带 Compose resources) × 420
文件构成：primary 472(10%) + .asc 472(10%) + 本体checksum 1,888(40%) + .asc checksum 1,888(40%)
```

- 每模块 = 6 KMP target（android/jvm/js/wasmJs/iosArm64/iosSim）+ root = 7 publications → 7 artifactId
- 共 12 SDK 模块、84 artifactId；preview/benchmark/demo **均未发布**（Gradle 实测 0 publish task）
- repo1 实际存储 5,140 文件（+84 个 artifactId 的 metadata.xml×5，Central 不计 File Count）

## 问题定性（答案)

- 有没有 ASC checksum 膨胀？**有**：1,888 个 .asc 的 checksum（40%），由 **Gradle
  maven-publish/signing 在 staging 阶段自动生成**（本审计用假凭据 401 实验捕获目录为证），
  Sonatype 官方文档明示"通常不需要、会显著增加 File Count"，**但无官方关闭开关**。
- 有没有多余 publication？**无**。 有没有误发布 preview 等？**无**（源码与 Central 双向验证）。
- 需要删 sources/javadoc/.module？**不需要也不能**——Central 与 KMP 解析的硬要求。
- 有没有被忽略的第四项目？**有**：Markdown（25%），任务书未列出。

## 结论（Q8 对应）

- A：本 namespace 合法且结构已最优（该删的都没发）；
- B：**.asc checksum（−40%）是唯一大头优化点**，但需先在 staging 环境验证可行机制；
  次要：停用 latex `-kt2.1.0` 别名双发 Job（避免未来每次 release ×2）。
- C：极端情况（合并模块/删 target）收益高但破坏坐标/平台覆盖，不推荐。

## 推荐行动（本月）

1. **申请 OSS publishing limit adjustment**（central-support@sonatype.com）——理由：月度上限
   1,000 vs 单次 latex release 即 1,180；本 namespace 全部为标准 KMP 产物、无违规项，
   属 Sonatype 政策支持的正常开源用法；官方按滚动三月平均评估，4 项目错峰亦难达标。
   附本审计 + Usage Center 截图。
2. **停用/条件化 latex 别名发布 Job**（防 ×2）。
3. **择机验证 .asc checksum 抑制机制**（实验室做法已写于联合报告 §24，先 staging 验证再启用）；
   预期 -40% → 每月若只有 2 个项目发版 ≈ 1,888-2,832（仍需配合 1 调整）。
4. 各项目按需正常发版即可（limit adjustment 前避免同月多发）。

## 附：各 Release Size（上传字节，非文件数口径)

| Release | Size | 依据 |
| --- | --- | --- |
| latex 1.5.4 | 15.04 MB | 本地 .m2 实测（精确） |
| Markdown 1.5.2 | 16.10 MB | 本地 .m2 实测（精确） |
| codehigh 1.1.2 / diagram 1.0.4 | 合计 38.5 MB | 69.66 − 上述两值（残差，精确拆分需各项目本地产物，不影响文件数结论） |
