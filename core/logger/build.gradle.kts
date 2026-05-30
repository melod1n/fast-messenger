plugins {
    alias(libs.plugins.fast.android.library)
}

android {
    namespace = "dev.meloda.fast.logger"
}

dependencies {
    implementation(libs.koin.android)
}
