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
    }
}
