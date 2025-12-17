package dev.meloda.fast.convos.di

import dev.meloda.fast.convos.ConvosViewModel
import dev.meloda.fast.domain.ConvoUseCase
import dev.meloda.fast.domain.ConvoUseCaseImpl
import dev.meloda.fast.model.ConvosFilter
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

val convosModule = module {
    viewModel(named(ConvosFilter.ALL)) {
        createConvosViewModel(ConvosFilter.ALL)
    }
    viewModel(named(ConvosFilter.ARCHIVE)) {
        createConvosViewModel(ConvosFilter.ARCHIVE)
    }

    singleOf(::ConvoUseCaseImpl) bind ConvoUseCase::class
}

private fun Scope.createConvosViewModel(filter: ConvosFilter): ConvosViewModel {
    return ConvosViewModel(
        filter = filter,
        updatesParser = get(),
        convoUseCase = get(),
        messagesUseCase = get(),
        resources = get(),
        userSettings = get(),
        imageLoader = get(),
        applicationContext = get(),
        loadConvosByIdUseCase = get()
    )
}
