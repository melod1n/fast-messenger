plugins {
    alias(libs.plugins.fast.android.feature)
    alias(libs.plugins.fast.android.library.compose)
}

android {
    namespace = "dev.meloda.fast.createchat"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.bundles.nanokt)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.coil.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(libs.eithernet)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlin.serialization)
}
