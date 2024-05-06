plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
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
        kotlinCompilerExtensionVersion = Configs.composeCompiler
        useLiveLiterals = true
    }
}

dependencies {
    // TODO: 05/05/2024, Danil Nikolaev: maybe remove
    implementation(projects.core.common)
    implementation(projects.core.datastore)

    implementation(libs.appcompat)
    implementation(libs.accompanist.permissions)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
}
