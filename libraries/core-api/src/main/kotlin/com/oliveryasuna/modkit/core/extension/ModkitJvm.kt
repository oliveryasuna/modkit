package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property

/**
 * JVM settings for the build.
 *
 * Usually, you don't touch this. The core plugin picks a [toolchain] from the
 * declared Minecraft versions (newer Minecraft wants a newer JDK). Set it only
 * to override that.
 */
public abstract class ModkitJvm {

    /** Java toolchain version, e.g., "21". */
    public abstract val toolchain: Property<Int>

}
