import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.hrm.latex.parser"
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
            baseName = "LatexParser"
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
            api(projects.latexBase)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

mavenPublishing {
    coordinates("io.github.zusrsoft", "latex-parser", rootProject.property("VERSION").toString())

    pom {
        name.set("Kotlin Multiplatform LaTeX Parser")
        description.set(""""
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
