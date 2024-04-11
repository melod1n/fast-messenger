package com.meloda.fast.screens.login.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.ext.handleEnterKey
import com.meloda.fast.ext.handleTabKey
import com.meloda.fast.screens.captcha.model.CaptchaArguments
import com.meloda.fast.screens.login.LoginViewModel
import com.meloda.fast.screens.login.LoginViewModelImpl
import com.meloda.fast.screens.login.model.LoginScreenState
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import com.meloda.fast.ui.widgets.TextFieldErrorText
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginRoute(
    restart: () -> Unit,
    navigateToTwoFa: (twoFaArguments: TwoFaArguments) -> Unit,
    navigateToCaptcha: (captchaArguments: CaptchaArguments) -> Unit,
    navigateToConversations: () -> Unit,
    viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    BackHandler(enabled = !screenState.isNeedToShowLogo) {
        viewModel.onBackPressed()
    }

    LoginScreenContent(
        restart = restart,
        navigateToTwoFa = navigateToTwoFa,
        navigateToCaptcha = navigateToCaptcha,
        navigateToConversations = navigateToConversations,
        screenState = screenState,
        viewModel = viewModel
    )
}

@Composable
fun LoginScreenContent(
    restart: () -> Unit,
    navigateToTwoFa: (twoFaArguments: TwoFaArguments) -> Unit,
    navigateToCaptcha: (captchaArguments: CaptchaArguments) -> Unit,
    navigateToConversations: () -> Unit,
    screenState: LoginScreenState,
    viewModel: LoginViewModel,
) {
    if (screenState.isNeedToRestart) {
        viewModel.onRestarted()
        restart()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (screenState.isNeedToShowLogo) {
                LoginLogo(viewModel)
            } else {
                if (screenState.isNeedToOpenConversations) {
                    viewModel.onNavigatedToConversations()

                    navigateToConversations()
                }

                if (screenState.isNeedToOpenCaptcha) {
                    viewModel.onNavigatedToCaptcha()

                    val captchaArguments = screenState.captchaArguments ?: return@Box
                    navigateToCaptcha(captchaArguments)
                }

                if (screenState.isNeedToOpenTwoFa) {
                    viewModel.onNavigatedToTwoFa()

                    val twoFaArguments = screenState.twoFaArguments ?: return@Box
                    navigateToTwoFa(twoFaArguments)
                }

                LoginSignIn(
                    onSignInClick = viewModel::onSignInButtonClicked,
                    onLoginInputChanged = viewModel::onLoginInputChanged,
                    onPasswordInputChanged = viewModel::onPasswordInputChanged,
                    onPasswordVisibilityButtonClicked = viewModel::onPasswordVisibilityButtonClicked,
                    screenState = screenState,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginLogo(viewModel: LoginViewModel) {
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
                contentDescription = "Application Logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = viewModel::onLogoLongClicked,
                    onClick = {}
                )
            )
            Spacer(modifier = Modifier.height(46.dp))
            Text(
                text = stringResource(id = R.string.fast_messenger),
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
                contentDescription = "Go button",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginSignIn(
    onSignInClick: () -> Unit,
    onLoginInputChanged: (String) -> Unit,
    onPasswordInputChanged: (String) -> Unit,
    onPasswordVisibilityButtonClicked: () -> Unit,
    screenState: LoginScreenState
) {
    val focusManager = LocalFocusManager.current
    val (loginFocusable, passwordFocusable) = FocusRequester.createRefs()
    val isLoading = screenState.isLoading

    val goButtonClickAction = {
        if (!isLoading) {
            focusManager.clearFocus()
            onSignInClick.invoke()
        }
    }
    val loginFieldTabClick = {
        passwordFocusable.requestFocus()
        true
    }

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
                text = stringResource(id = R.string.sign_in_to_vk),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(58.dp))

            var loginText by remember { mutableStateOf(TextFieldValue(screenState.login)) }
            val showLoginError = screenState.loginError

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .handleEnterKey(loginFieldTabClick::invoke)
                    .handleTabKey(loginFieldTabClick::invoke)
                    .focusRequester(loginFocusable),
                value = loginText,
                onValueChange = { newText ->
                    loginText = newText
                    onLoginInputChanged.invoke(newText.text)
                },
                label = { Text(text = stringResource(id = R.string.login_hint)) },
                placeholder = { Text(text = stringResource(id = R.string.login_hint)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_person_24),
                        contentDescription = "Login icon",
                        tint = if (showLoginError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                },
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                keyboardActions = KeyboardActions(onNext = { passwordFocusable.requestFocus() }),
                isError = showLoginError,
                singleLine = true
            )
            AnimatedVisibility(visible = showLoginError) {
                TextFieldErrorText(text = stringResource(id = R.string.error_empty_field))
            }

            Spacer(modifier = Modifier.height(16.dp))

            var passwordText by remember { mutableStateOf(TextFieldValue(screenState.password)) }
            val showPasswordError = screenState.passwordError
            var passwordVisible = screenState.passwordVisible

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .handleEnterKey {
                        goButtonClickAction.invoke()
                        true
                    }
                    .focusRequester(passwordFocusable),
                value = passwordText,
                onValueChange = { newText ->
                    passwordText = newText
                    onPasswordInputChanged.invoke(newText.text)
                },
                label = { Text(text = stringResource(id = R.string.password_login_hint)) },
                placeholder = { Text(text = stringResource(id = R.string.password_login_hint)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_vpn_key_24),
                        contentDescription = "Password icon",
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
                        onClick = {
                            onPasswordVisibilityButtonClicked.invoke()
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            painter = imagePainter,
                            contentDescription = if (passwordVisible) "Password visible icon"
                            else "Password invisible icon"
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Go,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onGo = { goButtonClickAction.invoke() }
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
                TextFieldErrorText(text = stringResource(id = R.string.error_empty_field))
            }
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {

            FloatingActionButton(
                onClick = goButtonClickAction::invoke,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.testTag("Sign in button")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_end),
                    contentDescription = "Sign in icon",
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
