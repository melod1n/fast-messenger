plugins {
    alias(libs.plugins.fast.android.library)
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

    implementation(libs.koin.android)

    // TODO: 05/05/2024, Danil Nikolaev: research, maybe remove
    implementation(libs.retrofit)
    implementation(libs.eithernet)
}
