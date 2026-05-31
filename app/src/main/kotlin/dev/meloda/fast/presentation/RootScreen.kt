package dev.meloda.fast.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.conena.nanokt.android.content.pxToDp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.meloda.fast.MainViewModel
import dev.meloda.fast.auth.authNavGraph
import dev.meloda.fast.auth.captcha.presentation.CaptchaScreen
import dev.meloda.fast.auth.navigateToAuth
import dev.meloda.fast.chatmaterials.navigation.chatMaterialsScreen
import dev.meloda.fast.chatmaterials.navigation.navigateToChatMaterials
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.NetworkStateListener
import dev.meloda.fast.common.model.LongPollState
import dev.meloda.fast.convos.navigation.createChatScreen
import dev.meloda.fast.convos.navigation.navigateToCreateChat
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.CaptchaTokenResult
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.languagepicker.navigation.languagePickerScreen
import dev.meloda.fast.languagepicker.navigation.navigateToLanguagePicker
import dev.meloda.fast.messageshistory.navigation.messagesHistoryScreen
import dev.meloda.fast.messageshistory.navigation.navigateToMessagesHistory
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.navigation.Main
import dev.meloda.fast.navigation.mainScreen
import dev.meloda.fast.photoviewer.presentation.PhotoViewDialog
import dev.meloda.fast.settings.model.SettingsNavigationIntent
import dev.meloda.fast.settings.navigation.navigateToSettings
import dev.meloda.fast.settings.navigation.settingsScreen
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.common.LocalLogger
import dev.meloda.fast.ui.common.LocalNetworkState
import dev.meloda.fast.ui.common.LocalSizeConfig
import dev.meloda.fast.ui.model.DeviceSize
import dev.meloda.fast.ui.model.SizeConfig
import dev.meloda.fast.ui.model.ThemeConfig
import dev.meloda.fast.ui.theme.AppTheme
import dev.meloda.fast.ui.theme.LocalNavController
import dev.meloda.fast.ui.theme.LocalNavRootController
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.theme.LocalUser
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList
import dev.meloda.fast.ui.util.isNeedToEnableDarkMode
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RootScreen(
    toggleLongPollService: (enable: Boolean, inBackground: Boolean?) -> Unit,
    toggleOnlineService: (enable: Boolean) -> Unit
) {
    val logger = LocalLogger.current
    val resources = LocalResources.current

    val userSettings: UserSettings = koinInject()
    val longPollController: LongPollController = koinInject()

    val longPollCurrentState by longPollController.currentState.collectAsStateWithLifecycle()
    val longPollStateToApply by longPollController.stateToApply.collectAsStateWithLifecycle()

    val viewModel: MainViewModel = koinViewModel()
    val currentUser: VkUser? by viewModel.currentUser.collectAsStateWithLifecycle()

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    val isNeedToCheckPermission by viewModel.isNeedToCheckNotificationsPermission.collectAsStateWithLifecycle()
    val isNeedToRequestPermission by viewModel.isNeedToRequestNotifications.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToCheckPermission) {
        if (isNeedToCheckPermission) {
            viewModel.onPermissionCheckStatus(permissionState.status)

            if (permissionState.status.isGranted) {
                if (longPollCurrentState == LongPollState.InApp) {
                    toggleLongPollService(false, null)
                }

                toggleLongPollService(true, true)
            }
        }
    }

    LaunchedEffect(isNeedToRequestPermission) {
        if (isNeedToRequestPermission) {
            viewModel.onPermissionsRequested()
            permissionState.launchPermissionRequest()
        }
    }

    LifecycleResumeEffect(longPollStateToApply) {
        logger.debug("RootScreen", "longPollStateToApply: $longPollStateToApply")
        if (longPollStateToApply != LongPollState.Background) {
            if (longPollStateToApply.isLaunched() && longPollCurrentState.isLaunched()
                && longPollCurrentState != longPollStateToApply
            ) {
                toggleLongPollService(false, null)
            }

            toggleLongPollService(
                longPollStateToApply.isLaunched(),
                longPollStateToApply == LongPollState.Background
            )
        }

        onPauseOrDispose {}
    }

    val sendOnline by userSettings.sendOnlineStatus.collectAsStateWithLifecycle()
    LifecycleResumeEffect(sendOnline) {
        toggleOnlineService(sendOnline)

        onPauseOrDispose {
            toggleOnlineService(false)
        }
    }

    val deviceWidthDp = remember(true) {
        resources.displayMetrics.widthPixels.pxToDp()
    }
    val deviceHeightDp = remember(true) {
        resources.displayMetrics.heightPixels.pxToDp()
    }

    val deviceWidthSize by remember(deviceWidthDp) {
        derivedStateOf {
            when {
                deviceWidthDp <= 360 -> DeviceSize.Small
                deviceWidthDp <= 600 -> DeviceSize.Compact
                deviceWidthDp <= 840 -> DeviceSize.Medium
                else -> DeviceSize.Expanded
            }
        }
    }

    val deviceHeightSize by remember(deviceHeightDp) {
        derivedStateOf {
            when {
                deviceHeightDp <= 480 -> DeviceSize.Small
                deviceHeightDp <= 700 -> DeviceSize.Compact
                deviceHeightDp <= 900 -> DeviceSize.Medium
                else -> DeviceSize.Expanded
            }
        }
    }

    val sizeConfig by remember(deviceWidthSize, deviceHeightSize) {
        mutableStateOf(
            SizeConfig(
                widthSize = deviceWidthSize,
                heightSize = deviceHeightSize
            )
        )
    }

    val darkMode by userSettings.darkMode.collectAsStateWithLifecycle()
    val dynamicColors by userSettings.enableDynamicColors.collectAsStateWithLifecycle()
    val amoledDark by userSettings.enableAmoledDark.collectAsStateWithLifecycle()
    val enableBlur by userSettings.useBlur.collectAsStateWithLifecycle()
    val enableMultiline by userSettings.enableMultiline.collectAsStateWithLifecycle()
    val useSystemFont by userSettings.useSystemFont.collectAsStateWithLifecycle()
    val enableAnimations by userSettings.enableAnimations.collectAsStateWithLifecycle()

    val setDarkMode = isNeedToEnableDarkMode(darkMode = darkMode)

    val themeConfig by remember(
        darkMode,
        dynamicColors,
        amoledDark,
        enableBlur,
        enableMultiline,
        setDarkMode,
        useSystemFont
    ) {
        derivedStateOf {
            ThemeConfig(
                darkMode = setDarkMode,
                dynamicColors = dynamicColors,
                selectedColorScheme = 0,
                amoledDark = amoledDark,
                enableBlur = enableBlur,
                enableMultiline = enableMultiline,
                useSystemFont = useSystemFont,
                enableAnimations = enableAnimations
            )
        }
    }

    val networkStateListener: NetworkStateListener = koinInject()
    val networkState by networkStateListener.networkStateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(networkState) {
        logger.debug("RootScreen", "NetworkState: $networkState")
    }

    CompositionLocalProvider(
        LocalThemeConfig provides themeConfig,
        LocalSizeConfig provides sizeConfig,
        LocalUser provides currentUser,
        LocalNetworkState provides networkState
    ) {
        AppTheme(
            useDarkTheme = themeConfig.darkMode,
            useDynamicColors = themeConfig.dynamicColors,
            selectedColorScheme = themeConfig.selectedColorScheme,
            useAmoledBackground = themeConfig.amoledDark,
            useSystemFont = themeConfig.useSystemFont
        ) {
            val navController: NavHostController = rememberNavController()
            val activity = LocalActivity.current
            val context = LocalContext.current
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
            val isNeedToOpenAuth by viewModel.isNeedToReplaceWithAuth.collectAsStateWithLifecycle()
            val isNeedToShowDeniedDialog by viewModel.isNeedToShowNotificationsDeniedDialog.collectAsStateWithLifecycle()
            val isNeedToShowRationaleDialog by viewModel.isNeedToShowNotificationsRationaleDialog.collectAsStateWithLifecycle()

            LaunchedEffect(isNeedToOpenAuth) {
                if (isNeedToOpenAuth) {
                    viewModel.onNavigatedToAuth()
                    navController.navigateToAuth(clearBackStack = true)
                }
            }

            if (isNeedToShowDeniedDialog) {
                AlertDialog(
                    onDismissRequest = viewModel::onNotificationsDeniedDialogDismissed,
                    title = { Text(text = stringResource(id = R.string.warning)) },
                    text = { Text(text = stringResource(id = R.string.background_long_poll_denied_text)) },
                    confirmButton = {
                        TextButton(onClick = viewModel::onNotificationsDeniedDialogConfirmClicked) {
                            Text(text = stringResource(id = R.string.action_request))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onNotificationsDeniedDialogCancelClicked) {
                            Text(text = stringResource(id = R.string.action_disable))
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }

            if (isNeedToShowRationaleDialog) {
                AlertDialog(
                    onDismissRequest = viewModel::onNotificationsRationaleDialogDismissed,
                    title = { Text(text = stringResource(id = R.string.warning)) },
                    text = { Text(text = stringResource(id = R.string.background_long_poll_rationale_text)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                )
                            }
                        ) {
                            Text(text = stringResource(id = R.string.title_settings))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::onNotificationsRationaleDialogCancelClicked) {
                            Text(text = stringResource(id = R.string.action_disable))
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }

            if (startDestination != null) {
                CompositionLocalProvider(
                    LocalNavRootController provides navController,
                    LocalNavController provides navController
                ) {
                    var photoViewerInfo by rememberSaveable {
                        mutableStateOf<Pair<List<String>, Int?>?>(null)
                    }

                    val captchaRedirectUri by AppSettings.getCaptchaRedirectUriFlow()
                        .collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = requireNotNull(startDestination),
                            enterTransition = { fadeIn(animationSpec = tween(200)) },
                            exitTransition = { fadeOut(animationSpec = tween(200)) }
                        ) {
                            authNavGraph(
                                onNavigateToMain = {
                                    viewModel.onUserAuthenticated()
                                    navController.navigateToMain()
                                },
                                onNavigateToSettings = navController::navigateToSettings,
                                navController = navController
                            )

                            mainScreen(
                                onError = viewModel::onError,
                                onSettingsButtonClicked = navController::navigateToSettings,
                                onNavigateToMessagesHistory = navController::navigateToMessagesHistory,
                                onPhotoClicked = { url ->
                                    photoViewerInfo = listOf(url) to null
                                },
                                onMessageClicked = navController::navigateToMessagesHistory,
                                onNavigateToCreateChat = navController::navigateToCreateChat,
                            )

                            messagesHistoryScreen(
                                onError = viewModel::onError,
                                onBack = navController::navigateUp,
                                onNavigateToChatMaterials = navController::navigateToChatMaterials,
                                onNavigateToPhotoViewer = { photos, index ->
                                    photoViewerInfo = photos to index
                                }
                            )
                            chatMaterialsScreen(
                                onBack = navController::navigateUp,
                                onPhotoClicked = { url ->
                                    photoViewerInfo = listOf(url) to null
                                }
                            )
                            createChatScreen(
                                onChatCreated = { convoId ->
                                    navController.popBackStack()
                                    navController.navigateToMessagesHistory(convoId)
                                },
                                navController = navController
                            )

                            settingsScreen(
                                handleNavigationIntent = { intent ->
                                    when (intent) {
                                        SettingsNavigationIntent.Back -> navController.navigateUp()
                                        SettingsNavigationIntent.Language -> navController.navigateToLanguagePicker()
                                        SettingsNavigationIntent.Restart -> {
                                            activity?.let {
                                                val intent =
                                                    Intent(activity, MainActivity::class.java)
                                                intent.setFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                )
                                                activity.finish()
                                                activity.startActivity(intent)
                                            }
                                        }

                                        SettingsNavigationIntent.LogOut -> {
                                            navController.navigateToAuth(true)
                                        }
                                    }
                                }
                            )

                            languagePickerScreen(onBack = navController::navigateUp)
                        }

                        PhotoViewDialog(
                            photoViewerInfo = photoViewerInfo?.let { info ->
                                info.first.toImmutableList() to info.second
                            },
                            onDismiss = { photoViewerInfo = null }
                        )

                        CaptchaScreen(
                            captchaRedirectUri = captchaRedirectUri,
                            onBack = {
                                AppSettings.setCaptchaResult(CaptchaTokenResult.Cancelled)
                            },
                            onResult = { result ->
                                AppSettings.setCaptchaResult(
                                    CaptchaTokenResult.Success(result)
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

fun NavController.navigateToMain() {
    this.navigate(Main) {
        popUpTo(0) {
            inclusive = true
        }
    }
}
