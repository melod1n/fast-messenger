plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
}

group = "dev.meloda.fast.network"

android {
    namespace = "dev.meloda.fast.network"
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
        buildConfig = true
    }
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)

    implementation(libs.moshi.kotlin)
    implementation(libs.koin.android)

    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.eithernet)

    implementation(libs.converter.moshi)

    implementation(libs.logging.interceptor)

    implementation(libs.guava)
}
