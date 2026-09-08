# Publication Matrix：io.github.zusrsoft（2026-09-06 全量发布）

> 口径：Central（repo1）实测每个 artifactId 的版本目录文件 = primary ×10。
> "Primary" = Central 实际计数中非 checksum 的文件（本体/签名）的 1/2；每 primary 实际 = 本体+asc+8 checksum。
> 下表给出每模块的 primary 构成（root 无后缀 = `kotlinMultiplatform` publication 的根 artifactId）。

## 模块 → Artifact → Primary 计数

| 模块 | Root artifact | 7 artifactId | Primary | ×10 = Central 文件 |
| --- | --- | --- | --- | --- |
| latex-base | latex-base | base, -android, -jvm, -js, -wasm-js, -iosarm64, -iossimulatorarm64 | 38 | 380 |
| latex-parser | latex-parser | 同上 | 38 | 380 |
| latex-renderer | latex-renderer | 同上 | 42 | 420 |
| **latex 小计** | | 21 artifactId | **118** | **1,180** |
| markdown-parser | markdown-parser | 同上 | 38 | 380 |
| markdown-renderer | markdown-renderer | 同上 | 42 | 420 |
| markdown-runtime | markdown-runtime | 同上 | 38 | 380 |
| **Markdown 小计** | | 21 artifactId | **118** | **1,180** |
| diagram-core | diagram-core | 同上 | 38 | 380 |
| diagram-layout | diagram-layout | 同上 | 38 | 380 |
| diagram-parser | diagram-parser | 同上 | 38 | 380 |
| diagram-render | diagram-render | 同上 | 42 | 420 |
| **diagram 小计** | | 28 artifactId | **156** | **1,560** |
| codehighlight-parser | codehighlight-parser | 同上 | 38 | 380 |
| codehighlight-render | codehighlight-render | 同上 | 42 | 420 |
| **codehigh 小计** | | 14 artifactId | **80** | **800** |
| **12 模块合计** | | **84 artifactId** | **472** | **4,720** |

（maven-metadata.xml 及其 checksum 每 artifactId 5 个 ×84 = 420，不计入 Central File Count）

## Type A 模块 primary 拆解（38，无 Compose resources）

| 类型 | Root | android | jvm | js | wasm-js | iosarm64 | iossimulatorarm64 | Σ |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 主产物 jar/aar/klib | jar | aar | jar | klib | klib | klib | klib | 7 |
| -sources.jar | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 7 |
| -javadoc.jar（空桩 261B） | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 7 |
| -metadata.jar | — | — | — | — | — | ✓ | ✓ | 2 |
| -kotlin-tooling-metadata.json | ✓ | — | — | — | — | — | — | 1 |
| .module | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 7 |
| .pom | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 7 |
| **Σ** | **6** | **5** | **5** | **5** | **5** | **6** | **6** | **38** |

## Type B 模块 primary 拆解（42 = 38 + 4，renderer 类模块）

| 类型 | Root | android | jvm | js | wasm-js | iosarm64 | iossimulatorarm64 | Σ |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Type A 全部 | 6 | 5 | 5 | 5 | 5 | 6 | 6 | 38 |
| -kotlin_resources.kotlin_resources.zip | — | — | — | +1 | +1 | +1 | +1 | 4 |
| **Σ** | **6** | **5** | **5** | **6** | **6** | **7** | **7** | **42** |

Type B 模块（4 个）：latex-renderer、markdown-renderer、diagram-render、codehighlight-render
Type A 模块（8 个）：latex-base、latex-parser、markdown-parser、markdown-runtime、
diagram-core、diagram-layout、diagram-parser、codehighlight-parser

## 单 artifact 的 Central 版本目录文件清单（repo1 实测，latex-base 1.5.4 为例）

```
latex-base-1.5.4.jar(.asc)(.asc.md5/.asc.sha1/.asc.sha256/.asc.sha512)
latex-base-1.5.4-sources.jar (+9)
latex-base-1.5.4-javadoc.jar  (+9)
latex-base-1.5.4-kotlin-tooling-metadata.json (+9)
latex-base-1.5.4.module (+9)
latex-base-1.5.4.pom (+9)
= 6 primary × 10 = 60 文件；另有 maven-metadata.xml(+4)（不计入 File Count）
```
