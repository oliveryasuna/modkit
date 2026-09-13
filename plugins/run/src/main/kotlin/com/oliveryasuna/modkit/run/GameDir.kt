package com.oliveryasuna.modkit.run

import org.gradle.api.file.Directory

/**
 * Renders a run's game directory relative to the project, for reporting.
 *
 * `gameDir` is a `DirectoryProperty`, so it resolves to an absolute directory.
 * The reports (`modkitRunInfo`, `modkitDoctor`) show the project-relative path
 * instead (e.g., `run/client`), which is what the user wrote and what stays
 * stable across machines.
 */
internal fun Directory.relativeToProject(projectDir: Directory): String =
    asFile.relativeTo(projectDir.asFile).path
