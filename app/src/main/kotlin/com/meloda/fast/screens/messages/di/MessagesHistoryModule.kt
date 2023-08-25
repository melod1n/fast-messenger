package com.meloda.fast.screens.messages.di

import com.meloda.fast.di.navigationModule
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.screen.MessagesHistoryCoordinator
import com.meloda.fast.screens.messages.screen.MessagesHistoryCoordinatorImpl
import com.meloda.fast.screens.messages.screen.MessagesHistoryScreen
import com.meloda.fast.screens.messages.validation.MessagesHistoryValidator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

private val argumentsMap = hashMapOf<Int, MessagesHistoryArguments>()

val messagesHistoryModule = module {
    val moduleQualifier = named("messagesHistory")

    includes(navigationModule)

    single(moduleQualifier) { screen().resultFlow }
    single { screen().getArguments() }

    single {
        MessagesHistoryCoordinatorImpl(
            resultFlow = get(moduleQualifier),
            router = get()
        )
    } bind MessagesHistoryCoordinator::class

    singleOf(::MessagesHistoryValidator)

    viewModel { params ->
        val arguments: MessagesHistoryArguments = params.get()

        MessagesHistoryViewModelImpl(
            messagesRepository = get(),
            updatesParser = get(),
            photosRepository = get(),
            filesRepository = get(),
            audiosRepository = get(),
            videosRepository = get(),
            arguments = arguments
        )
    }
}

private fun Scope.screen(): MessagesHistoryScreen = get()
