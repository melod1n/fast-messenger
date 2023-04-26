package com.meloda.fast.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.meloda.fast.ext.getString
import com.meloda.fast.model.base.UiText
import com.meloda.fast.ui.AppTheme

@Composable
fun MaterialDialog(
    onDismissAction: (() -> Unit),
    title: UiText? = null,
    message: UiText? = null,
    positiveText: UiText? = null,
    positiveAction: (() -> Unit)? = null,
    negativeText: UiText? = null,
    negativeAction: (() -> Unit)? = null,
    neutralText: UiText? = null,
    neutralAction: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    var isVisible by remember {
        mutableStateOf(true)
    }
    val onDismissRequest = {
        onDismissAction.invoke()
        isVisible = false
    }

    AppTheme {
        // TODO: 08.04.2023, Danil Nikolaev: implement animation
        AlertAnimation(visible = isVisible) {
            Dialog(onDismissRequest = onDismissRequest) {
                val scrollState = rememberScrollState()
                val canScrollBackward by remember { derivedStateOf { scrollState.value > 0 } }
                val canScrollForward by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AlertDialogDefaults.containerColor,
                    shape = AlertDialogDefaults.shape,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            top = 20.dp,
                            end = 20.dp,
                            bottom = 10.dp
                        )
                    ) {
                        Row {
                            title?.getString()?.let { title ->
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }

                        if (canScrollBackward) {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(scrollState)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                message?.getString()?.let { message ->
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            content?.let { content ->
                                Spacer(modifier = Modifier.height(4.dp))
                                content.invoke()
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        if (canScrollForward) {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        Row {
                            neutralText?.getString()?.let { text ->
                                TextButton(
                                    onClick = {
                                        onDismissRequest.invoke()
                                        neutralAction?.invoke()
                                    }
                                ) {
                                    Text(text = text)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            negativeText?.getString()?.let { text ->
                                TextButton(
                                    onClick = {
                                        onDismissRequest.invoke()
                                        negativeAction?.invoke()
                                    }
                                ) {
                                    Text(text = text)
                                }
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            positiveText?.getString()?.let { text ->
                                TextButton(
                                    onClick = {
                                        onDismissRequest.invoke()
                                        positiveAction?.invoke()
                                    }
                                ) {
                                    Text(text = text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AlertAnimation(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                scaleIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(150)),
        content = content
    )
}
