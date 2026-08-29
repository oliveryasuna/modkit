package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

/**
 * Convention for Gradle-plugin modules (`modkit.plugin-conventions`).
 *
 * Adds `java-gradle-plugin`, Plugin Portal + Maven publishing, and a TestKit
 * `functionalTest` suite on top of [BaseConventionsPlugin]. Per-plugin id,
 * display name, description, and tags are declared in each module's
 * `gradlePlugin` block.
 */
class PluginConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        apply<BaseConventionsPlugin>()
        pluginManager.apply("java-gradle-plugin")
        pluginManager.apply("maven-publish")
        pluginManager.apply("com.gradle.plugin-publish")

        val functionalTest = the<SourceSetContainer>().create("functionalTest")

        configure<GradlePluginDevelopmentExtension> {
            website.set(MODKIT_URL)
            vcsUrl.set("$MODKIT_URL.git")

            testSourceSets(functionalTest)
        }

        configurations[functionalTest.implementationConfigurationName]
            .extendsFrom(configurations["testImplementation"])
        configurations[functionalTest.runtimeOnlyConfigurationName]
            .extendsFrom(configurations["testRuntimeOnly"])

        dependencies {
            // ProjectBuilder-based unit tests need the Gradle API on the test
            // classpath.
            "testImplementation"(gradleApi())
            "functionalTestImplementation"(gradleTestKit())
        }

        val functionalTestTask = tasks.register<Test>("functionalTest") {
            group = "verification"
            description = "Runs the Gradle TestKit functional tests."
            testClassesDirs = functionalTest.output.classesDirs
            classpath = functionalTest.runtimeClasspath
            useJUnitPlatform()
            shouldRunAfter(tasks.named("test"))
        }

        tasks.named("check") {
            dependsOn(functionalTestTask)
        }
    }

}
