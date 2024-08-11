plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.fast.android.library.compose)
}

android {
    namespace = "dev.meloda.fast.common"
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.preference.ktx)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    implementation(libs.coil.compose)

    implementation(libs.nanokt.jvm)
    implementation(libs.nanokt.android)
    implementation(libs.nanokt)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlin.serialization)
}
