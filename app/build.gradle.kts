import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.fast.android.application)
    alias(libs.plugins.fast.android.application.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.meloda.fastvk"

    defaultConfig {
        applicationId = "dev.meloda.fastvk"

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }

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
    }

    applicationVariants.all {
        outputs.all {
            val date = System.currentTimeMillis() / 1000
            val buildType = buildType.name
            val appVersion = versionName
            val appVersionCode = versionCode

            val newApkName = "app-$buildType-v$appVersion($appVersionCode)-$date.apk"
            (this as? BaseVariantOutputImpl)?.outputFileName = newApkName
        }
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
    implementation(projects.feature.createchat)

    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.domain)
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
