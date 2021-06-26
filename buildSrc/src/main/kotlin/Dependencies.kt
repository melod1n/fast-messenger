object BuildPlugins {
    val android by lazy { "com.android.tools.build:gradle:${Versions.gradlePlugin}" }
    val kotlin by lazy { "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}" }
}

object Deps {
    val kotlin by lazy { "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}" }

    val desugaring by lazy { "com.android.tools:desugar_jdk_libs:${Versions.desugaring}" }

    val appCompat by lazy { "androidx.appcompat:appcompat:${Versions.appCompat}" }
    val material by lazy { "com.google.android.material:material:${Versions.material}" }
    val core by lazy { "androidx.core:core-ktx:${Versions.core}" }
    val preferences by lazy { "androidx.preference:preference-ktx:${Versions.preferences}" }
    val swipeRefreshLayout by lazy { "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}" }
    val recyclerView by lazy { "androidx.recyclerview:recyclerview:${Versions.recyclerView}" }
    val cardView by lazy { "androidx.cardview:cardview:${Versions.cardView}" }
    val fragment by lazy { "androidx.fragment:fragment-ktx:${Versions.fragment}" }

    val coroutineCore by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}" }
    val coroutineAndroid by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}" }

    val roomRuntime by lazy { "androidx.room:room-runtime:${Versions.room}" }
    val roomCompiler by lazy { "androidx.room:room-compiler:${Versions.room}" }

    val gson by lazy { "com.google.code.gson:gson:${Versions.gson}" }

    val jsoup by lazy { "org.jsoup:jsoup:${Versions.jsoup}" }

    val acra by lazy { "ch.acra:acra:${Versions.acra}" }
}