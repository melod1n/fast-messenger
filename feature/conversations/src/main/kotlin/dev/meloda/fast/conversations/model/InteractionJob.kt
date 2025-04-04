package dev.meloda.fast.conversations.model

import dev.meloda.fast.model.InteractionType
import kotlinx.coroutines.Job

data class InteractionJob(
    val interactionType: InteractionType,
    val timerJob: Job
)
