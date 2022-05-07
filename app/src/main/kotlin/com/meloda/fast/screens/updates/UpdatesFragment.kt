package com.meloda.fast.screens.updates

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.common.AppConstants
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.databinding.FragmentUpdatesBinding
import com.meloda.fast.extensions.clear
import com.meloda.fast.extensions.setIfNotEquals
import com.meloda.fast.extensions.toggleVisibility
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.receiver.DownloadManagerReceiver
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class UpdatesFragment : BaseViewModelFragment<UpdatesViewModel>(R.layout.fragment_updates) {

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

    override val viewModel: UpdatesViewModel by viewModels()

    private val binding: FragmentUpdatesBinding by viewBinding()

    private var downloadId: Long = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UpdateManager.newUpdate.observe(viewLifecycleOwner) { item ->
            viewModel.currentItem.setIfNotEquals(item)
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            state?.run { refreshState(this) }
        }

        if (requireArguments().containsKey(ARG_UPDATE_ITEM)) {
            val updateItem: UpdateItem = requireArguments().getParcelable(ARG_UPDATE_ITEM) ?: return
            viewModel.currentItem.setIfNotEquals(updateItem)
            viewModel.updateState.setIfNotEquals(UpdateState.NewUpdate)
        } else {
            viewModel.checkUpdates()
        }

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        binding.changelog.setOnClickListener {
            showChangelogAlert()
        }
    }

    private fun refreshState(state: UpdateState) {
        binding.actionButton.toggleVisibility(
            viewModel.updateState.value != UpdateState.Loading
        )
        binding.flow.toggleVisibility(
            viewModel.updateState.value != UpdateState.Loading
        )
        binding.progress.toggleVisibility(
            viewModel.updateState.value == UpdateState.Loading
        )
        binding.changelog.toggleVisibility(
            viewModel.updateState.value == UpdateState.NewUpdate
        )

        when (state) {
            UpdateState.NewUpdate -> {
                val item = viewModel.currentItem.value ?: return
                binding.title.setText(R.string.fragment_updates_new_version)

                binding.description.text = getString(
                    R.string.fragment_updates_new_version_description,
                    item.version
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
                binding.description.text = getString(
                    R.string.fragment_updates_error_occurred_description, error
                )

                binding.actionButton.setText(R.string.fragment_updates_try_again)
                binding.actionButton.setOnClickListener { viewModel.checkUpdates() }
            }
        }
    }

    private fun checkIsInstallingAllowed(item: UpdateItem) {
        if (!AndroidUtils.isCanInstallUnknownApps(requireContext())) {
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

//        val timer = Timer()

        val apkName = newUpdate.version

        val destination = requireContext()
            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/$apkName.apk"

        val file = File(destination)
        if (file.exists()) file.delete()

        val request = DownloadManager.Request(Uri.parse(newUpdate.link)).apply {
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
//            timer.cancel()

            installUpdate(file)

            requireContext().unregisterReceiver(receiver)

            viewModel.updateState.setIfNotEquals(UpdateState.NoUpdates)
        }

        requireContext().registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        val downloadManager: DownloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        downloadId = downloadManager.enqueue(request)

//        timer.schedule(object : TimerTask() { // for progress
//            override fun run() {
//                val query = DownloadManager.Query()
//                query.setFilterById(downloadId)
//
//                val cursor = downloadManager.query(query)
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
//                    Log.d("Downloading update", "progress $progress%")
//                }
//            }
//
//        }, 0, 1000)
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
        val version = viewModel.currentItem.value?.version
        val changelog = viewModel.currentItem.value?.changelogs?.get(version)

        val messageText =
            if (changelog.isNullOrBlank()) getString(R.string.fragment_updates_changelog_none)
            else changelog

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.fragment_updates_changelog)
            .setMessage(messageText)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}