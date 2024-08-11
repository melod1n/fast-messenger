import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import dev.meloda.fast.configureKotlinAndroid
import dev.meloda.fast.disableUnnecessaryAndroidTests
import dev.meloda.fast.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.parcelize")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            extensions.configure<LibraryAndroidComponentsExtension> {
                disableUnnecessaryAndroidTests(target)
            }
            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }
}
