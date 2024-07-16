plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
}

group = "dev.meloda.fast.ui"

android {
    namespace = "dev.meloda.fast.ui"
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
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.common)
    api(projects.core.model)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    debugImplementation(libs.compose.ui.tooling)
}
