plugins {
    alias(libs.plugins.fast.android.library)
//    alias(libs.plugins.fast.koin)
}

android {
    namespace = "dev.meloda.fast.data"
}

dependencies {
    api(projects.core.common)
    api(projects.core.datastore)
    api(projects.core.model)
    api(projects.core.network)
    api(projects.core.database)

    // TODO: 11/08/2024, Danil Nikolaev: remove?
    implementation(libs.retrofit)
    implementation(libs.eithernet)
    implementation(libs.koin.android)
}
