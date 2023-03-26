@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val sdkPackage: String = gradleLocalProperties(rootDir).getProperty("sdkPackage", "\"\"")
val sdkFingerprint: String = gradleLocalProperties(rootDir).getProperty("sdkFingerprint", "\"\"")

val msAppCenterToken: String =
    gradleLocalProperties(rootDir).getProperty("msAppCenterAppToken", "\"\"")
val otaSecretCode: String = gradleLocalProperties(rootDir).getProperty("otaSecretCode", "\"\"")

val majorVersion = 1
val minorVersion = 6
val patchVersion = 4

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.meloda.fast"

    compileSdk = 33

    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "${name}-${versionName}-${versionCode}.apk"
        }
    }

    defaultConfig {
        applicationId = "com.meloda.fast"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "alpha"

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

            versionNameSuffix = "_${getVersionName()}"
        }
        getByName("release") {
            isMinifyEnabled = false

            buildConfigField("String", "sdkPackage", sdkPackage)
            buildConfigField("String", "sdkFingerprint", sdkFingerprint)

            buildConfigField("String", "msAppCenterAppToken", msAppCenterToken)

            buildConfigField("String", "otaSecretCode", otaSecretCode)

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
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}

fun getVersionName() = "$majorVersion.$minorVersion.$patchVersion"

val currentTime get() = (System.currentTimeMillis() / 1000).toInt()

dependencies {
    implementation("com.hannesdorfmann:adapterdelegates4-kotlin-dsl:4.3.2")
    implementation("com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:4.3.2")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")

    implementation("androidx.core:core-ktx:1.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")

    implementation("androidx.core:core-splashscreen:1.0.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.activity:activity-ktx:1.7.0")

    implementation("androidx.fragment:fragment-ktx:1.5.6")

    implementation("androidx.preference:preference-ktx:1.2.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.recyclerview:recyclerview:1.3.0")

    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.room:room-ktx:2.5.1")
    implementation("androidx.room:room-runtime:2.5.1")
    ksp("androidx.room:room-compiler:2.5.1")

    implementation("com.github.terrakok:cicerone:7.1")

    implementation("com.github.massoudss:waveformSeekBar:5.0.0")

    implementation("com.github.bumptech.glide:glide:4.15.0")
    ksp("com.github.bumptech.glide:compiler:4.15.0")

    implementation("com.github.fondesa:kpermissions:3.4.0")
    implementation("com.github.fondesa:kpermissions-coroutines:3.4.0")

    implementation("com.microsoft.appcenter:appcenter-analytics:5.0.0")
    implementation("com.microsoft.appcenter:appcenter-crashes:5.0.0")

    implementation("com.google.dagger:hilt-android:2.45")
    kapt("com.google.dagger:hilt-android-compiler:2.45")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.5.8")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.guava:guava:31.1-jre")

    implementation("com.google.android.material:material:1.8.0")

    implementation("org.jsoup:jsoup:1.15.4")

    implementation("com.github.chuckerteam.chucker:library:3.5.2")

    implementation("dev.chrisbanes.insetter:insetter:0.6.1")

    // Compose zone
    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.ui:ui:1.4.0")

    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")

    implementation("androidx.compose.material:material-icons-extended:1.3.1")

    implementation("androidx.compose.material3:material3-window-size-class:1.0.1")

    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    // end of Compose zone
}
