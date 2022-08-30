rootProject.name = "fast-messenger"
include(":app")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // androidx - Core
            library("androidx-core", "androidx.core:core-ktx:1.8.0")

            // androidx - Lifecycle
            version("androidx-lifecycle", "2.5.1")
            library("androidx-lifecycle-viewmodel", "androidx.lifecycle", "lifecycle-viewmodel-ktx").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-livedata", "androidx.lifecycle", "lifecycle-livedata-ktx").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-runtime", "androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-viewmodel-savedstate", "androidx.lifecycle", "lifecycle-viewmodel-savedstate").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-common-java8", "androidx.lifecycle", "lifecycle-common-java8").versionRef("androidx-lifecycle")

            // androidx - SplashScreen
            library("androidx-splashScreen", "androidx.core:core-splashscreen:1.0.0")

            // androidx - DataStore
            library("androidx-dataStore", "androidx.datastore:datastore-preferences:1.0.0")

            // androidx - AppCompat
            library("androidx-appCompat", "androidx.appcompat:appcompat:1.5.0")

            // androidx - Activity
            library("androidx-activity", "androidx.activity:activity-ktx:1.5.1")

            // androidx - Fragment
            library("androidx-fragment", "androidx.fragment:fragment-ktx:1.5.2")

            // androidx - Preference
            library("androidx-preference", "androidx.preference:preference-ktx:1.2.0")

            // androidx - SwipeRefreshLayout
            library("androidx-swipeRefreshLayout", "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

            // androidx - RecyclerView
            library("androidx-recyclerView", "androidx.recyclerview:recyclerview:1.2.1")

            // androidx - CardView
            library("androidx-cardView", "androidx.cardview:cardview:1.0.0")

            // androidx - ConstraintLayout
            library("androidx-constraintLayout", "androidx.constraintlayout:constraintlayout:2.1.4")

            // androidx - Room
            version("room", "2.4.3")
            library("androidx-room", "androidx.room", "room-ktx").versionRef("room")
            library("androidx-room-runtime", "androidx.room", "room-runtime").versionRef("room")
            library("androidx-room-compiler", "androidx.room", "room-compiler").versionRef("room")

            // Cicerone
            library("cicerone", "com.github.terrakok:cicerone:7.1")

            // WaveformSeekBar
            library("waveformSeekBar", "com.github.massoudss:waveformSeekBar:5.0.0")

            // Glide
            version("glide", "4.13.0")
            library("glide", "com.github.bumptech.glide", "glide").versionRef("glide")
            library("glide-compiler", "com.github.bumptech.glide", "compiler").versionRef("glide")

            // KPermissions
            version("kPermissions", "3.3.0")
            library("kPermissions", "com.github.fondesa", "kpermissions").versionRef("kPermissions")
            library("kPermissions-coroutines", "com.github.fondesa", "kpermissions-coroutines").versionRef("kPermissions")

            // Microsoft AppCenter
            version("appCenterSdk", "4.3.1")
            library("appCenter-analytics", "com.microsoft.appcenter", "appcenter-analytics").versionRef("appCenterSdk")
            library("appCenter-crashes", "com.microsoft.appcenter", "appcenter-crashes").versionRef("appCenterSdk")

            // Hilt
            version("hilt", "2.39.1")
            library("hilt", "com.google.dagger", "hilt-android").versionRef("hilt")
            library("hilt-compiler", "com.google.dagger", "hilt-android-compiler").versionRef("hilt")

            // Retrofit
            version("retrofit", "2.9.0")
            library("retrofit", "com.squareup.retrofit2", "retrofit").versionRef("retrofit")
            library("retrofit-gson-converter", "com.squareup.retrofit2", "converter-gson").versionRef("retrofit")

            // OkHttp3
            version("okhttp3", "5.0.0-alpha.2")
            library("okhttp3", "com.squareup.okhttp3", "okhttp").versionRef("okhttp3")
            library("okhttp3-interceptor", "com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp3")

            // Coroutines
            version("coroutines", "1.6.1")
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")

            // ViewBinding Delegate
            library("viewBindingDelegate", "com.github.yogacp:android-viewbinding:1.0.4")

            // Google - Gson
            library("google-gson", "com.google.code.gson:gson:2.8.9")

            // Google - Guava
            library("google-guava", "com.google.guava:guava:31.1-android")

            // Google - Material
            library("google-material", "com.google.android.material:material:1.6.1")

            // Jsoup
            library("jsoup", "org.jsoup:jsoup:1.15.1")

            // Chucker
            library("chucker", "com.github.chuckerteam.chucker:library:3.5.2")
        }
    }
}