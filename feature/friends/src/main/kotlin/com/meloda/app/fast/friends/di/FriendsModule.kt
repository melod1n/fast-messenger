package com.meloda.app.fast.friends.di

import com.meloda.app.fast.data.api.friends.FriendsUseCase
import com.meloda.app.fast.friends.FriendsViewModelImpl
import com.meloda.app.fast.friends.domain.FriendsUseCaseImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val friendsModule = module {
    singleOf(::FriendsUseCaseImpl) bind FriendsUseCase::class

    viewModelOf(::FriendsViewModelImpl)
}
