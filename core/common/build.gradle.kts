plugins {
    alias(libs.plugins.fast.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.meloda.fast.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.nanokt)

    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.kotlin.serialization)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)

}
