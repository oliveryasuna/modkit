package com.oliveryasuna.modkit.run

/** Configures one loader's run container from the unified `run` block. */
internal sealed interface RunBackend {

    fun configure(ctx: RunContext)

}
