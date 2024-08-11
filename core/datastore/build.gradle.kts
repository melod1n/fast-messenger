plugins {
    alias(libs.plugins.fast.android.library)
}

android {
    namespace = "dev.meloda.fast.datastore"
}

dependencies {
    api(projects.core.common)
    api(projects.core.ui)

    implementation(libs.koin.android)
}
