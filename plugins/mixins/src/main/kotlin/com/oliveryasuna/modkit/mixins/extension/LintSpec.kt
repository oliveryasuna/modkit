package com.oliveryasuna.modkit.mixins.extension

import org.gradle.api.provider.Property

/**
 * The `mixins { lint { } }` block. Gates and configures the `@Mixin` lint.
 */
public abstract class LintSpec {

    /** Whether the lint runs at all. Off by default. */
    public abstract val enabled: Property<Boolean>

    /** Whether to fail when a `@Mixin` target class cannot be resolved. */
    public abstract val checkTargetsExist: Property<Boolean>

}
