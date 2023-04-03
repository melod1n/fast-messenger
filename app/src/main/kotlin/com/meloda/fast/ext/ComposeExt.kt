package com.meloda.fast.ext

import android.media.AudioManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.meloda.fast.common.AppGlobal

@ExperimentalFoundationApi
fun Modifier.clickableSound(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = this.clickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = {
        AppGlobal.audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
        onClick.invoke()
    }
)


@ExperimentalFoundationApi
fun Modifier.combinedClickableSound(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = this.combinedClickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onLongClickLabel = onLongClickLabel,
    onLongClick = onLongClick,
    onDoubleClick = onDoubleClick,
    onClick = {
        AppGlobal.audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
        onClick.invoke()
    }
)
