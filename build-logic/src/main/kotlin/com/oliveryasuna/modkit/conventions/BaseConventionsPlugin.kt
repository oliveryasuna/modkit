package com.oliveryasuna.modkit.conventions

import com.diffplug.gradle.spotless.SpotlessExtension
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
 * Shared base for every JVM module: Kotlin/Java toolchain, Spotless, bytecode
 * target, and JUnit setup. Applied via `modkit.library-conventions` and
 * `modkit.plugin-conventions`, not directly.
 */
class BaseConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("com.diffplug.spotless")

        val ideaBinaryPath = findProperty("modkit.ideaBinaryPath")?.toString()
        val codeStylePath = rootProject.file(".idea/codeStyles/Project.xml").absolutePath
        configure<SpotlessExtension> {
            // Keep Spotless out of the normal build graph. The `idea()` step
            // shells out to IntelliJ, which the configuration cache forbids, so
            // wiring `spotlessCheck` into `check` would break every cached
            // build. Format on demand instead:
            //   ./gradlew spotlessApply --no-configuration-cache
            isEnforceCheck = false

            format("idea") {
                target("src/**/*.kt")
                val idea = idea()
                idea.codeStyleSettingsPath(codeStylePath)
                idea.withDefaults(false)
                ideaBinaryPath?.let(idea::binaryPath)
            }
        }

        val toolchainVersion = libs.version("java").toInt()
        // Compile on `java`, emit `java-target` bytecode so artifacts still
        // load on the oldest Gradle JVM consumers run. Modules that wrap loader
        // tooling (e.g., `:plugins:loaders`) override `modkit.bytecodeTarget`,
        // since Loom and ModDevGradle require Java 21.
        val bytecodeTarget = findProperty("modkit.bytecodeTarget")?.toString()?.toInt()
            ?: libs.version("javaTarget").toInt()

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
            "testRuntimeOnly"(libs.library("junit-platformLauncher").get())
        }

        tasks.named<Test>("test") {
            useJUnitPlatform()
        }

        applyCoverage()
    }

}
