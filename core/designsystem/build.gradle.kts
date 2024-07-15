plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
}

group = "com.meloda.app.fast.designsystem"

android {
    namespace = "com.meloda.app.fast.designsystem"

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
    composeOptions {
        useLiveLiterals = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)

    implementation(libs.appcompat)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    debugImplementation(libs.compose.ui.tooling)
}
