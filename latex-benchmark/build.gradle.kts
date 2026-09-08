plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.allopen)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

// JMH 瑕佹眰 benchmark 绫婚潪 final锛宎llopen 鑷姩澶勭悊
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
    annotation("kotlinx.benchmark.State")
}

kotlin {
    jvmToolchain(21)
}

// 璁?benchmark 妯″潡鑳借闂?latex-renderer 鐨?internal API锛堜粎鐢ㄤ簬鎬ц兘鍩哄噯娴嬭瘯锛?
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    val rendererJar = project(":latex-renderer").tasks.named("jvmJar")
    dependsOn(rendererJar)
    val jarFile = rendererJar.map { (it as Jar).archiveFile.get().asFile.absolutePath }
    compilerOptions {
        freeCompilerArgs.addAll(jarFile.map { listOf("-Xfriend-paths=$it") })
    }
}

dependencies {
    implementation(project(":latex-parser"))
    implementation(project(":latex-renderer"))
    implementation(libs.kotlinx.benchmark.runtime)

    // Compose Desktop 渚濊禆锛堢敤浜?TextMeasurer, Density 绛夛級
    implementation(compose.desktop.currentOs)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.runtime)
}

benchmark {
    configurations {
        named("main") {
            warmups = 5
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "s"
            outputTimeUnit = "ms"
            mode = "avgt"
        }
    }
    targets {
        register("main")
    }
}
