package dev.meloda.fast.convos.presentation

import androidx.compose.runtime.Composable
import dev.meloda.fast.convos.model.ConvoIntent
import dev.meloda.fast.convos.model.ConvosScreenState

@Composable
fun ConvosRoute(
    handleIntent: (ConvoIntent) -> Unit,
    screenState: ConvosScreenState,
    isArchive: Boolean,
) {
    ConvosScreen(
        handleIntent = handleIntent,
        screenState = screenState,
        isArchive = isArchive,
    )

    HandleDialogs(
        handleIntent = handleIntent,
        screenState = screenState,
    )
}
