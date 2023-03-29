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
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.ui.AppTheme
import com.meloda.fast.ui.widgets.TextFieldErrorText
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class TwoFaFragment : BaseFragment() {

    private val viewModel: TwoFaViewModel by activityViewModel<TwoFaViewModelImpl>()

    private val validationSid by lazy { requireArguments().getString(ARG_VALIDATION_SID).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TwoFaScreen() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun TwoFaScreen() {
        val focusManager = LocalFocusManager.current

        AppTheme {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
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
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(38.dp))
                        Text(
                            text = "Enter code from your mobile app",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(30.dp))

                        val state by viewModel.screenState.collectAsState()
                        var code by remember { mutableStateOf(TextFieldValue(state.twoFaCode)) }
                        val showError by viewModel.isNeedToShowCodeError.collectAsState()

                        TextField(
                            value = code,
                            onValueChange = { newText ->
                                code = newText
                                viewModel.onCodeInputChanged(newText.text)
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
                                    tint = if (showError) {
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
                                    viewModel.onTextFieldDoneClicked()
                                }
                            ),
                            isError = showError
                        )

                        AnimatedVisibility(visible = showError) {
                            TextFieldErrorText(text = "Field must not be empty")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        var isVisible by remember {
                            mutableStateOf(false)
                        }

                        AnimatedVisibility(
                            visible = isVisible,
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = viewModel::onRequestSmsButtonClicked,
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
                            onClick = viewModel::onDoneButtonClicked,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_done_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        AnimatedVisibility(
                            visible = !isVisible,
                            exit = shrinkHorizontally()
                        ) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.onViewFirstCreation(validationSid)
        }

        activity?.onBackPressedDispatcher?.addCallback {
            viewModel.onBackButtonClicked()
        }
    }

    companion object {
        private const val ARG_VALIDATION_SID = "validationSid"

        fun newInstance(validationSid: String): TwoFaFragment {
            val fragment = TwoFaFragment()
            fragment.arguments = bundleOf(ARG_VALIDATION_SID to validationSid)
            return fragment
        }
    }
}
