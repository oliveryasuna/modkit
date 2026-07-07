package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Base convention shared by every JVM module in the workspace — plugin modules
 * and library modules alike. Owns the Kotlin/Java toolchain and JUnit unit-test
 * setup so the specialized conventions only add what's unique to their type.
 *
 * Not applied directly; layered in by `modkit.plugin-conventions` and
 * `modkit.library-conventions`.
 */
class BaseConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        // 1. Apply plugins.
        //

        pluginManager.apply("org.jetbrains.kotlin.jvm")

        // 2. Read libs.
        //

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()
        val targetVersion = libs.findVersion("java-target").get().requiredVersion

        // 3. Configure the toolchain and bytecode target.
        //
        // Build with `java` but emit `java-target` bytecode so the published
        // artifacts load on the oldest Gradle JVM consumers use.
        // -Xjdk-release` also pins the JDK API level so 21-only APIs can't leak
        // in.
        //

        configure<KotlinJvmProjectExtension> {
            jvmToolchain(javaVersion)
            explicitApi()
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(targetVersion))
                freeCompilerArgs.add("-Xjdk-release=$targetVersion")
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(targetVersion.toInt())
        }

        // 4. Testing.
        //

        dependencies {
            "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
            "testImplementation"(libs.findLibrary("junit-jupiter").get())
            "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
        }

        tasks.named<Test>("test") {
            useJUnitPlatform()
        }
    }
}
