package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention plugin applied by every plugin module
 * (`id("modkit.plugin-conventions")`).
 *
 * Wires up the Kotlin/Java toolchain, publishing coordinates, and a JUnit +
 * Gradle TestKit test setup so individual modules stay boilerplate-free.
 */
class PluginConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("java-gradle-plugin")
        pluginManager.apply("maven-publish")
        pluginManager.apply("com.gradle.plugin-publish")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()

        // ==================================================
        // Toolchain
        // ==================================================
        configure<KotlinJvmProjectExtension> {
            jvmToolchain(javaVersion)
            explicitApi()
        }

        // ==================================================
        // Publishing
        // ==================================================
        // Workspace-wide coordinates. Per-plugin id / displayName / description
        // / tags are set in each module's `gradlePlugin` block.
        configure<GradlePluginDevelopmentExtension> {
            website.set("https://github.com/oliveryasuna/modkit")
            vcsUrl.set("https://github.com/oliveryasuna/modkit.git")
        }

        // ==================================================
        // Testing
        // ==================================================
        // JUnit unit tests + a dedicated TestKit functional-test suite.
        val functionalTest = extensions.getByType<SourceSetContainer>().create("functionalTest")
        configure<GradlePluginDevelopmentExtension> {
            testSourceSets(functionalTest)
        }

        configurations[functionalTest.implementationConfigurationName]
            .extendsFrom(configurations.getByName("testImplementation"))
        configurations[functionalTest.runtimeOnlyConfigurationName]
            .extendsFrom(configurations.getByName("testRuntimeOnly"))

        dependencies {
            "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
            "testImplementation"(libs.findLibrary("junit-jupiter").get())
            "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
            // ProjectBuilder-based unit tests need the full Gradle API at test
            // runtime.
            "testImplementation"(gradleApi())

            "functionalTestImplementation"(gradleTestKit())
        }

        val functionalTestTask = tasks.register<Test>("functionalTest") {
            description = "Runs the Gradle TestKit functional tests."
            group = "verification"
            testClassesDirs = functionalTest.output.classesDirs
            classpath = functionalTest.runtimeClasspath
            useJUnitPlatform()
            shouldRunAfter(tasks.named("test"))
        }

        tasks.named<Test>("test") {
            useJUnitPlatform()
        }

        tasks.named("check") {
            dependsOn(functionalTestTask)
        }
    }

}
