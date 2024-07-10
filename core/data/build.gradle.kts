plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

group = "com.meloda.app.fast.data"

android {
    namespace = "com.meloda.app.fast.data"
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
    api(projects.core.datastore)
    api(projects.core.model)
    api(projects.core.network)
    api(projects.core.database)

    implementation(libs.koin.android)

    // TODO: 05/05/2024, Danil Nikolaev: research, maybe remove
    implementation(libs.retrofit)
    implementation(libs.eithernet)
}
