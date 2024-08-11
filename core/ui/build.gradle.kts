plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.fast.android.library.compose)
}

android {
    namespace = "dev.meloda.fast.ui"
}

dependencies {
    implementation(projects.core.common)
    api(projects.core.model)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    debugImplementation(libs.compose.ui.tooling)
}
