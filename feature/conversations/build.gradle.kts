plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
}

group = "com.meloda.app.fast.conversations"

android {
    namespace = "com.meloda.app.fast.conversations"
    compileSdk = Configs.compileSdk

    defaultConfig {
        minSdk = Configs.minSdk
    }

    ksp {
        arg("compose-destinations.moduleName", "conversations")
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

    // Hack for Lazy-* composables which fixes bug with scrolling
    implementation(libs.hijacker)

    implementation(libs.coil.compose)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Material3 Pull-to-Refresh (until official release)
    // TODO: 27/12/2023, Danil Nikolaev: remove when official release
    implementation(libs.compose.material3.pullrefresh)
}
