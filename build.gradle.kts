buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.android.tools.build:gradle:${Versions.gradlePlugin}")
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