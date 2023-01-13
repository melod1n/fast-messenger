package com.meloda.fast.screens.updates

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.FragmentUpdatesBinding
import com.meloda.fast.ext.clear
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.string
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.model.base.Text
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.updates.model.UpdatesScreenState
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.ViewUtils.showDialog
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import okhttp3.ResponseBody
import java.util.Timer

@AndroidEntryPoint
class UpdatesFragment : BaseFragment(R.layout.fragment_updates) {

    private val binding by viewBinding(FragmentUpdatesBinding::bind)

    val viewModel: IUpdatesViewModel by viewModels<UpdatesViewModel>()

    private var downloadId: Long? = null
    private var timer: Timer? = null
    private var changelog = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()
        listenViewModel()

        if (requireArguments().containsKey(ARG_UPDATE_ITEM)) {
            val updateItem: UpdateItem =
                requireArguments().getParcelableCompat(ARG_UPDATE_ITEM, UpdateItem::class.java)
                    ?: return

            viewModel.onUpdateItemExists(updateItem)
        } else {
            viewModel.checkUpdates()
        }
    }

    private fun prepareView() {
        applyInsets()
        prepareToolbar()
        prepareChangelog()
        prepareActionButton()
    }

    private fun applyInsets() {
        binding.appBar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.root.applyInsetter {
            type(navigationBars = true) { padding() }
        }
    }

    private fun prepareToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun prepareChangelog() {
        binding.changelog.setOnClickListener {
            showChangelogAlert()
        }
    }

    private fun prepareActionButton() {
        binding.actionButton.setOnClickListener {
            viewModel.onActionButtonClicked()
        }
    }

    private fun listenViewModel() {
        viewModel.screenStateFlow.listenValue(::refreshState)
        viewModel.isNeedToShowUnknownSourcesAlert.listenValue { needToShow ->
            if (needToShow) {
                showUnknownSourcesAlert()
            }
        }
        viewModel.isNeedToShowProgressBar.listenValue(binding.loadingProgress::toggleVisibility)
    }

    private fun showUnknownSourcesAlert() {
        context?.apply {
            showDialog(
                title = Text.Resource(R.string.warning),
                message = Text.Resource(R.string.fragment_updates_unknown_sources_disabled_message),
                positiveText = Text.Resource(R.string.yes),
                positiveAction = { AndroidUtils.openInstallUnknownAppsScreen(this) },
                negativeText = Text.Resource(R.string.no),
                onDismissAction = viewModel::onUnknownSourcesAlertDismissed
            )
        }
    }

    private fun refreshState(screenState: UpdatesScreenState) {
        val updateState = screenState.updateState

        binding.actionButton.toggleVisibility(
            !listOf(
                UpdateState.Downloading,
                UpdateState.Loading
            ).contains(updateState)
        )
        binding.flow.toggleVisibility(
            !listOf(
                UpdateState.Downloading,
                UpdateState.Loading
            ).contains(updateState)
        )
        binding.progress.toggleVisibility(
            updateState == UpdateState.Loading
        )
        binding.changelog.toggleVisibility(
            updateState == UpdateState.NewUpdate
        )
        binding.loadingProgress.toggleVisibility(
            updateState == UpdateState.Downloading
        )

        if (updateState != UpdateState.Downloading) {
            timer?.cancel()
            downloadId?.run { AppGlobal.downloadManager.remove(this) }
        }



        when (updateState) {
            UpdateState.NewUpdate -> {
                val item = screenState.updateItem ?: return
                binding.title.setText(R.string.fragment_updates_new_version)

                binding.description.text = getString(
                    R.string.fragment_updates_new_version_description,
                    item.versionName
                )

                binding.actionButton.setText(R.string.fragment_updates_download_update)

            }
            UpdateState.NoUpdates -> {
                binding.title.setText(R.string.fragment_updates_no_updates)
                binding.description.setText(R.string.fragment_updates_no_updates_description)

                binding.actionButton.setText(R.string.fragment_updates_check_updates)
            }
            UpdateState.Loading -> {
                binding.title.clear()
                binding.description.clear()
                binding.actionButton.clear()
            }
            UpdateState.Error -> {
                val error = screenState.error ?: return

                binding.title.setText(R.string.error_occurred)

                val errorText =
                    if (error.contains("cannot be converted", ignoreCase = true)
                        || error.contains("begin_object", ignoreCase = true)
                    ) {
                        "OTA Server is unavailable"
                    } else {
                        getString(R.string.error_occurred_description, error)
                    }

                binding.description.text = errorText

                binding.actionButton.setText(R.string.fragment_updates_try_again)
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

    private fun writeFileToStorage(responseBody: ResponseBody?) {
//        if (responseBody == null) return
//
//        val updateItem = requireNotNull(viewModel.currentItem.value)
//
//        try {
//            val destination = requireContext()
//                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() +
//                    "${File.separator}${updateItem.fileName}.${updateItem.extension}"
//
//            val file = File(destination)
//            if (file.exists()) file.delete()
//
//            var inputStream: InputStream? = null
//            var outputStream: OutputStream? = null
//            try {
//                val fileReader = ByteArray(4096)
//                val fileSize: Long = responseBody.contentLength()
//
//                requireActivity().runOnUiThread {
//                    binding.loadingProgress.max = fileSize.toInt()
//                    binding.loadingProgress.progress = 0
//                }
//
//                var fileSizeDownloaded: Long = 0
//                inputStream = responseBody.byteStream()
//                outputStream = FileOutputStream(file)
//                while (true) {
//                    val read: Int = inputStream.read(fileReader)
//                    if (read == -1) {
//                        break
//                    }
//                    outputStream.write(fileReader, 0, read)
//                    fileSizeDownloaded += read.toLong()
//                }
//                outputStream.flush()
//
//                requireActivity().runOnUiThread {
//                    installUpdate(file)
//                }
//            } catch (e: IOException) {
//
//            } finally {
//                inputStream?.close()
//                outputStream?.close()
//            }
//        } catch (e: IOException) {
//
//        }
    }

    private fun showChangelogAlert() {
        val messageText = changelog.ifBlank { string(R.string.fragment_updates_changelog_none) }

        context?.showDialog(
            title = Text.Resource(R.string.fragment_updates_changelog),
            message = Text.Simple(messageText),
            positiveText = Text.Resource(R.string.ok)
        )
    }

    companion object {
        private const val ARG_UPDATE_ITEM = "arg_update_item"
        private const val ARG_FILE_BASE_PATH = "file://"

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
