package com.meloda.fast.activity

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.TaskManager
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.extensions.FloatExtensions.int
import com.meloda.fast.listener.OnResponseListener
import com.meloda.fast.model.NewUpdateInfo
import com.meloda.fast.receiver.DownloadUpdateReceiver
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.TimeUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class UpdateActivity : BaseActivity() {

    companion object {
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
    }

    private var isChecking = false
    private var isNewUpdate = false
    private var isDownloading = false

    private var downloadId = 0L

    private var lastCheckTime = 0L

    private var newUpdate = NewUpdateInfo()

    private lateinit var updateCheckUpdates: ExtendedFloatingActionButton
    private lateinit var updateState: TextView
    private lateinit var updateVersion: TextView
    private lateinit var updateInfo: TextView
    private lateinit var updateInfoLayout: LinearLayout
    private lateinit var updateProgress: LinearLayout
    private lateinit var updateProgressBar: CircularProgressView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        initViews()

        updateProgressBar.maxProgress = 100F

        lastCheckTime = AppGlobal.preferences.getLong("updateCheckTime", 0)

        refreshState()

        checkUpdates()

        updateCheckUpdates.setOnClickListener {
            lastCheckTime = System.currentTimeMillis()
            AppGlobal.preferences.edit().putLong("updateCheckTime", lastCheckTime).apply()

            checkUpdates()
        }
    }

    private fun initViews() {
        updateCheckUpdates = findViewById(R.id.updateCheckUpdates)
        updateInfo = findViewById(R.id.updateInfo)
        updateVersion = findViewById(R.id.updateVersion)
        updateState = findViewById(R.id.updateState)
        updateInfoLayout = findViewById(R.id.updateInfoLayout)
        updateProgress = findViewById(R.id.updateProgress)
        updateProgressBar = updateProgress.getChildAt(0) as CircularProgressView
    }

    private fun installUpdate(context: Activity, file: File) {
        val install = Intent(Intent.ACTION_VIEW)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                file
            )
        } else {
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(Uri.fromFile(file), MIME_TYPE)
        }

        context.startActivity(install)
//        context.finishAffinity()
    }

    private fun downloadUpdate() {
        checkIsInstallingAllowed()

        val timer = Timer()

        updateCheckUpdates.shrink()
        updateCheckUpdates.isClickable = false

        isDownloading = true
        refreshState()

        TaskManager.execute {
            val apkName = newUpdate.version

            val destination =
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/$apkName.apk"

            val uri = Uri.parse("$FILE_BASE_PATH$destination")

            val file = File(destination)
            if (file.exists()) file.delete()

            val request = DownloadManager.Request(Uri.parse(newUpdate.downloadLink))

            request.setTitle("${getString(R.string.app_name)} ${apkName}.apk")
            request.setMimeType(MIME_TYPE)
            request.setDestinationUri(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val receiver = DownloadUpdateReceiver()
            receiver.listener = object : OnResponseListener<Any?> {
                override fun onResponse(response: Any?) {
                    timer.cancel()

                    installUpdate(this@UpdateActivity, file)

                    unregisterReceiver(receiver)

                    runOnUiThread {
                        updateProgressBar.isIndeterminate = true

                        updateCheckUpdates.extend()
                        updateCheckUpdates.isClickable = true

                        isDownloading = false
                        refreshState()
                    }
                }

                override fun onError(t: Throwable) {
                }
            }

            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            downloadId = AppGlobal.downloadManager.enqueue(request)

            timer.schedule(object : TimerTask() {
                override fun run() {
                    val query = DownloadManager.Query()
                    query.setFilterById(downloadId)

                    val cursor = AppGlobal.downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val sizeIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val downloadedIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val size = cursor.getInt(sizeIndex)
                        val downloaded = cursor.getInt(downloadedIndex)

                        val progress = if (size != -1) (downloaded * 100.0F / size) else 0.0F

                        Log.d("Downloading update", "progress $progress%")

                        if (progress.int() > 0) {
                            runOnUiThread {
                                if (updateProgressBar.isIndeterminate) {
                                    updateProgressBar.isIndeterminate = false
                                    updateProgressBar.stopAnimation()
                                }

                                updateProgressBar.progress = progress
                            }
                        }
                    }
                }

            }, 0, 1000)
        }
    }

    private fun checkUpdates() {
        if (isChecking) return

        isChecking = true
        refreshState()

        UpdateManager.checkUpdates(object : UpdateManager.OnUpdateListener {
            override fun onNewUpdate(updateInfo: NewUpdateInfo) {
                isChecking = false
                isNewUpdate = true

                this@UpdateActivity.newUpdate = updateInfo

                refreshState()
            }

            override fun onNoUpdates() {
                isNewUpdate = false
                isChecking = false

                this@UpdateActivity.newUpdate = NewUpdateInfo()

                refreshState()
            }
        })
    }

    private fun checkIsInstallingAllowed() {
        if (!AndroidUtils.isCanInstallUnknownApps(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.warning)
            builder.setMessage(R.string.update_unknown_sources_disabled_message)
            builder.setPositiveButton(R.string.yes) { _, _ ->
                AndroidUtils.openInstallUnknownAppsScreen(this)
            }
            builder.setNegativeButton(R.string.no, null)
            builder.show()
        }
    }

    private fun refreshState() {
        when {
            isChecking -> {
                updateState.text = getString(R.string.update_state_checking)

                setAlpha(updateInfoLayout, true)
                setAlpha(updateProgress, false)
                setAlpha(updateCheckUpdates, true)
            }
            isDownloading -> {
                updateState.text = getString(R.string.update_state_downloading)

                setAlpha(updateInfoLayout, true)
                setAlpha(updateProgress, false)
                setAlpha(updateCheckUpdates, true)
            }
            else -> {
                if (isNewUpdate) {
                    updateCheckUpdates.text = getString(R.string.update_download)
                    updateCheckUpdates.icon = drawable(R.drawable.ic_file_download)
                } else {
                    updateCheckUpdates.text = getString(R.string.update_check_updates)
                    updateCheckUpdates.icon = drawable(R.drawable.ic_refresh)
                }

                updateCheckUpdates.setOnClickListener {
                    if (isNewUpdate) {
                        downloadUpdate()
                    } else {
                        checkUpdates()
                    }
                }

                updateState.text =
                    getString(if (isNewUpdate) R.string.update_state_update_available else R.string.update_state_no_updates)

                updateVersion.text =
                    if (isNewUpdate)
                        getString(
                            R.string.update_new_version,
                            newUpdate.version,
                            newUpdate.code
                        )
                    else getString(
                        R.string.update_current_version,
                        AppGlobal.versionName,
                        AppGlobal.versionCode
                    )

                updateInfo.text =
                    when {
                        isNewUpdate -> if (newUpdate.changelog.isEmpty()) "" else getString(
                            R.string.update_changelog,
                            HtmlCompat.fromHtml(
                                newUpdate.changelog,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        lastCheckTime.toString().isEmpty() || lastCheckTime == 0L -> ""
                        else -> getString(R.string.update_last_check_time, getCheckTime())
                    }

                setAlpha(updateInfoLayout, false)
                setAlpha(updateProgress, true)
                setAlpha(updateCheckUpdates, false)
            }
        }
    }

    private fun getCheckTime(): String {
        val time = lastCheckTime

        val lastTime = TimeUtils.removeTime(Date(time))
        val currentTime = TimeUtils.removeTime(Date(System.currentTimeMillis()))

        val format = if (currentTime > lastTime) {
            "dd.MM.yyyy HH:mm"
        } else {
            "HH:mm"
        }

        return SimpleDateFormat(format, Locale.getDefault()).format(time)
    }

    private fun setAlpha(view: View, toZero: Boolean) {
        if (toZero) {
            view.animate()
                .alpha(0F)
                .setDuration(250)
                .withEndAction { view.isVisible = false }
                .start()
        } else {
            view.animate()
                .alpha(1F)
                .setDuration(250)
                .withStartAction { view.isVisible = true }
                .start()
        }
    }

}