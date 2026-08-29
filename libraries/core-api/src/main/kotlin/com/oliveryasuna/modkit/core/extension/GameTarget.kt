package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

/**
 * A Minecraft version to build for, plus which loaders to build on that
 * version.
 *
 * Targets sit in a container keyed by the version string, so the name is the
 * version: `minecraft("1.21.1")` makes a target named "1.21.1". Gradle creates
 * these and passes in the name, which is why the constructor is [Inject]ed.
 */
public abstract class GameTarget @Inject constructor(
    /** The Minecraft version, e.g., "1.21.1". Doubles as the container key. */
    public val name: String
) {

    /** Same value as [name]; reads better where the version is what matters. */
    public val minecraftVersion: String
        get() = name

    /** Loaders to build on this version. */
    public abstract val loaders: SetProperty<ModLoader>

    /**
     * Whether to build this target. Defaults to `true` (set by the `core`
     * plugin). Set it to `false` to keep a target declared but skip it for now.
     */
    public abstract val enabled: Property<Boolean>

}
