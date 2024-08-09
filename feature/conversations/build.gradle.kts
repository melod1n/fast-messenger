plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.meloda.fast.conversations"

android {
    namespace = "dev.meloda.fast.conversations"
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
    composeOptions {
        useLiveLiterals = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.nanokt.android)
    implementation(libs.nanokt.jvm)
    implementation(libs.nanokt)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.coil.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    // TODO: 03/07/2024, Danil Nikolaev: remove when stable release (lazy column fixes)
    implementation("androidx.compose.foundation:foundation:1.7.0-beta07")

    implementation(libs.eithernet)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlin.serialization)
}
