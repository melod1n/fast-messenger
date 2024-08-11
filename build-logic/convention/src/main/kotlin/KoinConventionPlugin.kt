import dev.meloda.fast.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            dependencies {
                add("implementation", libs.findLibrary("koin.core").get())
            }

            pluginManager.withPlugin("com.android.base") {
                dependencies {
                    add("implementation", libs.findLibrary("koin.android").get())
                    add("implementation", libs.findLibrary("koin.androidx.compose").get())
                    add("implementation", libs.findLibrary("koin.androidx.compose.navigation").get())
                }
            }
        }
    }
}
