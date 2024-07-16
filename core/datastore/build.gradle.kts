plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

group = "dev.meloda.fast.datastore"

android {
    namespace = "dev.meloda.fast.datastore"
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
    api(projects.core.common)
    api(projects.core.ui)

    implementation(libs.koin.android)
}
