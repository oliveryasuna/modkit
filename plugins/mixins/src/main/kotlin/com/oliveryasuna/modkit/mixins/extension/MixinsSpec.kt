package com.oliveryasuna.modkit.mixins.extension

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

/**
 * The `modkit.mixins { }` block. Register-focused: the user hand-authors each
 * `src/main/resources/<name>.mixins.json`, and this block provides the refmap
 * convention, publishes the registered config file names to the shared manifest
 * registry, and optionally lints `@Mixin` target references.
 */
public abstract class MixinsSpec {

    /** The refmap file name emitted for the mixin configs. */
    public abstract val refmap: Property<String>

    /** The registered mixin configs, keyed by name. */
    public abstract val configs: NamedDomainObjectContainer<MixinConfig>

    @get:Nested
    public abstract val lint: LintSpec

    /** Registers a mixin config named [name], backing `<name>.mixins.json`. */
    public fun register(name: String, action: Action<in MixinConfig> = Action {}) {
        configs.create(name, action)
    }

    public fun lint(action: Action<in LintSpec>) {
        action.execute(lint)
    }

}
