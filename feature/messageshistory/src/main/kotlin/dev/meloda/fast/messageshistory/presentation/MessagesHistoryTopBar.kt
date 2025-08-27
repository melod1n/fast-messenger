package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.getImage

import dev.meloda.fast.ui.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun MessagesHistoryTopBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    showReplyAction: Boolean,
    isClickable: Boolean,
    isMessagesSelecting: Boolean,
    isPeerAccount: Boolean,
    avatar: UiImage,
    title: String,
    onTopBarClicked: () -> Unit = {},
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    onDeleteSelectedButtonClicked: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val view = LocalView.current
    val theme = LocalThemeConfig.current

    var dropDownMenuExpanded by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        modifier = modifier
            .then(
                if (theme.enableBlur) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.thick()
                    )
                } else Modifier
            )
            .fillMaxWidth()
            .then(
                if (!isClickable) Modifier
                else Modifier.clickable {
                    onTopBarClicked()
                }
            ),
        title = {
            Row(
//                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isMessagesSelecting) {
                    if (isPeerAccount) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp),
                                painter = painterResource(id = R.drawable.ic_round_bookmark_border_24),
                                contentDescription = "Favorites icon",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        val actualAvatar = avatar.getImage()

                        if (actualAvatar is Painter) {
                            Image(
                                painter = actualAvatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            AsyncImage(
                                model = actualAvatar,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                placeholder = painterResource(id = R.drawable.ic_account_circle_cut),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (!isMessagesSelecting) onBack()
                    else onClose()
                }
            ) {
                Crossfade(targetState = !isMessagesSelecting) { state ->
                    Icon(
                        imageVector = if (state) {
                            Icons.AutoMirrored.Rounded.ArrowBack
                        } else {
                            Icons.Rounded.Close
                        },
                        contentDescription = if (state) "Close button"
                        else "Back button"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            if (isMessagesSelecting) {
                AnimatedVisibility(showReplyAction) {
                    IconButton(
                        onClick = {
                            if (AppSettings.General.enableHaptic) {
                                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_reply_24),
                            contentDescription = null
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (AppSettings.General.enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_reply_all_24),
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = {
                        if (AppSettings.General.enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_forward_24),
                        contentDescription = null
                    )
                }
                IconButton(onClick = onDeleteSelectedButtonClicked) {
                    Icon(
                        painter = painterResource(R.drawable.round_delete_outline_24),
                        contentDescription = null
                    )
                }
            } else {
                IconButton(
                    onClick = { dropDownMenuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options"
                    )
                }

                DropdownMenu(
                    modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = {
                        dropDownMenuExpanded = false
                    },
                    offset = DpOffset(x = (-4).dp, y = (-60).dp)
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onRefresh()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = stringResource(R.string.action_refresh))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    )
}
