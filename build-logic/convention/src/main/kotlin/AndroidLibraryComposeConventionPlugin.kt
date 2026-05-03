import com.android.build.api.dsl.LibraryExtension
import dev.meloda.fast.configureAndroidCompose
import dev.meloda.fast.getVersionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            apply(plugin = "com.github.skydoves.compose.stability.analyzer")

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
                androidResources.enable = false
                defaultConfig {
                    minSdk = getVersionInt("minSdk")
                    compileSdk = getVersionInt("compileSdk")
                }
            }
        }
    }
}
