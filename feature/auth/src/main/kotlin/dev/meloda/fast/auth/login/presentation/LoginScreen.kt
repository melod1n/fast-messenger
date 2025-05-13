package dev.meloda.fast.auth.login.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.auth.login.LoginViewModel
import dev.meloda.fast.auth.login.LoginViewModelImpl
import dev.meloda.fast.auth.login.model.CaptchaArguments
import dev.meloda.fast.auth.login.model.LoginDialog
import dev.meloda.fast.auth.login.model.LoginScreenState
import dev.meloda.fast.auth.login.model.LoginUserBannedArguments
import dev.meloda.fast.auth.login.model.LoginValidationArguments
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.components.TextFieldErrorText
import dev.meloda.fast.ui.theme.LocalSizeConfig
import dev.meloda.fast.ui.util.handleEnterKey
import dev.meloda.fast.ui.util.handleTabKey
import org.koin.androidx.compose.koinViewModel
import dev.meloda.fast.ui.R as UiR

@Composable
fun LoginRoute(
    onNavigateToUserBanned: (LoginUserBannedArguments) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToCaptcha: (CaptchaArguments) -> Unit,
    onNavigateToValidation: (LoginValidationArguments) -> Unit,
    validationCode: String?,
    captchaCode: String?,
    viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isNeedToOpenMain by viewModel.isNeedToOpenMain.collectAsStateWithLifecycle()
    val userBannedArguments by viewModel.userBannedArguments.collectAsStateWithLifecycle()
    val captchaArguments by viewModel.captchaArguments.collectAsStateWithLifecycle()
    val validationArguments by viewModel.validationArguments.collectAsStateWithLifecycle()
    val loginDialog by viewModel.loginDialog.collectAsStateWithLifecycle()

    BackHandler(
        enabled = !screenState.showLogo,
        onBack = viewModel::onBackPressed
    )

    LaunchedEffect(isNeedToOpenMain) {
        if (isNeedToOpenMain) {
            viewModel.onNavigatedToMain()
            onNavigateToMain()
        }

    }
    LaunchedEffect(userBannedArguments) {
        userBannedArguments?.let { arguments ->
            viewModel.onNavigatedToUserBanned()
            onNavigateToUserBanned(arguments)
        }
    }
    LaunchedEffect(captchaArguments) {
        captchaArguments?.let { arguments ->
            viewModel.onNavigatedToCaptcha()
            onNavigateToCaptcha(arguments)
        }
    }
    LaunchedEffect(validationArguments) {
        validationArguments?.let { arguments ->
            viewModel.onNavigatedToValidation()
            onNavigateToValidation(arguments)
        }
    }
    LaunchedEffect(validationCode) {
        viewModel.onValidationCodeReceived(validationCode)
    }
    LaunchedEffect(captchaCode) {
        viewModel.onCaptchaCodeReceived(captchaCode)
    }

    LoginScreen(
        screenState = screenState,
        onLoginInputChanged = viewModel::onLoginInputChanged,
        onPasswordInputChanged = viewModel::onPasswordInputChanged,
        onPasswordFieldEnterKeyClicked = viewModel::onSignInButtonClicked,
        onPasswordVisibilityButtonClicked = viewModel::onPasswordVisibilityButtonClicked,
        onPasswordFieldGoAction = viewModel::onSignInButtonClicked,
        onSignInButtonClicked = viewModel::onSignInButtonClicked,
        onLogoClicked = viewModel::onLogoClicked
    )

    HandleDialogs(
        loginDialog = loginDialog,
        onConfirmed = viewModel::onDialogConfirmed,
        onDismissed = viewModel::onDialogDismissed
    )
}

@Composable
fun LoginScreen(
    screenState: LoginScreenState = LoginScreenState.EMPTY,
    onLoginInputChanged: (String) -> Unit = {},
    onPasswordInputChanged: (String) -> Unit = {},
    onPasswordFieldEnterKeyClicked: () -> Unit = {},
    onPasswordVisibilityButtonClicked: () -> Unit = {},
    onPasswordFieldGoAction: () -> Unit = {},
    onSignInButtonClicked: () -> Unit = {},
    onLogoClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val size = LocalSizeConfig.current
    val focusManager = LocalFocusManager.current

    val titleSpacerSize by animateDpAsState(
        targetValue = if (size.isHeightSmall) 24.dp else 58.dp,
        label = "title spacer size"
    )
    val bottomPadding by animateDpAsState(
        targetValue = if (size.isHeightSmall) 10.dp else 30.dp,
        label = "bottom padding"
    )

    val (loginFocusable, passwordFocusable) =
        FocusRequester.createRefs()

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(top = 30.dp)
                .padding(horizontal = 30.dp)
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = screenState.showLogo,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "Logo visibility"
            ) {
                Logo(onLogoClicked = onLogoClicked)
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                visible = !screenState.showLogo,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "Login visibility"
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

                    Spacer(modifier = Modifier.height(titleSpacerSize))

                    TextField(
                        modifier = Modifier
                            .height(58.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .handleEnterKey {
                                passwordFocusable.requestFocus()
                                true
                            }
                            .handleTabKey {
                                passwordFocusable.requestFocus()
                                true
                            }
                            .focusRequester(loginFocusable)
                            .semantics {
                                contentType = ContentType.Username + ContentType.EmailAddress
                            },
                        value = screenState.login,
                        onValueChange = onLoginInputChanged,
                        label = { Text(text = stringResource(id = UiR.string.login_hint)) },
                        placeholder = { Text(text = stringResource(id = UiR.string.login_hint)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = UiR.drawable.ic_round_person_24),
                                contentDescription = "Login icon",
                                tint = if (screenState.loginError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusable.requestFocus() }),
                        isError = screenState.loginError,
                        singleLine = true
                    )
                    AnimatedVisibility(
                        visible = screenState.loginError,
                        label = "Login error visibility"
                    ) {
                        TextFieldErrorText(text = stringResource(id = UiR.string.error_empty_field))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        modifier = Modifier
                            .height(58.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .handleEnterKey {
                                focusManager.clearFocus()
                                onPasswordFieldEnterKeyClicked()
                                true
                            }
                            .focusRequester(passwordFocusable)
                            .semantics { contentType = ContentType.Password },
                        value = screenState.password,
                        onValueChange = onPasswordInputChanged,
                        label = { Text(text = stringResource(id = UiR.string.password_login_hint)) },
                        placeholder = { Text(text = stringResource(id = UiR.string.password_login_hint)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = UiR.drawable.round_vpn_key_24),
                                contentDescription = "Password icon",
                                tint = if (screenState.passwordError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        },
                        trailingIcon = {
                            val imagePainter = painterResource(
                                id = if (screenState.passwordVisible) UiR.drawable.round_visibility_off_24
                                else UiR.drawable.round_visibility_24
                            )

                            IconButton(onClick = onPasswordVisibilityButtonClicked) {
                                Icon(
                                    painter = imagePainter,
                                    contentDescription = if (screenState.passwordVisible) "Password visible icon"
                                    else "Password invisible icon"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Go,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                onPasswordFieldGoAction()
                            }
                        ),
                        isError = screenState.passwordError,
                        visualTransformation = if (screenState.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true
                    )
                    AnimatedVisibility(
                        visible = screenState.passwordError,
                        label = "Password error visibility"
                    ) {
                        TextFieldErrorText(text = stringResource(id = UiR.string.error_empty_field))
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!screenState.isLoading) {
                            focusManager.clearFocus()
                            onSignInButtonClicked()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("sing_in_fab")
                ) {
                    AnimatedVisibility(
                        visible = screenState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        label = "Progress indicator visibility"
                    ) {
                        CircularProgressIndicator()
                    }

                    AnimatedVisibility(
                        visible = !screenState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        label = "Sign in icon visibility"
                    ) {
                        Icon(
                            painter = painterResource(id = UiR.drawable.ic_arrow_end),
                            contentDescription = "Sign in icon",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                AnimatedVisibility(
                    visible = screenState.showLogo,
                    label = "Bottom padding visibility"
                ) {
                    Spacer(Modifier.height(bottomPadding))
                }

                AnimatedVisibility(
                    visible = !screenState.showLogo,
                    label = "Spacer between fab and bottom text buttons visibility"
                ) {
                    Spacer(Modifier.height(4.dp))
                }

                AnimatedVisibility(
                    visible = !screenState.showLogo,
                    label = "Text button row visibility"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, "https://vk.com/join".toUri())
                                )
                            }
                        ) {
                            Text(stringResource(UiR.string.login_sign_up))
                        }

                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, "https://vk.com/restore".toUri())
                                )
                            }
                        ) {
                            Text(stringResource(UiR.string.login_forgot_password))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HandleDialogs(
    loginDialog: LoginDialog?,
    onConfirmed: (LoginDialog, Bundle) -> Unit = { _, _ -> },
    onDismissed: (LoginDialog) -> Unit = {},
) {
    when (loginDialog) {
        null -> Unit

        is LoginDialog.Error -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(loginDialog) },
                title = stringResource(UiR.string.title_error),
                text = loginDialog.errorTextResId?.let { stringResource(it) }
                    ?: loginDialog.errorText
                    ?: stringResource(UiR.string.unknown_error_occurred),
                confirmText = stringResource(id = UiR.string.ok)
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        screenState = LoginScreenState.EMPTY.copy(
            showLogo = false
        )
    )
}
