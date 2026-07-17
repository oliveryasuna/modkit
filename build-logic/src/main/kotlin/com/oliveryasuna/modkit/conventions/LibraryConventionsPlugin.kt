package com.oliveryasuna.modkit.conventions

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.signing.SigningExtension

/**
 * Convention applied by every shared-library module
 * (`id("modkit.library-conventions")`).
 *
 * A plain Kotlin/JVM library — no `java-gradle-plugin`, no Plugin Portal. Adds
 * `java-library` (for the `api` configuration) and publishes the library as its
 * own artifact to **Maven Central** (Central Portal) via the Vanniktech
 * maven-publish plugin: full POM, sources + javadoc jars, and (when a signing
 * key is present) GPG-signed artifacts. Plugins on the Gradle Plugin Portal
 * reference these libraries as transitive dependencies, so they must resolve
 * from Central. Toolchain and unit-test setup come from
 * [BaseConventionsPlugin].
 *
 * Credentials/signing are read from `~/.gradle/gradle.properties`
 * (`mavenCentralUsername`/`mavenCentralPassword`, plus `signingInMemoryKey`...
 * or `signing.keyId`...). Signing is applied only when a key is configured, so
 * `publishToMavenLocal` still works in environments without one.
 */
class LibraryConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        // 1. Apply plugins. Vanniktech applies `maven-publish` itself and
        //    creates the sources/javadoc jars, so we do not configure those
        //    here (that would double-register them).
        //

        plugins.apply(BaseConventionsPlugin::class.java)
        pluginManager.apply("java-library")
        pluginManager.apply("com.vanniktech.maven.publish")

        // 2. Configure publishing to Maven Central.
        //

        configure<MavenPublishBaseExtension> {
            // Platform (java-library → javadoc + sources jars) is
            // auto-detected. publishToMavenCentral() targets the Central Portal
            // by default.
            publishToMavenCentral()

            // Sign only when a key is configured, so credential-less
            // `publishToMavenLocal` still works.
            if(hasSigningKey()) {
                signAllPublications()
            }

            pom {
                name.set("Modkit :: ${target.name}")
                description.set("Modkit shared library '${target.name}' — a building block for the Modkit Gradle plugins.")
                url.set(PROJECT_URL)
                licenses {
                    license {
                        name.set(providers.gradleProperty("modkit.pom.licenseName").getOrElse("All Rights Reserved"))
                        url.set(providers.gradleProperty("modkit.pom.licenseUrl").getOrElse("$PROJECT_URL/blob/main/LICENSE"))
                    }
                }
                developers {
                    developer {
                        id.set("oliveryasuna")
                        name.set("Oliver Yasuna")
                        url.set("https://github.com/oliveryasuna")
                    }
                }
                scm {
                    url.set(PROJECT_URL)
                    connection.set("scm:git:$PROJECT_URL.git")
                    developerConnection.set("scm:git:ssh://git@github.com/oliveryasuna/modkit.git")
                }
            }
        }

        // The standard `signing` plugin (applied by `signAllPublications()`)
        // defaults to the in-memory/keyring signatory; switch it to the GnuPG
        // command-line signatory when `signing.gnupg.*` is configured.
        if(hasSigningKey() && providers.gradleProperty("signing.gnupg.keyName").isPresent) {
            configure<SigningExtension> { useGpgCmd() }
        }
    }

    private fun Project.hasSigningKey(): Boolean =
        providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.gradleProperty("signing.keyId").isPresent ||
        providers.gradleProperty("signing.gnupg.keyName").isPresent

    private companion object {

        private const val PROJECT_URL: String = "https://github.com/oliveryasuna/modkit"

    }
}
