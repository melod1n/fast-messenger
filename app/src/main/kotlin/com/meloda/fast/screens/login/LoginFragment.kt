package com.meloda.fast.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.viewmodel.ViewModelUtils
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.databinding.DialogFastLoginBinding
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.trimmedText
import com.meloda.fast.ui.AppTheme
import com.meloda.fast.ui.widgets.TextFieldErrorText
import com.meloda.fast.util.ViewUtils.showDialog
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.meloda.fast.model.base.Text as UiText

class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by activityViewModel<LoginViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listenViewModel()

        (view as? ComposeView)?.apply {
            setContent {
                val showLogo by viewModel.isNeedToShowLogo.collectAsState()

                AppTheme {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        if (showLogo) {
                            LoginLogo()
                        } else {
                            LoginSignIn()
                        }
                    }
                }
            }
        }
    }

    private fun listenViewModel() = with(viewModel) {
        events.listenValue(::handleEvent)
        isNeedToShowErrorDialog.listenValue(::handleErrorAlertShow)
        isNeedToShowFastLoginDialog.listenValue(::handleFastLoginAlertShow)
    }

    private fun handleEvent(event: VkEvent) {
        ViewModelUtils.parseEvent(this, event)
    }

    private fun handleErrorAlertShow(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showErrorDialog()
        }
    }

    private fun handleFastLoginAlertShow(isNeedToShow: Boolean) {
        if (isNeedToShow) {
            showFastLoginDialog()
        }
    }

    private fun showErrorDialog() {
        context?.showDialog(
            title = UiText.Resource(R.string.title_error),
            message = UiText.Simple(viewModel.screenState.value.error.orEmpty()),
            positiveText = UiText.Resource(R.string.ok),
            onDismissAction = viewModel::onErrorDialogDismissed
        )
    }

    private fun showFastLoginDialog() {
        val dialogFastLoginBinding = DialogFastLoginBinding.inflate(layoutInflater, null, false)

        context?.showDialog(
            title = UiText.Resource(R.string.fast_login_title),
            view = dialogFastLoginBinding.root,
            positiveText = UiText.Resource(R.string.ok),
            positiveAction = {
                val text = dialogFastLoginBinding.fastLoginText.trimmedText
                if (text.isEmpty()) return@showDialog

                val split = text.split(";")
                try {
                    val login = split[0]
                    val password = split[1]

                    viewModel.onLoginInputChanged(login)
                    viewModel.onPasswordInputChanged(password)

                    viewModel.onFastLoginDialogOkButtonClicked()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onFastLoginDialogDismissed
        )
    }

    @Composable
    fun LoginLogo() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_big),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(46.dp))
                Text(
                    text = "Fast Messenger",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            FloatingActionButton(
                onClick = viewModel::onLogoNextButtonClicked,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_end),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginSignIn() {
        val focusManager = LocalFocusManager.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "Sign in to VK",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displayMedium
                )

                Spacer(modifier = Modifier.height(58.dp))

                val state by viewModel.screenState.collectAsState()

                var loginText by remember { mutableStateOf(TextFieldValue(state.login)) }
                val showLoginError by viewModel.isNeedToShowLoginError.collectAsState()

                TextField(
                    value = loginText,
                    onValueChange = { newText ->
                        loginText = newText
                        viewModel.onLoginInputChanged(newText.text)
                    },
                    label = { Text(text = "Login") },
                    placeholder = { Text(text = "Login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_person_24),
                            contentDescription = null,
                            tint = if (showLoginError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(),
                    isError = showLoginError,
                    singleLine = true
                )
                AnimatedVisibility(visible = showLoginError) {
                    TextFieldErrorText(text = "Field must not be empty")
                }

                Spacer(modifier = Modifier.height(16.dp))

                var passwordText by remember { mutableStateOf(TextFieldValue(state.password)) }
                val showPasswordError by viewModel.isNeedToShowPasswordError.collectAsState()
                val passwordVisible by viewModel.isPasswordVisible.collectAsState()

                TextField(
                    value = passwordText,
                    onValueChange = { newText ->
                        passwordText = newText
                        viewModel.onPasswordInputChanged(newText.text)
                    },
                    label = { Text(text = "Password") },
                    placeholder = { Text(text = "Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_vpn_key_24),
                            contentDescription = null,
                            tint = if (showPasswordError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    },
                    trailingIcon = {
                        val imagePainter = painterResource(
                            id = if (passwordVisible) R.drawable.round_visibility_off_24
                            else R.drawable.round_visibility_24
                        )

                        IconButton(
                            onClick = viewModel::onPasswordVisibilityButtonClicked
                        ) {
                            Icon(painter = imagePainter, contentDescription = null)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Go,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            focusManager.clearFocus()
                            viewModel.onSignInButtonClicked()
                        }
                    ),
                    isError = showPasswordError,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true
                )
                AnimatedVisibility(visible = showPasswordError) {
                    TextFieldErrorText(text = "Field must not be empty")
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(72.dp),
                contentAlignment = Alignment.Center
            ) {
                val isLoading by viewModel.isLoadingInProgress.collectAsState()

                FloatingActionButton(
                    onClick = {
                        if (!isLoading) {
                            viewModel.onSignInButtonClicked()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_end),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    companion object {

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
