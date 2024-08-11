import dev.meloda.fast.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("fast.android.library")
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:ui"))

                add("testImplementation", kotlin("test"))
                add("androidTestImplementation", kotlin("test"))

                add("implementation", libs.findBundle("nanokt").get())
            }
        }
    }
}
