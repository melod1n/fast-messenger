import org.gradle.api.JavaVersion

object Configs {

    const val appCode = 1
    const val appName = "1.8.1"

    const val compileSdk = 34
    const val minSdk = 24
    const val targetSdk = 34

    val java = JavaVersion.VERSION_17

    const val composeCompiler = "1.5.13"
}
