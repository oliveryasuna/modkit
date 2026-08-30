package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

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

            // The plugin under test runs in a forked Gradle daemon, not in this
            // test JVM. The JaCoCo agent that Gradle attaches here only sees
            // the test JVM, so on its own it records no plugin coverage. We
            // point the agent at our own exec file and pass its `-javaagent`
            // argument to the tests in a system property. FunctionalTestBase
            // then hands that argument to the daemon (see there), and the
            // daemon writes into the same exec file. One fork at a time so the
            // appends stay ordered.
            val jacocoAgent = extensions.getByType<JacocoTaskExtension>()
            val execFile = layout.buildDirectory.file("jacoco/functionalTest.exec")
            jacocoAgent.setDestinationFile(execFile.map { it.asFile })
            maxParallelForks = 1
            jvmArgumentProviders.add(
                CommandLineArgumentProvider {
                    // asJvmArg gives a destfile path relative to this project.
                    // The daemon has a different working directory (the TestKit
                    // project), so we swap in the absolute path. Otherwise the
                    // daemon writes somewhere under its own temp dir and we
                    // lose it.
                    val absolute = execFile.get().asFile.absolutePath
                    val arg = jacocoAgent.asJvmArg.replace(
                        Regex("destfile=[^,]+"), "destfile=$absolute"
                    )
                    listOf("-Dmodkit.jacocoAgentArg=$arg")
                }
            )
        }

        tasks.named("check") {
            dependsOn(functionalTestTask)
        }

        addCoverageFrom("functionalTest")
    }

}
