package com.oliveryasuna.modkit.loaders

import org.gradle.api.Project

private const val PARCHMENT_MAVEN_URL: String = "https://maven.parchmentmc.org"

internal fun Project.addParchmentRepository() {
    repositories.maven { repo -> repo.setUrl(PARCHMENT_MAVEN_URL) }
}
