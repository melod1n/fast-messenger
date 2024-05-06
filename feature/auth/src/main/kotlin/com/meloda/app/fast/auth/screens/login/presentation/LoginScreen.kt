package com.meloda.app.fast.auth.screens.login.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.auth.navigation.AuthGraph
import com.meloda.app.fast.auth.screens.login.LoginViewModel
import com.meloda.app.fast.auth.screens.login.LoginViewModelImpl
import com.meloda.app.fast.auth.screens.login.model.LoginError
import com.meloda.app.fast.auth.screens.login.model.LoginScreenState
import com.meloda.app.fast.auth.screens.login.model.UiAction
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.designsystem.TextFieldErrorText
import com.meloda.app.fast.designsystem.autoFillRequestHandler
import com.meloda.app.fast.designsystem.connectNode
import com.meloda.app.fast.designsystem.defaultFocusChangeAutoFill
import com.meloda.app.fast.designsystem.handleEnterKey
import com.meloda.app.fast.designsystem.handleTabKey
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.auth.destinations.CaptchaDestination
import com.ramcosta.composedestinations.generated.auth.destinations.TwofaDestination
import com.ramcosta.composedestinations.generated.conversations.destinations.ConversationsDestination
import com.ramcosta.composedestinations.generated.destinations.UserbannedDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import com.meloda.app.fast.designsystem.R as UiR

typealias OnAction = (UiAction) -> Unit


@Destination<AuthGraph>(route = "login")
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>(),
) {
    LoginScreenContent(
        onAction = { action ->
            when (action) {
                is UiAction.NavigateToCaptcha -> {
                    navigator.navigate(
                        CaptchaDestination(
                            captchaSid = action.arguments.captchaSid,
                            captchaImage = action.arguments.captchaImage
                        )
                    )
                }

                UiAction.NavigateToConversations -> {
                    navigator.navigate(ConversationsDestination(null)) {
                        popUpTo("logo") { inclusive = true }
                    }
                }

                is UiAction.NavigateToTwoFa -> {
                    navigator.navigate(
                        TwofaDestination(
                            validationSid = action.arguments.validationSid,
                            redirectUri = action.arguments.redirectUri,
                            phoneMask = action.arguments.phoneMask,
                            validationType = action.arguments.validationType,
                            canResendSms = action.arguments.canResendSms,
                            wrongCodeError = action.arguments.wrongCodeError
                        )
                    )
                }

                is UiAction.NavigateToUserBanned -> {
                    navigator.navigate(
                        UserbannedDestination(
                            name = action.arguments.name,
                            message = action.arguments.message,
                            isLastScreenLogin = true
                        )
                    ) {
                        popUpTo("logo") { inclusive = true }
                    }
                }

                is UiAction.LoginInputChanged -> {
                    viewModel.onLoginInputChanged(action.newText)
                }

                is UiAction.PasswordInputChanged -> {
                    viewModel.onPasswordInputChanged(action.newText)
                }

                UiAction.PasswordVisibilityClicked -> {
                    viewModel.onPasswordVisibilityButtonClicked()
                }

                UiAction.SignInClicked -> {
                    viewModel.onSignInButtonClicked()
                }
            }
        },
        viewModel = viewModel
    )
}

@Composable
fun LoginScreenContent(
    onAction: OnAction,
    viewModel: LoginViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.isNeedToOpenUserBanned) {
        viewModel.onNavigatedToUserBanned()

        screenState.userBannedArguments?.let { arguments ->
            onAction(UiAction.NavigateToUserBanned(arguments))
        }
    }

    if (screenState.isNeedToOpenConversations) {
        viewModel.onNavigatedToConversations()

        onAction(UiAction.NavigateToConversations)
    }

    if (screenState.isNeedToOpenCaptcha) {
        screenState.captchaArguments?.let { arguments ->
            onAction(UiAction.NavigateToCaptcha(arguments))
        }
    }

    if (screenState.isNeedToOpenTwoFa) {
        screenState.twoFaArguments?.let { arguments ->
            onAction(UiAction.NavigateToTwoFa(arguments))
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LoginSignIn(
                onAction = {},
                screenState = screenState,
            )
        }
    }

    HandleError(
        onDismiss = viewModel::onErrorDialogDismissed,
        error = screenState.error
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginSignIn(
    onAction: OnAction,
    screenState: LoginScreenState
) {
    val focusManager = LocalFocusManager.current
    val (loginFocusable, passwordFocusable) = FocusRequester.createRefs()
    val isLoading = screenState.isLoading

    val goButtonClickAction = {
        if (!isLoading) {
            focusManager.clearFocus()
            onAction(UiAction.SignInClicked)
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
                text = stringResource(id = UiR.string.sign_in_to_vk),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(58.dp))

            var loginText by remember { mutableStateOf(TextFieldValue(screenState.login)) }
            val showLoginError = screenState.loginError

            val autoFillEmailHandler = autoFillRequestHandler(
                autofillTypes = listOf(AutofillType.EmailAddress),
                onFill = { value ->
                    loginText = TextFieldValue(text = value, selection = TextRange(value.length))
                    onAction(UiAction.LoginInputChanged(newText = value))
                }
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .handleEnterKey(loginFieldTabClick::invoke)
                    .handleTabKey(loginFieldTabClick::invoke)
                    .focusRequester(loginFocusable)
                    .connectNode(handler = autoFillEmailHandler)
                    .defaultFocusChangeAutoFill(handler = autoFillEmailHandler),
                value = loginText,
                onValueChange = { newText ->
                    val text = newText.text
                    if (text.isEmpty()) {
                        autoFillEmailHandler.requestVerifyManual()
                    }

                    loginText = newText
                    onAction(UiAction.LoginInputChanged(newText = text))
                },
                label = { Text(text = stringResource(id = UiR.string.login_hint)) },
                placeholder = { Text(text = stringResource(id = UiR.string.login_hint)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_round_person_24),
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
                TextFieldErrorText(text = stringResource(id = UiR.string.error_empty_field))
            }

            Spacer(modifier = Modifier.height(16.dp))

            var passwordText by remember { mutableStateOf(TextFieldValue(screenState.password)) }
            val showPasswordError = screenState.passwordError
            var passwordVisible = screenState.passwordVisible

            val autoFillPasswordHandler = autoFillRequestHandler(
                autofillTypes = listOf(AutofillType.Password),
                onFill = { value ->
                    passwordText = TextFieldValue(text = value, selection = TextRange(value.length))
                    onAction(UiAction.PasswordInputChanged(newText = value))
                }
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .handleEnterKey {
                        goButtonClickAction.invoke()
                        true
                    }
                    .focusRequester(passwordFocusable)
                    .connectNode(handler = autoFillPasswordHandler)
                    .defaultFocusChangeAutoFill(handler = autoFillPasswordHandler),
                value = passwordText,
                onValueChange = { newText ->
                    val text = newText.text
                    if (text.isEmpty()) {
                        autoFillPasswordHandler.requestVerifyManual()
                    }

                    passwordText = newText
                    onAction(UiAction.PasswordInputChanged(newText = text))
                },
                label = { Text(text = stringResource(id = UiR.string.password_login_hint)) },
                placeholder = { Text(text = stringResource(id = UiR.string.password_login_hint)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = UiR.drawable.round_vpn_key_24),
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
                        id = if (passwordVisible) UiR.drawable.round_visibility_off_24
                        else UiR.drawable.round_visibility_24
                    )

                    IconButton(
                        onClick = {
                            onAction(UiAction.PasswordVisibilityClicked)
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
                TextFieldErrorText(text = stringResource(id = UiR.string.error_empty_field))
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
                    painter = painterResource(id = UiR.drawable.ic_arrow_end),
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

@Composable
fun HandleError(
    onDismiss: () -> Unit,
    error: LoginError?,
) {
    when (error) {
        LoginError.WrongCredentials -> {
            MaterialDialog(
                onDismissAction = onDismiss,
                title = UiText.Simple("Error"),
                text = UiText.Simple("Wrong login or password"),
                confirmText = UiText.Resource(UiR.string.ok)
            )
        }

        null -> Unit
    }
}
