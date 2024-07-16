plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.kotlin.compose.compiler)
}

group = "dev.meloda.fast.common"

android {
    namespace = "dev.meloda.fast.common"
    compileSdk = Configs.compileSdk

    defaultConfig {
        minSdk = Configs.minSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = Configs.java
        targetCompatibility = Configs.java
    }
    kotlinOptions {
        jvmTarget = Configs.java.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
    }
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
