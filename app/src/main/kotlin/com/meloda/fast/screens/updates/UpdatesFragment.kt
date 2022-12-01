package com.meloda.fast.screens.updates

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.common.AppConstants
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.databinding.FragmentUpdatesBinding
import com.meloda.fast.ext.clear
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.setIfNotEquals
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.receiver.DownloadManagerReceiver
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class UpdatesFragment : BaseViewModelFragment<UpdatesViewModel>(R.layout.fragment_updates) {

    private val binding by viewBinding(FragmentUpdatesBinding::bind)

    override val viewModel: UpdatesViewModel by viewModels()

    private var downloadId: Long? = null

    private var timer: Timer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.applyInsetter {
            type(statusBars = true) { padding() }
        }

        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)

        binding.root.applyInsetter {
            type(navigationBars = true) { padding() }
        }

        UpdateManager.newUpdate.observe(viewLifecycleOwner) { item ->
            viewModel.currentItem.setIfNotEquals(item)
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            state?.run { refreshState(this) }
        }

        if (requireArguments().containsKey(ARG_UPDATE_ITEM)) {
            val updateItem: UpdateItem =
                requireArguments().getParcelableCompat(ARG_UPDATE_ITEM, UpdateItem::class.java)
                    ?: return
            viewModel.currentItem.setIfNotEquals(updateItem)
            viewModel.updateState.setIfNotEquals(UpdateState.NewUpdate)
        } else {
            viewModel.checkUpdates()
        }

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.changelog.setOnClickListener {
            showChangelogAlert()
        }
    }

    private fun refreshState(state: UpdateState) {
        binding.actionButton.toggleVisibility(
            !listOf(
                UpdateState.Downloading,
                UpdateState.Loading
            ).contains(viewModel.updateState.value)
        )
        binding.flow.toggleVisibility(
            !listOf(
                UpdateState.Downloading,
                UpdateState.Loading
            ).contains(viewModel.updateState.value)
        )
        binding.progress.toggleVisibility(
            viewModel.updateState.value == UpdateState.Loading
        )
        binding.changelog.toggleVisibility(
            viewModel.updateState.value == UpdateState.NewUpdate
        )
        binding.loadingProgress.toggleVisibility(
            viewModel.updateState.value == UpdateState.Downloading
        )

        if (state != UpdateState.Downloading) {
            timer?.cancel()
            downloadId?.run { AppGlobal.downloadManager.remove(this) }
        }

        when (state) {
            UpdateState.NewUpdate -> {
                val item = viewModel.currentItem.value ?: return
                binding.title.setText(R.string.fragment_updates_new_version)

                binding.description.text = getString(
                    R.string.fragment_updates_new_version_description,
                    item.versionName
                )

                binding.actionButton.setText(R.string.fragment_updates_download_update)
                binding.actionButton.setOnClickListener { checkIsInstallingAllowed(item) }
            }
            UpdateState.NoUpdates -> {
                binding.title.setText(R.string.fragment_updates_no_updates)
                binding.description.setText(R.string.fragment_updates_no_updates_description)

                binding.actionButton.setText(R.string.fragment_updates_check_updates)
                binding.actionButton.setOnClickListener { viewModel.checkUpdates() }
            }
            UpdateState.Loading -> {
                binding.title.clear()
                binding.description.clear()
                binding.actionButton.clear()
            }
            UpdateState.Error -> {
                val error = viewModel.currentError.value ?: return

                binding.title.setText(R.string.fragment_updates_error_occurred)

                val errorText =
                    if (error.contains("cannot be converted", ignoreCase = true)
                        || error.contains("begin_object", ignoreCase = true)
                    ) {
                        "OTA Server is unavailable"
                    } else {
                        getString(R.string.fragment_updates_error_occurred_description, error)
                    }

                binding.description.text = errorText

                binding.actionButton.setText(R.string.fragment_updates_try_again)
                binding.actionButton.setOnClickListener { viewModel.checkUpdates() }
            }
            UpdateState.Downloading -> {
                binding.loadingProgress.run {
                    max = 0
                    progress = 0
                    isIndeterminate = true
                }
            }
        }
    }

    private fun checkIsInstallingAllowed(item: UpdateItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
            !AndroidUtils.isCanInstallUnknownApps(requireContext())
        ) {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(R.string.warning)
            builder.setMessage(R.string.fragment_updates_unknown_sources_disabled_message)
            builder.setPositiveButton(R.string.yes) { _, _ ->
                AndroidUtils.openInstallUnknownAppsScreen(requireContext())
            }
            builder.setNegativeButton(R.string.no, null)
            builder.show()
        } else {
            downloadUpdate(item)
        }
    }

    private fun downloadUpdate(newUpdate: UpdateItem) {
        viewModel.updateState.setIfNotEquals(UpdateState.Loading)

        timer = Timer()

        val apkName = newUpdate.versionName

        val destination = requireContext()
            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/$apkName.apk"

        val file = File(destination)
        if (file.exists()) file.delete()

        val request = DownloadManager.Request(Uri.parse(newUpdate.downloadLink)).apply {
            setTitle("${getString(R.string.app_name)} ${apkName}.apk")
            setMimeType(AppConstants.INSTALL_APP_MIME_TYPE)
            setDestinationInExternalFilesDir(
                requireContext(),
                Environment.DIRECTORY_DOWNLOADS,
                "$apkName.apk"
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

            installUpdate(file)

            requireContext().unregisterReceiver(receiver)

            viewModel.updateState.setIfNotEquals(UpdateState.NewUpdate)
        }

        requireContext().registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        downloadId = AppGlobal.downloadManager.enqueue(request)

        viewModel.updateState.setIfNotEquals(UpdateState.Downloading)

        if (binding.loadingProgress.max != 100 * 100) {
            binding.loadingProgress.max = 100 * 100
        }

        timer?.schedule(object : TimerTask() { // for progress
            override fun run() {
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

                    val progress =
                        if (size != -1) (downloaded * 100.0F / size) else 0.0F

                    if (progress.toInt() >= 1) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (view == null) {
                                downloadId?.run { AppGlobal.downloadManager.remove(this) }
                                timer?.cancel()
                                return@launch
                            }
                            binding.loadingProgress.isIndeterminate = false

                            if (binding.loadingProgress.progress != progress.toInt()) {
                                ObjectAnimator.ofInt(
                                    binding.loadingProgress,
                                    "progress",
                                    binding.loadingProgress.progress,
                                    progress.toInt() * 100
                                ).apply {
                                    duration = 250
                                    setAutoCancel(true)
                                    interpolator = DecelerateInterpolator()
                                }.start()
                            }
                        }
                    }

                    Log.d("Downloading update", "progress $progress%")
                }
            }

        }, 0, 250)
    }

    private fun writeFileToStorage(responseBody: ResponseBody?) {
        if (responseBody == null) return

        val updateItem = requireNotNull(viewModel.currentItem.value)

        try {
            val destination = requireContext()
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() +
                    "${File.separator}${updateItem.fileName}.${updateItem.extension}"

            val file = File(destination)
            if (file.exists()) file.delete()

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize: Long = responseBody.contentLength()

                requireActivity().runOnUiThread {
                    binding.loadingProgress.max = fileSize.toInt()
                    binding.loadingProgress.progress = 0
                }

                var fileSizeDownloaded: Long = 0
                inputStream = responseBody.byteStream()
                outputStream = FileOutputStream(file)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outputStream.flush()

                requireActivity().runOnUiThread {
                    installUpdate(file)
                }
            } catch (e: IOException) {

            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {

        }
    }

    private fun installUpdate(file: File) {
        val installIntent = AndroidUtils.getInstallPackageIntent(
            requireContext(),
            ARG_PROVIDER_PATH,
            file
        )

        requireContext().startActivity(installIntent)
    }

    private fun showChangelogAlert() {
        val changelog = viewModel.currentItem.value?.changelog

        val messageText =
            if (changelog.isNullOrBlank()) getString(R.string.fragment_updates_changelog_none)
            else changelog

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.fragment_updates_changelog)
            .setMessage(messageText)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    companion object {
        private const val ARG_UPDATE_ITEM = "arg_update_item"
        private const val ARG_FILE_BASE_PATH = "file://"
        private const val ARG_PROVIDER_PATH = ".provider"

        fun newInstance(updateItem: UpdateItem? = null): UpdatesFragment {
            val fragment = UpdatesFragment()
            if (updateItem != null) {
                fragment.arguments = bundleOf(ARG_UPDATE_ITEM to updateItem)
            } else {
                fragment.arguments = Bundle()
            }

            return fragment
        }
    }
}