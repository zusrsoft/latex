import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.allopen) apply false
    alias(libs.plugins.kotlinx.benchmark) apply false
    alias(libs.plugins.maven.publish) apply false
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(true)
            signAllPublications()
            pom {
                inceptionYear.set("2026")
                url.set("https://github.com/zusrsoft/latex")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
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
