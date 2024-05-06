plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

group = "com.meloda.app.fast.common"

android {
    namespace = "com.meloda.app.fast.common"
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
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.preference.ktx)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.koin.android)
    implementation(libs.coil.compose)
}
