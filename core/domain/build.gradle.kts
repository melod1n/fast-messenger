plugins {
    alias(libs.plugins.fast.android.library)
//    alias(libs.plugins.fast.android.koin)
}

android {
    namespace = "dev.meloda.fast.domain"
}

dependencies {
    api(projects.core.common)
    api(projects.core.data)
    api(projects.core.model)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    implementation(libs.eithernet)

    implementation(libs.bundles.nanokt)

    implementation(libs.compose.ui)
}
