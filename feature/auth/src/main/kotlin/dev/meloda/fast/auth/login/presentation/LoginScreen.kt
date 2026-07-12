package dev.meloda.fast.auth.login.presentation

import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
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
import dev.meloda.fast.auth.login.model.LoginDialog
import dev.meloda.fast.auth.login.model.LoginIntent
import dev.meloda.fast.auth.login.model.LoginScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.common.LocalSizeConfig
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.components.TextFieldErrorText
import dev.meloda.fast.ui.util.handleEnterKey
import dev.meloda.fast.ui.util.handleTabKey

@Composable
fun LoginRoute(
    handleIntent: (LoginIntent) -> Unit,
    screenState: LoginScreenState
) {
    BackHandler(
        enabled = !screenState.showLogo,
        onBack = { handleIntent(LoginIntent.Back) }
    )

    LoginScreen(
        handleIntent = handleIntent,
        screenState = screenState
    )

    HandleDialogs(
        screenState = screenState,
        onDismissed = { handleIntent(LoginIntent.Dialog.Dismiss) }
    )
}

@Composable
fun LoginScreen(
    handleIntent: (LoginIntent) -> Unit,
    screenState: LoginScreenState
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
                Logo(
                    onLogoClicked = { handleIntent(LoginIntent.LogoClicked) },
                    onLogoLongClicked = { handleIntent(LoginIntent.LogoLongClicked) }
                )
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
                        text = stringResource(id = R.string.sign_in_to_vk),
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
                        onValueChange = { handleIntent(LoginIntent.LoginInputChange(it)) },
                        label = { Text(text = stringResource(id = R.string.login_hint)) },
                        placeholder = { Text(text = stringResource(id = R.string.login_hint)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person_round_24),
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
                        TextFieldErrorText(text = stringResource(id = R.string.error_empty_field))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        modifier = Modifier
                            .height(58.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .handleEnterKey {
                                focusManager.clearFocus()
                                handleIntent(LoginIntent.PasswordFieldEnterKeyClick)
                                true
                            }
                            .focusRequester(passwordFocusable)
                            .semantics { contentType = ContentType.Password },
                        value = screenState.password,
                        onValueChange = { handleIntent(LoginIntent.PasswordInputChange(it)) },
                        label = { Text(text = stringResource(id = R.string.password_login_hint)) },
                        placeholder = { Text(text = stringResource(id = R.string.password_login_hint)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_vpn_key_round_24),
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
                                id = if (screenState.passwordVisible) R.drawable.ic_visibility_off_round_24
                                else R.drawable.ic_visibility_round_24
                            )

                            IconButton(onClick = { handleIntent(LoginIntent.PasswordVisibilityButtonClick) }) {
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
                                handleIntent(LoginIntent.PasswordFieldGoKeyClick)
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
                        TextFieldErrorText(text = stringResource(id = R.string.error_empty_field))
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
                            handleIntent(LoginIntent.SignInButtonClick)
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
                            painter = painterResource(id = R.drawable.ic_arrow_forward_round_24),
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
                                    Intent(Intent.ACTION_VIEW, "https://vk.ru/join".toUri())
                                )
                            }
                        ) {
                            Text(stringResource(R.string.login_sign_up))
                        }

                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, "https://vk.ru/restore".toUri())
                                )
                            }
                        ) {
                            Text(stringResource(R.string.login_forgot_password))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HandleDialogs(
    screenState: LoginScreenState,
    onDismissed: (LoginDialog) -> Unit = {},
) {
    when (val dialog = screenState.dialog) {
        null -> Unit

        is LoginDialog.Error -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(R.string.title_error),
                text = dialog.errorTextResId?.let { stringResource(it) }
                    ?: dialog.errorText
                    ?: stringResource(R.string.unknown_error_occurred),
                confirmText = stringResource(id = R.string.ok)
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        handleIntent = {},
        screenState = LoginScreenState.EMPTY.copy(
            showLogo = false
        )
    )
}
