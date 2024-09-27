plugins {
    alias(libs.plugins.fast.android.feature)
    alias(libs.plugins.fast.android.library.compose)
}

android {
    namespace = "dev.meloda.fast.messageshistory"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.nanokt.android)
    implementation(libs.nanokt.jvm)
    implementation(libs.nanokt)

    implementation(libs.coil.compose)
    implementation(libs.coil)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.eithernet)

    implementation(libs.kotlin.serialization)

    implementation(libs.androidx.navigation.compose)
}
