package com.oliveryasuna.modkit.conventions

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Applies JaCoCo and adds a `coverageReport` task. The report covers the
 * module's `main` sources and combines the execution data from every test
 * suite.
 *
 * Add a suite with [addCoverageFrom]. Every module has a `test` suite, so that
 * one is wired up here. Plugin modules also have `functionalTest`, which
 * [PluginConventionsPlugin] adds.
 */
internal fun Project.applyCoverage() {
    pluginManager.apply("jacoco")

    configure<JacocoPluginExtension> {
        toolVersion = libs.version("jacoco")
    }

    val main = extensions.getByType<SourceSetContainer>()["main"]

    val report = tasks.register<JacocoReport>("coverageReport") {
        group = "verification"
        description = "Merges JaCoCo coverage from every test suite into one report."

        // Source and class dirs are the same no matter which suite ran, so set
        // them once here. Suites only contribute execution data.
        sourceDirectories.from(main.allSource.srcDirs)
        classDirectories.from(main.output)

        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
        }
    }

    tasks.named("check") {
        dependsOn(report)
    }

    addCoverageFrom("test")
}

/**
 * Adds a test task's execution data to `coverageReport`.
 *
 * We pass the task to the `executionData(Task)` overload, which picks up the
 * task's JaCoCo destination file. It handles a missing file, so the report
 * still works if the suite never ran.
 */
internal fun Project.addCoverageFrom(testTaskName: String) {
    val testTask = tasks.named<Test>(testTaskName)

    tasks.named<JacocoReport>("coverageReport") {
        executionData(testTask.get())
        dependsOn(testTask)
    }
}
