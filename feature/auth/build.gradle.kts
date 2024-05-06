import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val sdkPackage: String = getLocalProperty("sdkPackage", "\"\"")
val sdkFingerprint: String = getLocalProperty("sdkFingerprint", "\"\"")

val debugUserId: String = getLocalProperty("userId", "\"0\"")
val debugAccessToken: String = getLocalProperty("accessToken", "\"\"")

fun getLocalProperty(key: String, defValue: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(key, defValue)
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
}

group = "com.meloda.app.fast.auth"

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
        }
    }
}

android {
    namespace = "com.meloda.app.fast.auth"
    compileSdk = Configs.compileSdk

    defaultConfig {
        minSdk = Configs.minSdk
    }

    ksp {
        arg("compose-destinations.moduleName", "auth")
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Configs.composeCompiler
        useLiveLiterals = true
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.ui)

    implementation(projects.feature.conversations)
    implementation(projects.feature.userbanned)

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
