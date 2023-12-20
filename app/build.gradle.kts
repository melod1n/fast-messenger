import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val sdkPackage: String = getLocalProperty("sdkPackage", "\"\"")
val sdkFingerprint: String = getLocalProperty("sdkFingerprint", "\"\"")

val otaSecretCode: String = getLocalProperty("otaSecretCode", "\"\"")

val debugUserId: String = getLocalProperty("userId", "\"0\"")
val debugAccessToken: String = getLocalProperty("accessToken", "\"\"")

val shakeClientId: String = getLocalProperty("shakeClientId", "\"\"")
val shakeClientSecret: String = getLocalProperty("shakeClientSecret", "\"\"")

fun getLocalProperty(key: String, defValue: String): String {
    return gradleLocalProperties(rootDir).getProperty(key, defValue)
}

val majorVersion = 1
val minorVersion = 6
val patchVersion = 4

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.vk.vkompose)
}

vkompose {
    skippabilityCheck = true

    recompose {
        isHighlighterEnabled = true
        isLoggerEnabled = true
    }

    testTag {
        isApplierEnabled = true
        isDrawerEnabled = false
        isCleanerEnabled = false
    }

    sourceInformationClean = true
}

androidComponents {
    onVariants { variant ->
        variant.buildConfigFields.apply {
            put(
                "sdkPackage",
                BuildConfigField(
                    type = "String",
                    value = sdkPackage,
                    comment = "sdkPackage for VK"
                )
            )
            put(
                "sdkFingerprint",
                BuildConfigField(
                    type = "String",
                    value = sdkFingerprint,
                    comment = "sdkFingerprint for VK"
                )
            )
            put(
                "otaSecretCode",
                BuildConfigField(
                    type = "String",
                    value = otaSecretCode,
                    comment = "OTA Secret code for in-app updates"
                )
            )
            put(
                "debugUserId",
                BuildConfigField(
                    type = "String",
                    value = debugUserId,
                    comment = "user id for debugging purposes"
                )
            )
            put(
                "debugAccessToken",
                BuildConfigField(
                    type = "String",
                    value = debugAccessToken,
                    comment = "access token for debugging purposes"
                )
            )
            put(
                "shakeClientId",
                BuildConfigField(
                    type = "String",
                    value = shakeClientId,
                    comment = "Shake client id"
                )
            )
            put(
                "shakeClientSecret",
                BuildConfigField(
                    type = "String",
                    value = shakeClientSecret,
                    comment = "Shake client secret"
                )
            )
        }
    }
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
        applicationId = "com.meloda.app.fast"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "_${getVersionName()}"
        }
        getByName("release") {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    configurations {
        debugImplementation {
//            exclude(group = "junit", module = "junit")
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
        useLiveLiterals = true
    }
}

fun getVersionName() = "$majorVersion.$minorVersion.$patchVersion"

val currentTime get() = (System.currentTimeMillis() / 1000).toInt()

dependencies {
    // Tests zone
    testDependencies()
    // end of Tests zone

    implementation(libs.shake)
    debugImplementation(libs.leakcanary.android)

    composeDependencies()

    performanceDependencies()

    // Koin for Default Android
    implementation(libs.koin.android)

    implementation(libs.coil)

    implementation(libs.kotlin.reflect)

    implementation(libs.core.ktx)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.preference.ktx)
    implementation(libs.material)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.kpermissions)
    implementation(libs.kpermissions.coroutines)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    networkDependencies()
}

fun DependencyHandlerScope.testDependencies() {
    testImplementation(libs.junit)
}

fun DependencyHandlerScope.networkDependencies() {
    // Moshi zone
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    // end of Moshi zone

    implementation(libs.retrofit)

    // Retrofit converters
    implementation(libs.converter.moshi)
    implementation(libs.converter.gson)
    // end of Retrofit converters

    implementation(libs.logging.interceptor)

    // TODO: 04/12/2023, Danil Nikolaev: remove gson and use moshi
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.chucker)
}

fun DependencyHandlerScope.composeDependencies() {

    // Compose-Bom zone
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    // end of Compose-Bom zone

    // Accompanist zone
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.permissions)

    // end of Accompanist zone

    // Koin for Compose
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    // end of DI zone

    // Voyager zone
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.androidx)
    implementation(libs.voyager.koin)
    // end of Voyager zone

    // Coil for Compose
    implementation(libs.coil.compose)

    // Material3 Pull-to-Refresh (until official release)
    implementation(libs.compose.material3.pullrefresh)

    // Hack for Lazy-* composables which fixes bug with scrolling
    implementation(libs.hijacker)

    androidTestImplementation(libs.compose.ui.test.junit4)

    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
}

// TODO: 01/12/2023, Danil Nikolaev: create baseline profile
fun DependencyHandlerScope.performanceDependencies() {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.profileinstaller)
}
