plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
}

group = "com.meloda.app.fast.userbanned"

android {
    namespace = "com.meloda.app.fast.userbanned"
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
        kotlinCompilerExtensionVersion = Configs.composeCompiler
        useLiveLiterals = true
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.nanokt.android)
    implementation(libs.nanokt.jvm)
    implementation(libs.nanokt)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.coil.compose)
}
