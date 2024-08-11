plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.fast.android.room)
}

android {
    namespace = "dev.meloda.fast.database"
}

dependencies {
    api(projects.core.model)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.koin.android)
}
