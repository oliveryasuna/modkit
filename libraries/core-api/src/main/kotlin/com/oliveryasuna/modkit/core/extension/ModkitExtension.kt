package com.oliveryasuna.modkit.core.extension

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

/**
 * The `modkit { }` block: one description o fthe mod that doesn't care about
 * loaders.
 *
 * It's just identity and intent. What the mod is called, who wrote it, which
 * Minecraft versions and loaders it targets. No loader-specific detail, no
 * logic. The sibling plugins read this and drive the actual per-loader tooling.
 *
 * It's a Gradle-managed type, so properties are lazy and the defaults get
 * applied by the core plugin. A data type shouldn't be deciding its own
 * defaults.
 */
public abstract class ModkitExtension {

    /** Mod id, e.g. `"my_mod"`. The one field with no reasonable default. */
    public abstract val modId: Property<String>

    /** Maven group. Defaults to the Gradle project's group. */
    public abstract val group: Property<String>

    /** Mod version. Defaults to the Gradle project's version. */
    public abstract val version: Property<String>

    /** Name shown in-game. Defaults to [modId]. */
    public abstract val displayName: Property<String>

    /** Short description. */
    public abstract val description: Property<String>

    /** License, e.g. "MIT". */
    public abstract val license: Property<String>

    /** Authors, in the order they should show up. */
    public abstract val authors: ListProperty<String>

    /** Homepage, source, and issue URLs. */
    @get:Nested
    public abstract val urls: ModkitUrls

    /** JVM/toolchain settings. */
    @get:Nested
    public abstract val jvm: ModkitJvm

    /** Minecraft versions to build for, keyed by version string. */
    public abstract val targets: NamedDomainObjectContainer<GameTarget>

    /** Configures [urls]. */
    public fun urls(action: Action<in ModkitUrls>): Unit = action.execute(urls)

    /** Configures [jvm]. */
    public fun jvm(action: Action<in ModkitJvm>): Unit = action.execute(jvm)

    /** Configures the [targets] container. */
    public fun targets(action: Action<in NamedDomainObjectContainer<GameTarget>>): Unit = action.execute(targets)

    /**
     * Adds or configures a target for a Minecraft [version].
     *
     * The nice way to reach [targets].
     * `minecraft("1.21.1") { loaders.add(FABRIC) }` creates the `"1.21.1"`
     * target and configures it.
     */
    public fun minecraft(version: String, action: Action<in GameTarget> = Action {}): GameTarget =
        targets.create(version, action)

}
