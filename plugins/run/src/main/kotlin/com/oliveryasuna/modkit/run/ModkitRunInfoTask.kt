package com.oliveryasuna.modkit.run

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Prints the resolved unified run configurations and probes the running JVM to
 * report whether an enhanced hot-swap runtime (JetBrains Runtime / DCEVM) is in
 * use. Purely diagnostic; it never injects any JVM argument.
 *
 * Inputs are snapshotted at configuration time so the task is configuration
 * cache safe; the JVM probe happens in the action.
 */
@DisableCachingByDefault(because = "Diagnostic task: prints run configuration and probes the running JVM.")
public abstract class ModkitRunInfoTask : DefaultTask() {

    /** Pre-formatted one-line summary of each declared run. */
    @get:Input
    public abstract val runSummaries: ListProperty<String>

    /** Snapshot of `hotswap.preferJetBrainsRuntime`. */
    @get:Input
    public abstract val preferJetBrainsRuntime: Property<Boolean>

    @TaskAction
    public fun report() {
        println("Modkit run configurations:")
        runSummaries.get().forEach { println("  $it") }

        val vendor = System.getProperty("java.vendor", "")
        val vmName = System.getProperty("java.vm.name", "")
        println("JVM: $vmName ($vendor)")
        println(hotswapReport(vendor, vmName, preferJetBrainsRuntime.getOrElse(false)))
    }

}
