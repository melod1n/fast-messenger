plugins {
    alias(libs.plugins.fast.android.feature)
    alias(libs.plugins.fast.android.library.compose)
}

android {
    namespace = "dev.meloda.fast.friends"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.nanokt.android)
    implementation(libs.nanokt.jvm)
    implementation(libs.nanokt)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.coil.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    // TODO: 03/07/2024, Danil Nikolaev: remove when stable release
    implementation("androidx.compose.foundation:foundation:1.7.0-beta07")

    implementation(libs.eithernet)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlin.serialization)
}
