package ru.melod1n.project.vkm.common

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Handler
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.facebook.drawee.backends.pipeline.Fresco
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import org.acra.ACRA
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes
import ru.melod1n.project.vkm.BuildConfig
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.api.UserConfig
import ru.melod1n.project.vkm.api.VKApi
import ru.melod1n.project.vkm.base.mvp.MvpBase
import ru.melod1n.project.vkm.database.AppDatabase
import ru.melod1n.project.vkm.database.MemoryCache
import ru.melod1n.project.vkm.fragment.FragmentSettings
import ru.melod1n.project.vkm.util.AndroidUtils
import java.util.*

@SuppressLint("NonConstantResourceId")
@ReportsCrashes(
    mailTo = "lischenkodev@gmail.com",
    mode = ReportingInteractionMode.DIALOG,
    resDialogTitle = R.string.app_has_been_crashed,
    resDialogText = R.string.empty,
    resDialogTheme = R.style.AppTheme_Dialog,
    resDialogPositiveButtonText = R.string.send_crash_report,
    resDialogNegativeButtonText = R.string.ok
)
class AppGlobal : Application() {

    companion object {
        const val APP_CENTER_TOKEN = "c87e410a-d622-4c52-ad7e-7388ab511704"

        lateinit var windowManager: WindowManager
        lateinit var connectivityManager: ConnectivityManager
        lateinit var inputMethodManager: InputMethodManager
        lateinit var clipboardManager: ClipboardManager
        lateinit var downloadManager: DownloadManager

        lateinit var preferences: SharedPreferences
        lateinit var locale: Locale
        lateinit var handler: Handler
        lateinit var resources: Resources
        lateinit var packageName: String
        lateinit var database: AppDatabase
        lateinit var instance: AppGlobal

        lateinit var packageManager: PackageManager

        var versionName = ""
        var versionCode = 0L

        var screenWidth = 0
        var screenHeight = 0

        fun post(runnable: Runnable) {
            handler.post(runnable)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (!BuildConfig.DEBUG) {
            AppCenter.start(
                this, APP_CENTER_TOKEN, Analytics::class.java, Crashes::class.java
            )

            ACRA.init(this)
        }

        Fresco.initialize(this)

        database = Room.databaseBuilder(this, AppDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        handler = Handler(mainLooper)
        locale = Locale.getDefault()

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info)

        Companion.resources = resources
        Companion.packageName = packageName
        Companion.packageManager = packageManager

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        screenWidth = AndroidUtils.getDisplayWidth()
        screenHeight = AndroidUtils.getDisplayHeight()

        UserConfig.restore()

        VKApi.init(this)

        MvpBase.init(handler)

        fillMemoryCache()

        applyNightMode()
    }

    private fun fillMemoryCache() {
        TaskManager.execute {
            val users = database.users.getAll()
            val groups = database.groups.getAll()

            MemoryCache.appendUsers(users)
            MemoryCache.appendGroups(groups)
        }
    }

    fun applyNightMode(value: String? = null) {
        val mode = value ?: preferences.getString(FragmentSettings.KEY_THEME, "-1")!!

        val nightMode = getNightMode(mode.toInt())

        val oldNightMode = AppCompatDelegate.getDefaultNightMode()

        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun getNightMode(nightMode: Int = -1): Int {
        val mode = if (nightMode != -1) nightMode else preferences.getString(
            FragmentSettings.KEY_THEME,
            "-1"
        )!!.toInt()

        return when (mode) {
            1 -> AppCompatDelegate.MODE_NIGHT_YES
            2 -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            3 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
    }

}