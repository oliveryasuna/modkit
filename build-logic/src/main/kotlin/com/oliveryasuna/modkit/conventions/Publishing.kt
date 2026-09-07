package com.oliveryasuna.modkit.conventions

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.signing.SigningExtension

internal const val MODKIT_URL = "https://github.com/oliveryasuna/modkit"

/**
 * `true` when any signing configuration is present in Gradle properties:
 * in-memory key, keyring, or gpg-agent.
 */
internal val Project.hasSigningKey: Boolean
    get() = listOf("signingInMemoryKey", "signing.keyId", "signing.gnupg.keyName")
        .any { providers.gradleProperty(it).isPresent }

/**
 * Switches the `signing` plugin to the gpg command-line signatory when
 * `signing.gnupg.keyName` is set. Call after the signing plugin is applied.
 */
internal fun Project.useGpgCmdIfConfigured() {
    if(providers.gradleProperty("signing.gnupg.keyName").isPresent) {
        configure<SigningExtension> { useGpgCmd() }
    }
}

/**
 * License, developer, and SCM blocks shared by every published Modkit artifact.
 */
internal fun MavenPom.modkitMetadata(project: Project) {
    url.set(MODKIT_URL)

    licenses {
        license {
            name.set(project.providers.gradleProperty("modkit.pom.licenseName").getOrElse("Apache-2.0"))
            url.set(
                project.providers.gradleProperty("modkit.pom.licenseUrl").getOrElse("$MODKIT_URL/blob/main/LICENSE")
            )
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
        url.set(MODKIT_URL)
        connection.set("scm:git:$MODKIT_URL.git")
        developerConnection.set("scm:git:ssh://git@github.com/oliveryasuna/modkit.git")
    }
}
