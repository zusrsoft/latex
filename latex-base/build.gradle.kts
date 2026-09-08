import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.hrm.latex.base"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {}

        // 鍙戝竷娑堣垂鏂?R8 / ProGuard 瑙勫垯锛岄殢 AAR 涓€璧峰垎鍙戠粰涓嬫父浣跨敤鏂?
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file(file("consumer-rules.pro"))
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "LatexBase"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // 鍩虹灞備繚鎸佹渶灏忎緷璧?
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    coordinates("io.github.zusrsoft", "latex-base", rootProject.property("VERSION").toString())

    pom {
        name.set("Kotlin Multiplatform LaTeX Rendering Engine")
        description.set(""""
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
