package com.oliveryasuna.modkit.loaders

import org.gradle.api.Project

/**
 * Registers ParchmentMC's Maven (where parchment mappings data lives) on the
 * project. Loom resolves its mappings from the *project* repositories rather
 * than settings-level ones, so declaring it in consumer settings is not enough;
 * loaders adds it so users need not know parchment lives on a dedicated Maven.
 * Harmless when parchment is unused — an unqueried repository costs nothing.
 */
internal fun Project.addParchmentRepository() {
    repositories.maven { repo -> repo.setUrl("https://maven.parchmentmc.org") }
}
