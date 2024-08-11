package dev.meloda.fast.friends.di

import dev.meloda.fast.domain.FriendsUseCase
import dev.meloda.fast.friends.FriendsViewModelImpl
import dev.meloda.fast.domain.FriendsUseCaseImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val friendsModule = module {
    singleOf(::FriendsUseCaseImpl) bind dev.meloda.fast.domain.FriendsUseCase::class

    viewModelOf(::FriendsViewModelImpl)
}
