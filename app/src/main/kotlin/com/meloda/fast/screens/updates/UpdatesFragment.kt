package com.meloda.fast.screens.updates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.showDialog
import com.meloda.fast.ext.string
import com.meloda.fast.model.UpdateItem
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.updates.model.UpdateState
import com.meloda.fast.ui.AppTheme
import com.meloda.fast.util.AndroidUtils
import okhttp3.ResponseBody
import org.koin.androidx.viewmodel.ext.android.viewModel


class UpdatesFragment : BaseFragment(R.layout.fragment_updates) {

    private val viewModel: UpdatesViewModel by viewModel<UpdatesViewModelImpl>()

    private val changelogPlaceholder by lazy {
        string(R.string.fragment_updates_changelog_none)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                UpdatesScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UpdatesScreen() {
        AppTheme {
            val state by viewModel.screenState.collectAsState()
            val updateState = state.updateState
            val downloadProgress by viewModel.currentDownloadProgress.collectAsState()
            val animatedProgress by animateFloatAsState(
                targetValue = downloadProgress / 100f,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )

            Scaffold(topBar = { Toolbar() }) { paddingValues ->
                Surface(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when {
                            updateState.isLoading() -> CircularProgressIndicator()
                            updateState.isDownloading() -> {
                                Text(
                                    text = getString(R.string.fragment_updates_downloading_update),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                if (animatedProgress > 0) {
                                    LinearProgressIndicator(progress = animatedProgress)
                                } else {
                                    LinearProgressIndicator()
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                FilledTonalButton(onClick = viewModel::onCancelDownloadButtonClicked) {
                                    Text(text = getString(R.string.action_stop))
                                }
                            }

                            else -> {
                                getTitle(updateState)?.let { title ->
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                getSubtitle(updateState)?.let { subtitle ->
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                state.updateItem?.changelog?.let {
                                    Text(
                                        text = getString(R.string.fragment_updates_changelog),
                                        style = TextStyle(textDecoration = TextDecoration.Underline),
                                        modifier = Modifier.clickable(onClick = viewModel::onChangelogButtonClicked)
                                    )
                                }

                                getActionButtonText(updateState)?.let { buttonText ->
                                    Spacer(modifier = Modifier.height(24.dp))
                                    ExtendedFloatingActionButton(
                                        onClick = viewModel::onActionButtonClicked,
                                        modifier = Modifier,
                                        text = { Text(text = buttonText) },
                                        icon = {
                                            getActionButtonIcon(state = updateState)?.let { painter ->
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(painter = painter, contentDescription = null)
                                            }
                                        }
                                    )
                                }

                                if (updateState.isDownloaded()) {
                                    Spacer(modifier = Modifier.height(48.dp))
                                    Text(
                                        text = getString(R.string.fragment_updates_issues_installing),
                                        style = TextStyle(textDecoration = TextDecoration.Underline),
                                        modifier = Modifier.clickable(onClick = viewModel::onIssuesButtonClicked),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getTitle(state: UpdateState): String? {
        return when (state) {
            UpdateState.Error -> R.string.error_occurred
            UpdateState.NewUpdate -> R.string.fragment_updates_new_version
            UpdateState.NoUpdates -> R.string.fragment_updates_no_updates
            UpdateState.Downloaded -> R.string.fragment_updates_downloaded
            else -> null
        }?.let(requireContext()::getString)
    }

    private fun getSubtitle(state: UpdateState): String? {
        return when (state) {
            UpdateState.Error -> {
                viewModel.screenState.value.error?.let { error ->
                    if (error.contains("cannot be converted", ignoreCase = true)
                        || error.contains("begin_object", ignoreCase = true)
                    ) {
                        "OTA Server is unavailable"
                    } else {
                        string(R.string.error_occurred_description, error)
                    }
                }
            }

            UpdateState.NewUpdate, UpdateState.Downloaded -> {
                viewModel.screenState.value.updateItem?.let { item ->
                    string(
                        R.string.fragment_updates_new_version_description, item.versionName
                    )
                }
            }

            UpdateState.NoUpdates -> string(R.string.fragment_updates_no_updates_description)
            else -> null
        }
    }

    private fun getActionButtonText(state: UpdateState): String? {
        return when (state) {
            UpdateState.Error -> R.string.fragment_updates_try_again
            UpdateState.NewUpdate -> R.string.fragment_updates_download_update
            UpdateState.NoUpdates -> R.string.fragment_updates_check_updates
            UpdateState.Downloaded -> R.string.fragment_updates_install
            else -> null
        }?.let(requireContext()::getString)
    }

    @Composable
    private fun getActionButtonIcon(state: UpdateState): Painter? {
        return when (state) {
            UpdateState.Error -> R.drawable.round_restart_alt_24
            UpdateState.NewUpdate -> R.drawable.round_file_download_24
            UpdateState.Downloaded -> R.drawable.round_install_mobile_24
            else -> null
        }?.let { painterResource(id = it) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Toolbar() {
        TopAppBar(
            title = { Text(text = "Application updates") },
            navigationIcon = {
                IconButton(
                    onClick = { requireActivity().onBackPressedDispatcher.onBackPressed() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun listenViewModel() = with(viewModel) {
        isNeedToShowChangelogAlert.listenValue(::handleNeedToShowChangelogAlert)
        isNeedToShowUnknownSourcesAlert.listenValue(::handleNeedToShowUnknownSourcesAlert)
        isNeedToShowIssuesAlert.listenValue(::handleNeedToShowIssuesAlert)
        isNeedToShowFileNotFoundAlert.listenValue(::handleNeedToShowFileNotFoundAlert)
    }

    private fun handleNeedToShowChangelogAlert(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showChangelogAlert()
        }
    }

    private fun handleNeedToShowUnknownSourcesAlert(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showUnknownSourcesAlert()
        }
    }

    private fun handleNeedToShowIssuesAlert(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showIssuesAlert()
        }
    }

    private fun handleNeedToShowFileNotFoundAlert(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showFileNotFoundAlert()
        }
    }

    private fun showUnknownSourcesAlert() {
        context?.showDialog(
            title = UiText.Resource(R.string.warning),
            message = UiText.Resource(R.string.fragment_updates_unknown_sources_disabled_message),
            positiveText = UiText.Resource(R.string.yes),
            positiveAction = { AndroidUtils.openInstallUnknownAppsScreen(requireContext()) },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onUnknownSourcesAlertDismissed,
            isCancelable = false
        )
    }

    private fun showChangelogAlert() {
        val messageText =
            viewModel.screenState.value.updateItem?.changelog?.ifBlank {
                changelogPlaceholder
            } ?: changelogPlaceholder

        context?.showDialog(
            title = UiText.Resource(R.string.fragment_updates_changelog),
            message = UiText.Simple(messageText),
            positiveText = UiText.Resource(R.string.ok),
            onDismissAction = viewModel::onChangelogAlertDismissed
        )
    }

    private fun showIssuesAlert() {
        context?.showDialog(
            message = UiText.Resource(R.string.fragment_updates_issues_description),
            positiveText = UiText.Resource(R.string.action_delete),
            positiveAction = viewModel::onIssuesAlertPositiveButtonClicked,
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onIssuesAlertDismissed
        )
    }

    private fun showFileNotFoundAlert() {
        context?.showDialog(
            title = UiText.Resource(R.string.warning),
            message = UiText.Resource(R.string.fragment_updates_file_not_found_description),
            positiveText = UiText.Resource(R.string.ok),
            onDismissAction = viewModel::onFileNotFoundAlertDismissed,
            isCancelable = false
        )
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
