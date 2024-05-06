plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

group = "com.meloda.app.fast.ui"

android {
    namespace = "com.meloda.app.fast.ui"
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
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
}
