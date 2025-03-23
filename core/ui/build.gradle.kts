plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.fast.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.meloda.fast.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    implementation(projects.core.presentation)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlin.serialization)
    implementation(libs.koin.androidx.compose.navigation)

    debugImplementation(libs.compose.ui.tooling)
}
