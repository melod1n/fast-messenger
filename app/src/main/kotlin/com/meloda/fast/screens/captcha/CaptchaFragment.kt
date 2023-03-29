package com.meloda.fast.screens.captcha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.ui.*
import com.meloda.fast.ui.widgets.TextFieldErrorText
import org.koin.androidx.viewmodel.ext.android.viewModel

class CaptchaFragment : BaseFragment() {

    private val viewModel: CaptchaViewModel by viewModel<CaptchaViewModelImpl>()

    private val captchaImage by lazy {
        arguments?.getString("captchaImage") ?: "https://api.vk.com/captcha.php?sid=346849433736"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { CaptchaScreen() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun CaptchaScreen() {
        val focusManager = LocalFocusManager.current

        AppTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ExtendedFloatingActionButton(
                        onClick = viewModel::onCancelButtonClicked,
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
                            text = "Captcha",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(38.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "To proceed with your action, enter a code from the picture",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(0.5f)
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(captchaImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .height(48.dp)
                                    .width(130.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        val state by viewModel.screenState.collectAsState()
                        var code by remember { mutableStateOf(TextFieldValue(state.captchaCode)) }
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
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
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

                    FloatingActionButton(
                        onClick = viewModel::onDoneButtonClicked,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_done_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback {
            viewModel.onBackButtonClicked()
        }
    }

    companion object {
        fun newInstance(captchaImage: String?): CaptchaFragment {
            val fragment = CaptchaFragment()
            fragment.arguments = bundleOf("captchaImage" to captchaImage)

            return fragment
        }
    }
}
