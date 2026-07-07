package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

/**
 * Convention applied by every Gradle-plugin module
 * (`id("modkit.plugin-conventions")`).
 *
 * Layers plugin-development concerns — `java-gradle-plugin`, Plugin Portal +
 * Maven publishing, and a Gradle TestKit functional-test suite — on top of
 * [BaseConventionsPlugin]'s toolchain and unit-test setup.
 */
class PluginConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        // 1. Apply plugins.
        //

        plugins.apply(BaseConventionsPlugin::class.java)
        pluginManager.apply("java-gradle-plugin")
        pluginManager.apply("maven-publish")
        pluginManager.apply("com.gradle.plugin-publish")

        // 2. Configure publication.
        //
        // Workspace-wide coordinates. Per-plugin id / displayName /
        // description  / tags are set in each module's `gradlePlugin` block.
        //

        configure<GradlePluginDevelopmentExtension> {
            website.set("https://github.com/oliveryasuna/modkit")
            vcsUrl.set("https://github.com/oliveryasuna/modkit.git")
        }

        // 3. Functional testing (Gradle TestKit).
        //

        val functionalTest = extensions.getByType<SourceSetContainer>().create("functionalTest")
        configure<GradlePluginDevelopmentExtension> {
            testSourceSets(functionalTest)
        }

        configurations[functionalTest.implementationConfigurationName]
            .extendsFrom(configurations.getByName("testImplementation"))
        configurations[functionalTest.runtimeOnlyConfigurationName]
            .extendsFrom(configurations.getByName("testRuntimeOnly"))

        dependencies {
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

        tasks.named("check") {
            dependsOn(functionalTestTask)
        }
    }
}
