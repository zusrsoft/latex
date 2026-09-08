import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
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

        // 发布消费方 R8 / ProGuard 规则，随 AAR 一起分发给下游使用方
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
            // 基础层保持最小依赖
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
