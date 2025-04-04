package dev.meloda.fast.friends.di

import dev.meloda.fast.domain.FriendsUseCase
import dev.meloda.fast.domain.FriendsUseCaseImpl
import dev.meloda.fast.friends.FriendsViewModelImpl
import dev.meloda.fast.friends.OnlineFriendsViewModelImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val friendsModule = module {
    singleOf(::FriendsUseCaseImpl) bind FriendsUseCase::class
    viewModelOf(::FriendsViewModelImpl)
    viewModelOf(::OnlineFriendsViewModelImpl)
}
