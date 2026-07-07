package com.oliveryasuna.modkit.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

/**
 * Convention applied by every shared-library module
 * (`id("modkit.library-conventions")`).
 *
 * A plain Kotlin/JVM library — no `java-gradle-plugin`, no Plugin Portal. Adds
 * `java-library` (for the `api` configuration) and a Maven publication so the
 * library ships as its own artifact that plugins depend on. Toolchain and
 * unit-test setup come from [BaseConventionsPlugin].
 */
class LibraryConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        // 1. Apply plugins.
        //

        plugins.apply(BaseConventionsPlugin::class.java)
        pluginManager.apply("java-library")
        pluginManager.apply("maven-publish")

        // 2. Configure the Java plugin.
        //

        configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        // 3. Configure publication.
        //

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }
        }
    }
}
