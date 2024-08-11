plugins {
    alias(libs.plugins.fast.android.library)
//    alias(libs.plugins.fast.jvm.library)
//    alias(libs.plugins.fast.koin)
}

android {
    namespace = "dev.meloda.fast.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.nanokt)

    implementation(libs.lifecycle.viewmodel.ktx)
}
