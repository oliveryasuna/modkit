package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Shared base for every JVM module: Kotlin/Java toolchain, bytecode target, and
 * JUnit setup. Applied via `modkit.library-conventions` and
 * `modkit.plugin-conventions`, not directly.
 */
class BaseConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        val toolchainVersion = libs.version("java").toInt()

        // Compile on `java`, emit `java-target` bytecode so artifacts still
        // load on the oldest Gradle JVM consumers run. Modules that wrap loader
        // tooling (e.g., `:plugins:loaders`) override `modkit.bytecodeTarget`,
        // since Loom and ModDevGradle require Java 21.
        val bytecodeTarget = findProperty("modkit.bytecodeTarget")?.toString()?.toInt()
                             ?: libs.version("java-target").toInt()

        configure<KotlinJvmProjectExtension> {
            jvmToolchain(toolchainVersion)
            explicitApi()
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(bytecodeTarget.toString()))
                // Also pin the JDK API level, not just the class-file version.
                freeCompilerArgs.add("-Xjdk-release=${bytecodeTarget}")
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(bytecodeTarget)
        }

        dependencies {
            "testImplementation"(platform(libs.library("junit-bom").get()))
            "testImplementation"(libs.library("junit-jupiter").get())
            "testRuntimeOnly"(libs.library("junit-platform-launcher").get())
        }

        tasks.named<Test>("test") {
            useJUnitPlatform()
        }
    }

}
