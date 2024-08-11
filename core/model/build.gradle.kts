plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.meloda.fast.datastore"
}

dependencies {
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
}
