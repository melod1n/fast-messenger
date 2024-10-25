import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

val sdkPackage: String = getLocalProperty("sdkPackage", "\"\"")
val sdkFingerprint: String = getLocalProperty("sdkFingerprint", "\"\"")

val debugToken: String = getLocalProperty("debugToken", "\"\"")

fun getLocalProperty(key: String, defValue: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(key, defValue)
}

plugins {
    alias(libs.plugins.fast.android.feature)
    alias(libs.plugins.fast.android.library.compose)
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
                "debugToken",
                BuildConfigField(
                    type = "String",
                    value = debugToken,
                    comment = "debug token for authorization"
                )
            )
        }
    }
}

android {
    namespace = "dev.meloda.fast.auth"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.ui)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.coil.compose)

    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlin.serialization)

    implementation(libs.eithernet)

    androidTestImplementation(libs.bundles.compose.ui.test)
}
