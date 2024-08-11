plugins {
    alias(libs.plugins.fast.android.library)
//    alias(libs.plugins.fast.android.koin)
}

android {
    namespace = "dev.meloda.fast.domain"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)

    // TODO: 11/08/2024, Danil Nikolaev: remove?
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    implementation(libs.eithernet)
}
