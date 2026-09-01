package com.oliveryasuna.modkit.core.diagnostics

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty

/**
 * Where each plugin adds its bit to the `modkitDoctor` health report.
 *
 * Same trick as the manifest registry, but for diagnostics. Every plugin drops
 * a titled [section][sections] (and any [problems]), and the doctor task
 * stitches them together. No plugin has to know about the others. One instance
 * per project, handed out by `plugin-support`.
 *
 * The values are providers, so the doctor reads them at execution time, once
 * everything has actually been configured.
 */
public abstract class ModkitDiagnostics {

    /**
     * Report sections by title ("Model", "Loader", "Runs", ...), each one a
     * list of ready-to-print lines. Order is kept, so whoever contributes first
     * prints first.
     */
    public abstract val sections: MapProperty<String, List<String>>

    /**
     * Warnings for the report's "Problems" heading, like no targets declared or
     * no active loader. The doctor prints these but never fails the build over
     * them.
     */
    public abstract val problems: ListProperty<String>

}
