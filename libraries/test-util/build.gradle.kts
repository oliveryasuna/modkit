plugins {
    id("modkit.library-conventions")
}

dependencies {
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter)

    api(gradleTestKit())
}
