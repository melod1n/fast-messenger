plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
}

group = "dev.meloda.fast.messageshistory"

android {
    namespace = "dev.meloda.fast.messageshistory"
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

    implementation(libs.coil.compose)
    implementation(libs.coil)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    // TODO: 03/07/2024, Danil Nikolaev: remove when stable release
    implementation("androidx.compose.foundation:foundation:1.7.0-beta07")

    implementation(libs.eithernet)

    implementation(libs.kotlin.serialization)

    implementation(libs.androidx.navigation.compose)
}
