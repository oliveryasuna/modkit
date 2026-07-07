plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("pluginConventions") {
            id = "modkit.plugin-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.PluginConventionsPlugin"
        }
    }
}

dependencies {
    // Make these plugins available to apply from the convention plugin.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plugin.publish.plugin)
}
