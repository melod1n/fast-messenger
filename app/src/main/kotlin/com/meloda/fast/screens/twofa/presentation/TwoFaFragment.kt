package com.meloda.fast.screens.twofa.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.ext.getString
import com.meloda.fast.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.ui.AppTheme
import com.meloda.fast.ui.widgets.TextFieldErrorText
import org.koin.androidx.viewmodel.ext.android.viewModel

class TwoFaFragment : BaseFragment() {

    private val viewModel: TwoFaViewModel by viewModel<TwoFaViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback {
            viewModel.onBackButtonClicked()
        }

        (view as? ComposeView)?.apply {
            setContent {
                AppTheme {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        val state by viewModel.screenState.collectAsStateWithLifecycle()

                        TwoFaScreen(
                            onCodeInputChanged = viewModel::onCodeInputChanged,
                            onTextFieldDoneClicked = viewModel::onTextFieldDoneClicked,
                            onRequestSmsButtonClicked = viewModel::onRequestSmsButtonClicked,
                            onDoneButtonClicked = viewModel::onDoneButtonClicked,
                            state = state
                        )
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun TwoFaScreenPreview() {
        AppTheme(
            darkTheme = false,
            dynamicColors = false
        ) {
            Surface(color = MaterialTheme.colorScheme.background) {
                TwoFaScreen(
                    onCodeInputChanged = {},
                    onTextFieldDoneClicked = {},
                    onRequestSmsButtonClicked = {},
                    onDoneButtonClicked = {},
                    state = TwoFaScreenState.EMPTY
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TwoFaScreen(
        onCodeInputChanged: (String) -> Unit,
        onTextFieldDoneClicked: () -> Unit,
        onRequestSmsButtonClicked: () -> Unit,
        onDoneButtonClicked: () -> Unit,
        state: TwoFaScreenState,
    ) {
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                },
                text = {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_close_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {

                Text(
                    text = "Two-Factor\nAuthentication",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(38.dp))
                Text(
                    text = state.twoFaText.getString().orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(30.dp))

                val delayRemainedTime = state.delayTime
                AnimatedVisibility(visible = delayRemainedTime > 0) {
                    Text(text = "Can resend after $delayRemainedTime seconds")
                }

                var code by remember { mutableStateOf(TextFieldValue(state.twoFaCode)) }
                val codeError = state.codeError

                TextField(
                    value = code,
                    onValueChange = { newText ->
                        code = newText
                        onCodeInputChanged.invoke(newText.text)
                    },
                    label = { Text(text = "Code") },
                    placeholder = { Text(text = "Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_qr_code_24),
                            contentDescription = null,
                            tint = if (codeError != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onTextFieldDoneClicked.invoke()
                        }
                    ),
                    isError = codeError != null
                )

                AnimatedVisibility(visible = codeError != null) {
                    TextFieldErrorText(text = codeError.getString().orEmpty())
                }
            }

            // TODO: 09.04.2023, Danil Nikolaev: проверить работоспособность 2фа

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val canResendSms = state.canResendSms

                AnimatedVisibility(
                    visible = canResendSms,
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onRequestSmsButtonClicked,
                        text = {
                            Text(
                                text = "Request SMS",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.round_sms_24),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = null
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                FloatingActionButton(
                    onClick = onDoneButtonClicked,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_done_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                AnimatedVisibility(
                    visible = !canResendSms,
                    exit = shrinkHorizontally()
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }

    companion object {

        fun newInstance(): TwoFaFragment {
            return TwoFaFragment()
        }
    }
}
