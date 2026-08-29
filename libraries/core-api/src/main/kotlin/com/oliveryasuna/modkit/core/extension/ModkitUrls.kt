package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property

/**
 * Public URLs for the mod. They end up in the generated manifests.
 *
 * All optional. An unset one just doesn't get written.
 */
public abstract class ModkitUrls {

    /** Homepage or landing page. */
    public abstract val homepage: Property<String>

    /** Source repository. */
    public abstract val source: Property<String>

    /** Issue tracker. */
    public abstract val issues: Property<String>

}
