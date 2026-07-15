package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.ui.model.vk.UiFriend

@Immutable
data class SelectableUiFriend(
    val friend: UiFriend,
    val isSelected: Boolean
)
