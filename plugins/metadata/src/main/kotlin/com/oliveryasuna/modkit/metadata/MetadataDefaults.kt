package com.oliveryasuna.modkit.metadata

import com.oliveryasuna.modkit.plugin.PluginFeature

internal object MetadataDefaults : PluginFeature<MetadataContext> {

    private const val BOTH_SIDES: String = "*"

    override fun install(ctx: MetadataContext) {
        val metadata = ctx.metadata

        metadata.environment.convention(BOTH_SIDES)
        metadata.substituteTokens.convention(false)

        with(metadata.validation) {
            failOnMissingIcon.convention(true)
            failOnInvalidSemver.convention(true)
            failOnUndeclaredMixinConfig.convention(true)
            failOnMissingLicense.convention(true)
        }
    }

}
