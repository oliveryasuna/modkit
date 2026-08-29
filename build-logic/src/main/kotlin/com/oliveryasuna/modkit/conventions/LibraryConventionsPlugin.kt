package com.oliveryasuna.modkit.conventions

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Convention for shared-library modules (`modkit.library-conventions`).
 *
 * Plain Kotlin/JVM library published to Maven Central through the Vanniktech
 * plugin. Plugins on the Plugin Portal depend on these transitively, so they
 * have to resolve from Central. Toolchain and tests come from
 * [BaseConventionsPlugin].
 *
 * Credentials and signing keys are read from `~/.gradle/gradle.properties`.
 * Signing is only enabled when a key is configured so `publicToMavenLocal`
 * works without one.
 */
class LibraryConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        apply<BaseConventionsPlugin>()
        pluginManager.apply("java-library")
        // Applies `maven-publish` and registers sources/javadoc jars itself.
        pluginManager.apply("com.vanniktech.maven.publish")

        configure<MavenPublishBaseExtension> {
            publishToMavenCentral()

            if(hasSigningKey) {
                signAllPublications()
            }

            pom {
                name.set("Modkit :: ${project.name}")
                description.set("Modkit shared library '${project.name}', a building block for the Modkit Gradle plugins.")
                modkitMetadata(project)
            }
        }

        if(hasSigningKey) {
            useGpgCmdIfConfigured()
        }
    }

}
