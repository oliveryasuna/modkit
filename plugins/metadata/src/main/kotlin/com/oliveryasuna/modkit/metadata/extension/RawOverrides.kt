package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.MapProperty

public abstract class RawOverrides {

    public abstract val raw: MapProperty<String, Any>

}
