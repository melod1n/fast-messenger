plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.meloda.fast.network"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    api(projects.core.datastore)

    implementation(libs.moshi.kotlin)
    implementation(libs.koin.android)

    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.eithernet)

    implementation(libs.converter.moshi)

    implementation(libs.logging.interceptor)

    implementation(libs.guava)
}
