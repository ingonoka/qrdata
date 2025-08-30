/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokkaGradlePlugin)
    alias(libs.plugins.asciiDocGradlePlugin)
}

repositories {
    mavenCentral()
    google()
    mavenLocal()
}

project.group = "com.ingonoka"
project.version = getVersionName()

android {

    namespace = "${project.group}.${project.name}"
    //noinspection GradleDependency,GradleDependency
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    version = rootProject.version

    compileOptions {
        targetCompatibility = JavaVersion.valueOf(libs.versions.android.target.compatibility.get())
        sourceCompatibility = JavaVersion.valueOf(libs.versions.android.source.compatibility.get())
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-android-optimize.txt")
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-android-optimize.txt")
        }
        debug {
            isMinifyEnabled = false
        }
    }
}

kotlin {

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs = listOf("-Xexpect-actual-classes")
                }
            }
        }
    }

    js {
        browser {
            testTask {
                useMocha {
                    timeout = "5s" // Increase to a suitable duration, e.g., "5s" for 5 seconds
                }
            }
            commonWebpackConfig {
                sourceMaps = false // Disable source maps
            }
            webpackTask {
                sourceMaps = false // Explicitly disable for webpack task
            }
        }
        binaries.executable()
    }

    jvm().compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                jvmTarget.set(JvmTarget.valueOf(libs.versions.jvm.target.get()))
            }
        }
    }

    // Native target isn't implemented due to the lack of a decent crypto MPP library
    // macosX64()

    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    sourceSets {

        all {
            languageSettings.languageVersion = "2.0"
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }

        commonMain {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.protobuf)

                implementation(libs.ingonoka.hexutils)
                implementation(libs.ingonoka.utilslib)

                implementation(libs.bignum)

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jvmMain {

            dependencies {
            }
        }

        androidMain {
            dependencies {
            }
        }
    }
}

publishing {

    publications.withType<MavenPublication>().forEach { p ->

        p.pom {
            // Properties from the root project gradle.properties
            name = providers.gradleProperty("com.ingonoka.pomName").get()
            description = providers.gradleProperty("com.ingonoka.pomDescription").get()
            licenses {
                license {
                    // Properties from the ~/.gradle/gradle.properties
                    name = providers.gradleProperty("com.ingonoka.pomLicenseName").get()
                    url = providers.gradleProperty("com.ingonoka.pomLicenseUrl").get()
                }
            }
            developers {
                developer {
                    // Properties from the ~/.gradle/gradle.properties
                    name = providers.gradleProperty("com.ingonoka.pomDeveloper")
                }
            }
        }
    }
}

dokka {
    moduleName.set(rootProject.name)
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.get().dir("dokka"))

        suppressInheritedMembers.set(true)
        failOnWarning.set(true)
    }

    dokkaSourceSets.commonMain { }

    pluginsConfiguration.html {
        customAssets.from("docs/logo-icon.svg")
        footerMessage.set("(c) Ingo Noka")
    }
}

/**
 * Get a version name of the form "v0.3-8-g9518e52", which is the tag
 * assigned to the commit (v0.3), the number of commits since the
 * commit the tag is assigned to and the hash of the latest commit
 */
fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "describe", "--tags") //, '--long'
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

tasks {
    "asciidoctor"(AsciidoctorTask::class) {
        baseDirFollowsSourceDir()
        sourceDir(file("doc"))
        setOutputDir(file("build/docs"))
        asciidoctorj {
            attributes(
                mapOf(
                    "source-highlighter" to "rouge",
                    "library-version" to project.version
                )
            )
        }
    }
}
