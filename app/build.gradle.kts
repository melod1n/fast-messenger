@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val sdkPackage: String = getLocalProperty("sdkPackage", "\"\"")
val sdkFingerprint: String = getLocalProperty("sdkFingerprint", "\"\"")

val msAppCenterToken: String = getLocalProperty("msAppCenterAppToken", "\"\"")
val otaSecretCode: String = getLocalProperty("otaSecretCode", "\"\"")

val debugUserId: String = getLocalProperty("userId", "\"0\"")
val debugAccessToken: String = getLocalProperty("accessToken", "\"\"")

fun getLocalProperty(key: String, defValue: String): String {
    return gradleLocalProperties(rootDir).getProperty(key, defValue)
}

val majorVersion = 1
val minorVersion = 6
val patchVersion = 4

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.meloda.fast"

    compileSdk = 34

    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "${name}-${versionName}-${versionCode}.apk"
        }
    }

    defaultConfig {
        applicationId = "com.meloda.fast"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
//                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "sdkPackage", sdkPackage)
            buildConfigField("String", "sdkFingerprint", sdkFingerprint)

            buildConfigField("String", "msAppCenterAppToken", msAppCenterToken)

            buildConfigField("String", "otaSecretCode", otaSecretCode)

            buildConfigField("String", "userId", debugUserId)
            buildConfigField("String", "accessToken", debugAccessToken)

            versionNameSuffix = "_${getVersionName()}"
        }
        getByName("release") {
            isMinifyEnabled = false

            buildConfigField("String", "sdkPackage", sdkPackage)
            buildConfigField("String", "sdkFingerprint", sdkFingerprint)

            buildConfigField("String", "msAppCenterAppToken", msAppCenterToken)

            buildConfigField("String", "otaSecretCode", otaSecretCode)

            buildConfigField("String", "userId", debugUserId)
            buildConfigField("String", "accessToken", debugAccessToken)

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    val flavorDimension = "version"

    flavorDimensions += flavorDimension

    productFlavors {
        create("dev") {
            resourceConfigurations += listOf("en", "xxhdpi")

            dimension = flavorDimension
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("full") {
            dimension = flavorDimension
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
        useLiveLiterals = true
    }
}

kapt {
    correctErrorTypes = true
}

fun getVersionName() = "$majorVersion.$minorVersion.$patchVersion"

val currentTime get() = (System.currentTimeMillis() / 1000).toInt()

dependencies {
    // DI zone
    //Koin for Default Android
    implementation("io.insert-koin:koin-android:3.5.0")

    // Koin for Compose
    implementation("io.insert-koin:koin-androidx-compose:3.5.2-RC1")
    implementation("io.insert-koin:koin-androidx-compose-navigation:3.5.2-RC1")
    // end of DI zone

    implementation("com.github.skydoves:cloudy:0.1.2")

    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil:2.4.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")

    implementation("androidx.core:core-ktx:1.12.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.activity:activity-ktx:1.8.0")

    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.room:room-ktx:2.6.0")
    implementation("androidx.room:room-runtime:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    implementation("com.github.massoudss:waveformSeekBar:5.0.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.github.fondesa:kpermissions:3.4.0")
    implementation("com.github.fondesa:kpermissions-coroutines:3.4.0")

    implementation("com.microsoft.appcenter:appcenter-analytics:5.0.2")
    implementation("com.microsoft.appcenter:appcenter-crashes:5.0.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:1.7.3")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.guava:guava:32.1.3-jre")

    implementation("com.google.android.material:material:1.10.0")

    implementation("com.github.chuckerteam.chucker:library:4.0.0")

    // Compose zone

    implementation(platform("androidx.compose:compose-bom:2023.10.01"))

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")

    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.compose.material3:material3-window-size-class")

    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")

    implementation("androidx.compose.runtime:runtime-saveable")
    // end of Compose zone

    implementation("eu.bambooapps:compose-material3-pullrefresh:1.0.1")

    // Accompanist zone
    implementation("com.google.accompanist:accompanist-drawablepainter:0.33.1-alpha")
    // end of Accompanist zone

    // Moshi zone
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
    // end of Moshi zone

    val voyagerVersion = "1.0.0-rc10"

    // Multiplatform

    // Navigator
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")

    // BottomSheetNavigator
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")

    // TabNavigator
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")

    // Transitions
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

    // Android

    // Android ViewModel integration
    implementation("cafe.adriel.voyager:voyager-androidx:$voyagerVersion")

    // Koin integration
    implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion")


    // Tests zone
    testImplementation("junit:junit:4.13.2")
    // end of Tests zone
}
