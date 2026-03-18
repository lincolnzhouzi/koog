import ai.koog.gradle.publish.maven.Publishing.publishToMaven

group = rootProject.group
version = rootProject.version

plugins {
    id("ai.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain(17)
    
    androidTarget {
        publishLibraryVariants("release")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":agents:agents-core"))
                api(project(":agents:agents-tools"))
                api(project(":prompt:prompt-executor:prompt-executor-model"))
                api(project(":prompt:prompt-llm"))
                api(project(":prompt:prompt-model"))
                api(project(":utils"))

                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.serialization.kotlinx.json)

                implementation(libs.oshai.kotlin.logging)
            }
            
            kotlin.srcDir("shared/api")
            kotlin.srcDir("shared/core/agent")
            kotlin.srcDir("shared/core/model")
            kotlin.srcDir("shared/data/cache")
            kotlin.srcDir("shared/data/database")
            kotlin.srcDir("shared/data/repository")
            kotlin.srcDir("shared/device")
            kotlin.srcDir("shared/performance")
            kotlin.srcDir("shared/profile")
            kotlin.srcDir("shared/security")
        }

        val commonTest by getting {
            dependencies {
                implementation(project(":agents:agents-test"))
                implementation(project(":test-utils"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
            
            kotlin.srcDir("android/src/main/kotlin")
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
    }

    explicitApi()
}

publishToMaven()
