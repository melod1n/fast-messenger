package com.meloda.fast.screens.updates

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.meloda.fast.R
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.AppConstants
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.UpdateManagerState
import com.meloda.fast.ext.listenValue
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.receiver.DownloadManagerReceiver
import com.meloda.fast.screens.updates.model.UpdatesScreenState
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Timer
import javax.inject.Inject

interface IUpdatesViewModel {
    val screenStateFlow: StateFlow<UpdatesScreenState>

    val isNeedToShowUnknownSourcesAlert: Flow<Boolean>
    val isNeedToShowProgressBar: Flow<Boolean>

    fun onUpdateItemExists(updateItem: UpdateItem)

    fun checkUpdates()

    fun onActionButtonClicked()

    fun onUnknownSourcesAlertDismissed()
}

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateManager: UpdateManager,
) : BaseViewModel(), IUpdatesViewModel {

    override val screenStateFlow = MutableStateFlow(UpdatesScreenState.EMPTY)

    override val isNeedToShowUnknownSourcesAlert = MutableStateFlow(false)

    override val isNeedToShowProgressBar = MutableStateFlow(false)

    private var currentJob: Job? = null

    init {
        updateManager.stateFlow.listenValue(::updateState)
    }

    override fun onUpdateItemExists(updateItem: UpdateItem) {
        val newForm = screenStateFlow.value.copy(
            updateItem = updateItem,
            updateState = UpdateState.NewUpdate,
            error = null
        )
        screenStateFlow.update { newForm }
    }

    override fun checkUpdates() {
        if (currentJob != null) {
            currentJob?.cancel()
            currentJob = null
        }

        updateUpdateState(UpdateState.Loading)

        currentJob = updateManager.checkUpdates().apply {
            invokeOnCompletion { currentJob = null }
        }
    }

    override fun onActionButtonClicked() {
        when (screenStateFlow.value.updateState) {
            UpdateState.NewUpdate -> checkIsInstallingAllowed()
            UpdateState.NoUpdates, UpdateState.Error -> checkUpdates()
            else -> Unit
        }
    }

    override fun onUnknownSourcesAlertDismissed() {
        isNeedToShowUnknownSourcesAlert.tryEmit(false)
    }

    private fun updateState(updateManagerState: UpdateManagerState) {
        val item = updateManagerState.updateItem
        val error = updateManagerState.throwable

        val newUpdateState = when {
            item != null -> UpdateState.NewUpdate
            error != null -> UpdateState.Error
            else -> UpdateState.NoUpdates
        }
        updateUpdateState(newUpdateState)

        val newError = error?.message

        val newForm = screenStateFlow.value.copy(
            updateItem = item,
            error = newError
        )
        screenStateFlow.update { newForm }
    }

    private fun checkIsInstallingAllowed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
            !AndroidUtils.isCanInstallUnknownApps()
        ) {
            isNeedToShowUnknownSourcesAlert.update { true }
        } else {
            downloadUpdate()
        }
    }

    private var timer: Timer? = null
    private var downloadId: Long? = null

    private fun downloadUpdate() {
        val context = AppGlobal.Instance

        updateUpdateState(UpdateState.Loading)

        timer = Timer()

        val newUpdate = screenStateFlow.value.updateItem ?: return

        val apkName = newUpdate.versionName

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        if (file.exists()) file.delete()

        val request = DownloadManager.Request(Uri.parse(newUpdate.downloadLink)).apply {
            setTitle("${context.getString(R.string.app_name)} $apkFileName")
            setMimeType(AppConstants.INSTALL_APP_MIME_TYPE)
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                apkFileName
            )
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or
                        DownloadManager.Request.NETWORK_MOBILE
            )
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val receiver = DownloadManagerReceiver()
        receiver.onReceiveAction = {
            timer?.cancel()
            downloadId = null

            installUpdate(context, file)

            context.unregisterReceiver(receiver)

            updateUpdateState(UpdateState.NewUpdate)
        }

        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        downloadId = AppGlobal.downloadManager.enqueue(request)

        updateUpdateState(UpdateState.Downloading)

//        if (binding.loadingProgress.max != 100 * 100) {
//            binding.loadingProgress.max = 100 * 100
//        }

//        timer?.schedule(object : TimerTask() { // for progress
//            override fun run() {
//                val query = DownloadManager.Query()
//                query.setFilterById(downloadId ?: -1)
//
//                val cursor = AppGlobal.downloadManager.query(query)
//                if (cursor.moveToFirst()) {
//                    val sizeIndex =
//                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
//                    val downloadedIndex =
//                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
//                    val size = cursor.getInt(sizeIndex)
//                    val downloaded = cursor.getInt(downloadedIndex)
//
//                    val progress = if (size != -1) (downloaded * 100.0F / size) else 0.0F
//
//                    if (progress.toInt() >= 1) {
//                        viewModelScope.launch {
//                            if (view == null) {
//                                downloadId?.run { AppGlobal.downloadManager.remove(this) }
//                                timer?.cancel()
//                                return@launch
//                            }
//                            binding.loadingProgress.isIndeterminate = false
//
//                            if (binding.loadingProgress.progress != progress.toInt()) {
//                                ObjectAnimator.ofInt(
//                                    binding.loadingProgress,
//                                    "progress",
//                                    binding.loadingProgress.progress,
//                                    progress.toInt() * 100
//                                ).apply {
//                                    duration = 250
//                                    setAutoCancel(true)
//                                    interpolator = DecelerateInterpolator()
//                                }.start()
//                            }
//                        }
//                    }
//                    Log.d("Downloading update", "progress $progress%")
//                }
//            }
//        }, 0, 250)
    }

    private fun installUpdate(context: Context, file: File) {
        val installIntent = AndroidUtils.getInstallPackageIntent(
            context,
            ARG_PROVIDER_PATH,
            file
        )

        context.startActivity(installIntent)
    }

    private fun updateUpdateState(newState: UpdateState) {
        viewModelScope.launch { isNeedToShowProgressBar.emit(newState == UpdateState.Loading) }

        val newForm = screenStateFlow.value.copy(
            updateState = newState
        )
        screenStateFlow.update { newForm }
    }

    companion object {
        private const val ARG_PROVIDER_PATH = ".provider"
    }
}
