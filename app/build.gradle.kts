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
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
//    id("kotlin-parcelize")
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.com.google.devtools.ksp)
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

fun getVersionName() = "$majorVersion.$minorVersion.$patchVersion"

val currentTime get() = (System.currentTimeMillis() / 1000).toInt()

dependencies {
    // DI zone
    //Koin for Default Android
    implementation(libs.koin.android)

    // Koin for Compose
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    // end of DI zone

    implementation(libs.cloudy)

    implementation(libs.coil.compose)
    implementation(libs.coil)

    implementation(libs.kotlin.reflect)

    implementation(libs.core.ktx)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.core.splashscreen)

    implementation(libs.appcompat)

    implementation(libs.activity.ktx)

    implementation(libs.fragment.ktx)

    implementation(libs.preference.ktx)

    implementation(libs.swiperefreshlayout)

    implementation(libs.recyclerview)

    implementation(libs.constraintlayout)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.glide)
    ksp(libs.compiler)

    implementation(libs.kpermissions)
    implementation(libs.kpermissions.coroutines)

    implementation(libs.appcenter.analytics)
    implementation(libs.appcenter.crashes)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.logging.interceptor)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.jdk9)

    implementation(libs.gson)

    implementation(libs.guava)

    implementation(libs.material)

    implementation(libs.library)

    // Compose zone
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    // end of Compose zone

    implementation(libs.compose.material3.pullrefresh)

    // Accompanist zone
    implementation(libs.accompanist.drawablepainter)
    // end of Accompanist zone

    // Moshi zone
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    // end of Moshi zone

    // Multiplatform

    // Navigator
    implementation(libs.voyager.navigator)

    // BottomSheetNavigator
    implementation(libs.voyager.bottom.sheet.navigator)

    // TabNavigator
    implementation(libs.voyager.tab.navigator)

    // Transitions
    implementation(libs.voyager.transitions)

    // Android

    // Android ViewModel integration
    implementation(libs.voyager.androidx)

    // Koin integration
    implementation(libs.voyager.koin)


    // Tests zone
    testImplementation(libs.junit)
    // end of Tests zone
}
