plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(ConfigData.compileSdkVersion)
    buildToolsVersion(ConfigData.buildToolsVersion)

    defaultConfig {
        applicationId = "com.meloda.fast"
        minSdkVersion(ConfigData.minSdkVersion)
        targetSdkVersion(ConfigData.targetSdkVersion)
        versionCode = ConfigData.versionCode
        versionName = ConfigData.versionName
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

java {
    val kotlinSrcDir = "src/main/kotlin"
    println(sourceSets.names)
//    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
//    mainJavaSourceSet.srcDir(kotlinSrcDir)
//    println(mainJavaSourceSet.srcDirs)
}

//java.sourceSets.create("src/main/kotlin")

//sourceSets {
//    main.java.srcDirs += "src/main/kotlin"
//}

dependencies {
    implementation(Deps.kotlin)

    coreLibraryDesugaring(Deps.desugaring)

    implementation(Deps.appCompat)
    implementation(Deps.material)
    implementation(Deps.core)
    implementation(Deps.preferences)
    implementation(Deps.swipeRefreshLayout)
    implementation(Deps.recyclerView)
    implementation(Deps.cardView)
    implementation(Deps.fragment)

    implementation(Deps.coroutineCore)
    implementation(Deps.coroutineAndroid)

    implementation(Deps.roomRuntime)
    kapt(Deps.roomCompiler)

    implementation(Deps.gson)
    implementation(Deps.jsoup)
    implementation(Deps.acra)
    implementation("com.github.yogacp:android-viewbinding:1.0.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}")

    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Versions.retrofit}")
}