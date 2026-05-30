package dev.meloda.fast.auth.captcha.presentation

import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.common.LocalLogger
import dev.meloda.fast.ui.components.ActionInvokeDismiss
import dev.meloda.fast.ui.components.FullScreenDialog
import dev.meloda.fast.ui.components.MaterialDialog
import org.json.JSONObject

private const val TAG = "CaptchaScreen"

@Composable
fun CaptchaScreen(
    captchaRedirectUri: String?,
    onBack: () -> Unit = {},
    onResult: (String) -> Unit = {}
) {
    val logger = LocalLogger.current

    if (captchaRedirectUri != null) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(true) {
            focusManager.clearFocus(true)
            keyboardController?.hide()
        }

        var confirmedExit by remember {
            mutableStateOf(false)
        }

        var showExitAlert by rememberSaveable {
            mutableStateOf(false)
        }

        var isWebViewLoading by remember {
            mutableStateOf(true)
        }

        LaunchedEffect(confirmedExit) {
            if (confirmedExit) {
                onBack()
            }
        }

        BackHandler(enabled = !confirmedExit) {
            if (!confirmedExit) {
                showExitAlert = true
            }
        }

        FullScreenDialog(onDismiss = { showExitAlert = true }) {
            if (showExitAlert) {
                MaterialDialog(
                    onDismissRequest = { showExitAlert = false },
                    title = stringResource(id = R.string.warning_confirmation),
                    text = stringResource(id = R.string.captcha_exit_warning),
                    confirmAction = { confirmedExit = true },
                    confirmText = stringResource(id = R.string.yes),
                    cancelText = stringResource(id = R.string.no),
                    actionInvokeDismiss = ActionInvokeDismiss.Always
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showExitAlert = true }
                    )
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                    factory = { context ->
                        val webview = WebView(context)
                        webview.setBackgroundColor(0)
                        webview.settings.javaScriptEnabled = true
                        webview.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                logger.info(
                                    "CaptchaScreen",
                                    "WebViewClient(): shouldOverrideUrlLoading(): request: $request"
                                )
                                return false
                            }

                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                super.onPageStarted(view, url, favicon)
                                isWebViewLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isWebViewLoading = false
                            }
                        }
                        webview.addJavascriptInterface(
                            WebCaptchaListener(
                                onSuccessTokenReceived = {
                                    val response: String? = try {
                                        JSONObject(it).getString("token")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        null
                                    }

                                    if (response != null) {
                                        onResult(response)
                                    } else {
                                        // TODO: 03/05/2026, Danil Nikolaev: show error
                                    }
                                },
                                onCloseRequested = { showExitAlert = true },
                                logger = logger
                            ),
                            "AndroidBridge"
                        )
//                        webview.loadUrl("https://id.vk.ru/not_robot_captcha?variant=block&session_token=test&domain=test.com")
                        webview.loadUrl(captchaRedirectUri)
                        webview
                    }
                )

                AnimatedVisibility(
                    visible = isWebViewLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

class WebCaptchaListener(
    private val onSuccessTokenReceived: (String) -> Unit,
    private val onCloseRequested: (String) -> Unit,
    private val logger: FastLogger
) {
    @JavascriptInterface
    fun VKCaptchaGetResult(arg: String) {
        onSuccessTokenReceived(arg)
        logger.info(this::class, "VKCaptchaGetResult(): arg: $arg")
    }

    @JavascriptInterface
    fun VKCaptchaCloseCaptcha(arg: String) {
        onCloseRequested(arg)
        logger.info(this::class, "VKCaptchaCloseCaptcha(): arg: $arg")
    }
}
