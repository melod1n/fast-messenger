package dev.meloda.fast.friends.di

import dev.meloda.fast.data.api.friends.FriendsUseCase
import dev.meloda.fast.friends.FriendsViewModelImpl
import dev.meloda.fast.friends.domain.FriendsUseCaseImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val friendsModule = module {
    singleOf(::FriendsUseCaseImpl) bind FriendsUseCase::class

    viewModelOf(::FriendsViewModelImpl)
}
