import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

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
    id("dagger.hilt.android.plugin")
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
        freeCompilerArgs = listOf("-Xjvm-default=compatibility", "-opt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
    }
}

fun getVersionName() = "$majorVersion.$minorVersion.$patchVersion"

val currentTime get() = (System.currentTimeMillis() / 1000).toInt()

dependencies {
    implementation(kotlin("reflect", "1.6.10"))

    implementation(libs.androidx.core)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.common.java8)

    implementation(libs.androidx.splashScreen)

    implementation(libs.androidx.dataStore)

    implementation(libs.androidx.appCompat)

    implementation(libs.androidx.activity)

    implementation(libs.androidx.fragment)

    implementation(libs.androidx.preference)

    implementation(libs.androidx.swipeRefreshLayout)

    implementation(libs.androidx.recyclerView)

    implementation(libs.androidx.cardView)

    implementation(libs.androidx.constraintLayout)

    implementation(libs.androidx.room)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    implementation(libs.cicerone)

    implementation(libs.waveformSeekBar)

    implementation(libs.glide)
    kapt(libs.glide.compiler)

    implementation(libs.kPermissions)
    implementation(libs.kPermissions.coroutines)

    implementation(libs.appCenter.analytics)
    implementation(libs.appCenter.crashes)

    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)

    implementation(libs.okhttp3)
    implementation(libs.okhttp3.interceptor)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.viewBindingDelegate)

    implementation(libs.google.gson)

    implementation(libs.google.guava)

    implementation(libs.google.material)

    implementation(libs.jsoup)

    implementation(libs.chucker)
}