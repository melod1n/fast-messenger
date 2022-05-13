import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val sdkPackage: String = gradleLocalProperties(rootDir).getProperty("sdkPackage")
val sdkFingerprint: String = gradleLocalProperties(rootDir).getProperty("sdkFingerprint")

val msAppCenterToken: String =
    gradleLocalProperties(rootDir).getProperty("msAppCenterAppToken", null)
val otaSecretCode: String = gradleLocalProperties(rootDir).getProperty("otaSecretCode")

val majorVersion = 1
val minorVersion = 5
val patchVersion = 6

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.meloda.fast"

    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "com.meloda.fast"
        minSdk = 23
        targetSdk = 30
        versionCode = 1
        versionName = "alpha"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "com.meloda.fast"
}

fun getVersionName() = "$majorVersion.$minorVersion.$patchVersion"

val currentTime get() = (System.currentTimeMillis() / 1000).toInt()

kapt {
    correctErrorTypes = true

    //use this shit if you don't want have hilt errors
    javacOptions {
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }
}

dependencies {
    // Cicerone - Navigation
    implementation("com.github.terrakok:cicerone:7.1")

    // androidx - ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    // androidx - SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    // WaveformSeekBar - for voice messages
    implementation("com.github.massoudss:waveformSeekBar:5.0.0")


    // androidx - DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // androidx - Base
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.7.0-alpha01")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    implementation("androidx.room:room-ktx:2.4.2")
    implementation("androidx.room:room-runtime:2.4.2")
    kapt("androidx.room:room-compiler:2.4.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.4.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.1")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.dagger:hilt-android:2.39.1")
    kapt("com.google.dagger:hilt-android-compiler:2.39.1")

    implementation("com.github.yogacp:android-viewbinding:1.0.4")

    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.jsoup:jsoup:1.14.3")

    implementation("com.github.bumptech.glide:glide:4.13.0")
    kapt("com.github.bumptech.glide:compiler:4.13.0")
    implementation(kotlin("reflect"))

    // KPermissions
    val kPermissionsVersion = "3.3.0"
    implementation("com.github.fondesa:kpermissions:$kPermissionsVersion")
    implementation("com.github.fondesa:kpermissions-coroutines:$kPermissionsVersion")

    // Microsoft AppCenter
    val appCenterSdkVersion = "4.3.1"
    implementation("com.microsoft.appcenter:appcenter-analytics:$appCenterSdkVersion")
    implementation("com.microsoft.appcenter:appcenter-crashes:$appCenterSdkVersion")

    // Google Guava
    implementation("com.google.guava:guava:31.1-android")


}