plugins {
    alias(libs.plugins.fast.android.feature)
    alias(libs.plugins.fast.android.library.compose)
}

android {
    namespace = "dev.meloda.fast.photoviewer"
}

dependencies {
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

    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlin.serialization)
}
