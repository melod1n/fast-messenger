buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")

        classpath("com.google.dagger:hilt-android-gradle-plugin:2.37")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        //maven { url 'https://jitpack.io' }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}