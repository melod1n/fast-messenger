import java.util.Properties

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.meloda.fast"
    compileSdk = Configs.compileSdk

    defaultConfig {
        applicationId = "dev.meloda.fast"
        minSdk = Configs.minSdk
        targetSdk = Configs.targetSdk
        versionCode = Configs.appCode
        versionName = Configs.appName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // TODO: 06/05/2024, Danil Nikolaev: придумать, как совместить с github actions
//    applicationVariants.all {
//        val variant = this
//        variant.outputs
//            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
//            .forEach { output ->
//                if (variant.buildType.name == "release") {
//                    val outputFileName = "fastvk-v${variant.versionName}-${variant.flavorName}.apk"
//                    output.outputFileName = outputFileName
//                }
//            }
//    }

    signingConfigs {
        create("release") {
            val keystoreProperties = Properties()
            val keystorePropertiesFile = file("keystore/keystore.properties")

            storeFile = file("keystore/keystore.jks")

            if (keystorePropertiesFile.exists()) {
                keystorePropertiesFile.inputStream().let(keystoreProperties::load)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            } else {
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_SIGN_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_SIGN_KEY_PASSWORD")
            }
        }

        create("debugSigning") {
            initWith(getByName("release"))
        }
    }

    buildTypes {
        named("debug") {
            signingConfig = signingConfigs.getByName("debugSigning")
            applicationIdSuffix = ".debug"
        }
        named("release") {
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // TODO: 15/05/2024, Danil Nikolaev: add to other modules with build convention
        register("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
        }
    }

    val flavorDimension = "variant"
    flavorDimensions += flavorDimension

    productFlavors {
        register("amethyst") {
            dimension = flavorDimension
            isDefault = true
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.feature.auth)
    implementation(projects.feature.chatmaterials)
    implementation(projects.feature.conversations)
    implementation(projects.feature.languagepicker)
    implementation(projects.feature.messageshistory)
    implementation(projects.feature.photoviewer)
    implementation(projects.feature.settings)
    implementation(projects.feature.friends)
    implementation(projects.feature.profile)
    implementation(projects.feature.photoviewer)

    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.datastore)

    // Tests zone
    testImplementation(libs.junit)
    // end of Tests zone

    // Compose-Bom zone
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    // end of Compose-Bom zone

    implementation(libs.accompanist.permissions)

    // Coil for Compose
    implementation(libs.coil.compose)

//    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    implementation(libs.coil)

    implementation(libs.core.ktx)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.preference.ktx)
    implementation(libs.material)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.nanokt)
    implementation(libs.nanokt.android)
    implementation(libs.nanokt.jvm)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlin.serialization)

}
