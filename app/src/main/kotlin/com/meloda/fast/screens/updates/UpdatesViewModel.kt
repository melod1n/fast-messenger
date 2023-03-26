package com.meloda.fast.screens.updates

import android.app.DownloadManager
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.meloda.fast.R
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.AppConstants
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.UpdateManagerState
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.ext.listenValue
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.receiver.DownloadManagerReceiver
import com.meloda.fast.screens.updates.model.UpdateState
import com.meloda.fast.screens.updates.model.UpdatesScreenState
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

interface IUpdatesViewModel {
    val screenState: MutableStateFlow<UpdatesScreenState>
    val currentDownloadProgress: StateFlow<Int>

    val isNeedToShowChangelogAlert: Flow<Boolean>
    val isNeedToShowUnknownSourcesAlert: Flow<Boolean>
    val isNeedToShowIssuesAlert: Flow<Boolean>
    val isNeedToShowFileNotFoundAlert: Flow<Boolean>

    fun onUpdateItemExists(updateItem: UpdateItem)

    fun checkUpdates()

    fun onChangelogButtonClicked()
    fun onActionButtonClicked()
    fun onCancelDownloadButtonClicked()
    fun onIssuesButtonClicked()

    fun onChangelogAlertDismissed()
    fun onUnknownSourcesAlertDismissed()
    fun onIssuesAlertDismissed()
    fun onIssuesAlertPositiveButtonClicked()
    fun onFileNotFoundAlertDismissed()
}

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateManager: UpdateManager,
) : BaseViewModel(), IUpdatesViewModel {

    override val screenState = MutableStateFlow(UpdatesScreenState.EMPTY)
    override val currentDownloadProgress = MutableStateFlow(0)

    override val isNeedToShowChangelogAlert = MutableStateFlow(false)
    override val isNeedToShowUnknownSourcesAlert = MutableStateFlow(false)
    override val isNeedToShowIssuesAlert = MutableStateFlow(false)
    override val isNeedToShowFileNotFoundAlert = MutableStateFlow(false)

    private var currentJob: Job? = null

    init {
        updateManager.stateFlow.listenValue(::updateState)
    }

    override fun onUpdateItemExists(updateItem: UpdateItem) {
        val newForm = screenState.value.copy(
            updateItem = updateItem,
            updateState = UpdateState.NewUpdate,
            error = null
        )
        screenState.update { newForm }
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

    override fun onChangelogButtonClicked() {
        isNeedToShowChangelogAlert.tryEmit(true)
    }

    override fun onActionButtonClicked() {
        val state = screenState.value.updateState

        if (!state.isDownloaded()) {
            downloadUpdate()
            return
        }

        when (state) {
            UpdateState.NewUpdate -> checkIsInstallingAllowed()
            UpdateState.NoUpdates, UpdateState.Error -> checkUpdates()
            UpdateState.Downloaded -> installUpdate()
            else -> Unit
        }
    }

    override fun onCancelDownloadButtonClicked() {
        when (screenState.value.updateState) {
            UpdateState.Downloading -> cancelCurrentDownload()
            else -> Unit
        }
    }

    override fun onIssuesButtonClicked() {
        isNeedToShowIssuesAlert.tryEmit(true)
    }

    override fun onChangelogAlertDismissed() {
        isNeedToShowChangelogAlert.tryEmit(false)
    }

    override fun onUnknownSourcesAlertDismissed() {
        isNeedToShowUnknownSourcesAlert.tryEmit(false)
    }

    override fun onIssuesAlertDismissed() {
        isNeedToShowIssuesAlert.tryEmit(false)
    }

    override fun onIssuesAlertPositiveButtonClicked() {
        deleteInstalledFile()
        checkUpdates()
    }

    override fun onFileNotFoundAlertDismissed() {
        isNeedToShowFileNotFoundAlert.tryEmit(false)
        checkUpdates()
    }

    private fun deleteInstalledFile() {
        // TODO: 26.03.2023, Danil Nikolaev: use updateItem
        val apkName = "bruhLol"

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            AppGlobal.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        if (!file.exists()) return
        file.delete()
    }

    private fun updateState(updateManagerState: UpdateManagerState) {
        val item = UpdateItem.EMPTY
//            updateManagerState.updateItem
        val error = updateManagerState.throwable

        var fileExists = false

        if (item != null) {
            // TODO: 26.03.2023, Danil Nikolaev: use updateItem
            val apkName = "bruhLol"

            val apkFileName = "$apkName.apk"

            val destination = "%s/$apkFileName".format(
                AppGlobal.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            )

            val file = File(destination)
            fileExists = file.exists()
        }

        val newUpdateState = when {
            item != null -> {
                if (fileExists) {
                    UpdateState.Downloaded
                } else {
                    UpdateState.NewUpdate
                }
            }
            error != null -> UpdateState.Error
            else -> UpdateState.NoUpdates
        }
        updateUpdateState(newUpdateState)

        val newError = error?.message

        val newState = screenState.value.copy(
            updateItem = item,
            error = newError
        )
        screenState.update { newState }
    }

    private fun checkIsInstallingAllowed() {
        if (!isSdkAtLeast(Build.VERSION_CODES.O) && !AndroidUtils.isCanInstallUnknownApps()) {
            isNeedToShowUnknownSourcesAlert.update { true }
        } else {
            downloadUpdate()
        }
    }

    private var downloadId: Long? = null

    private fun downloadUpdate() {
        val context = AppGlobal.Instance

        updateUpdateState(UpdateState.Loading)
//        val newUpdate = screenState.value.updateItem ?: return

        // TODO: 26.03.2023, Danil Nikolaev: use updateItem
        val apkName = "bruhLol"

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        if (file.exists()) {
            updateUpdateState(UpdateState.Downloaded)
            return
        }

        val downloadUri = try {
            Uri.parse(
                "https://vk.com/doc157582555_635903147?hash=gTEOVno21WCtxX9GclYo8Liloat5V4xt4WB6nSuOMl8&dl=PQvcF2f7jyJDhJzMFOfRzCZXMx0MztmnwzhQYe4Ycdz"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Uri.EMPTY
        }

        val request = DownloadManager.Request(downloadUri).apply {
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
            downloadId = null

            installUpdate(file)

            context.unregisterReceiver(receiver)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        downloadId = AppGlobal.downloadManager.enqueue(request)

        updateUpdateState(UpdateState.Downloading)

        var isDownloaded = false

        createTimerFlow(
            isNeedToEndCondition = { isDownloaded },
            onStartAction = {
                Log.d("Downloading update", "downloadUpdate: onStart")
            },
            onTickAction = {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId ?: -1)

                val cursor = AppGlobal.downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val sizeIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val downloadedIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val size = cursor.getInt(sizeIndex)
                    val downloaded = cursor.getInt(downloadedIndex)
                    val progress = if (size != -1) {
                        downloaded * 100.0F / size
                    } else {
                        0.0F
                    }

                    val intProgress = progress.roundToInt()
                    if (intProgress >= 1) {
                        currentDownloadProgress.emit(intProgress)
                    }

                    Log.d("Downloading update", "progress: $progress%")

                    if (intProgress >= 100) {
                        isDownloaded = true
                        currentDownloadProgress.emit(0)
                        updateUpdateState(UpdateState.Downloaded)
                    }
                }
            },
            onEndAction = {},
            interval = 250.milliseconds
        ).launchIn(viewModelScope)
    }

    private fun checkDownloadedFileExists(): File? {
        // TODO: 26.03.2023, Danil Nikolaev: use updateItem
        val apkName = "bruhLol"

        val apkFileName = "$apkName.apk"

        val destination = "%s/$apkFileName".format(
            AppGlobal.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        )

        val file = File(destination)
        return if (file.exists()) file else null
    }

    private fun installUpdate(file: File? = null) {
        val context = AppGlobal.Instance
        val destinationFile = file ?: checkDownloadedFileExists() ?: run {
            isNeedToShowFileNotFoundAlert.tryEmit(true)
            return
        }

        val installIntent = AndroidUtils.getInstallPackageIntent(
            context,
            ARG_PROVIDER_PATH,
            destinationFile
        )

        context.startActivity(installIntent)
    }

    private fun updateUpdateState(newState: UpdateState) {
        val newForm = screenState.value.copy(updateState = newState)
        screenState.update { newForm }
    }

    private fun cancelCurrentDownload() {
        currentDownloadProgress.tryEmit(0)
        downloadId?.run { AppGlobal.downloadManager.remove(this) }
        checkUpdates()
    }

    companion object {
        private const val ARG_PROVIDER_PATH = ".provider"
    }
}
