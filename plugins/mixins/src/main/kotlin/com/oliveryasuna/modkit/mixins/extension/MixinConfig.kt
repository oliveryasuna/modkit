package com.oliveryasuna.modkit.mixins.extension

import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * A single registered mixin config. Each corresponds to a hand-authored
 * `src/main/resources/<name>.mixins.json`; this type carries only the metadata
 * `mixins` needs to name it, pick its environment, and scope the lint.
 */
public abstract class MixinConfig @Inject constructor(
    public val name: String
) {

    /**
     * Loader environment the config applies to: `"*"`, `"client"`, or
     * `"server"`.
     */
    public abstract val environment: Property<String>

    /**
     * Base package holding the config's `@Mixin` classes, used to scope the
     * lint.
     */
    public abstract val pkg: Property<String>

}
