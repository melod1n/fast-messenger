package dev.meloda.fast

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = 35

        defaultConfig {
            minSdk = 23
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    configureKotlin<KotlinAndroidProjectExtension>()
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configureKotlin<KotlinJvmProjectExtension>()
}

private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
    // Treat all Kotlin warnings as errors (disabled by default)
    // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
    val warningsAsErrors: String? by project
    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> TODO("Unsupported project extension $this ${T::class}")
    }.apply {
        jvmTarget = JvmTarget.JVM_21
        allWarningsAsErrors = warningsAsErrors.toBoolean()
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            // Enable experimental coroutines APIs, including Flow
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
}
