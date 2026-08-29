plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plugin.publish.plugin)
    implementation(libs.vanniktech.maven.publish.plugin)
}

gradlePlugin {
    plugins {
        create("baseConventions") {
            id = "modkit.base-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.BaseConventionsPlugin"
        }
        create("pluginConventions") {
            id = "modkit.plugin-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.PluginConventionsPlugin"
        }
        create("libraryConventions") {
            id = "modkit.library-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.LibraryConventionsPlugin"
        }
        create("loaderPluginConventions") {
            id = "modkit.loader-plugin-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.LoaderPluginConventionsPlugin"
        }
    }
}
