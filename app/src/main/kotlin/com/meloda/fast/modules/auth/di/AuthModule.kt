package com.meloda.fast.modules.auth.di

import com.meloda.fast.modules.auth.model.data.repository.AuthRepositoryImpl
import com.meloda.fast.modules.auth.model.data.repository.OAuthRepositoryImpl
import com.meloda.fast.modules.auth.model.data.service.AuthService
import com.meloda.fast.modules.auth.model.data.service.OAuthService
import com.meloda.fast.modules.auth.model.data.usecase.AuthUseCaseImpl
import com.meloda.fast.modules.auth.model.data.usecase.OAuthUseCaseImpl
import com.meloda.fast.modules.auth.model.domain.repository.AuthRepository
import com.meloda.fast.modules.auth.model.domain.repository.OAuthRepository
import com.meloda.fast.modules.auth.model.domain.usecase.AuthUseCase
import com.meloda.fast.modules.auth.model.domain.usecase.OAuthUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

val authModule = module {
    single { get<Retrofit>().create(AuthService::class.java) }
    single { get<Retrofit>(named("oauth")).create(OAuthService::class.java) }

    singleOf(::OAuthRepositoryImpl) bind OAuthRepository::class
    singleOf(::OAuthUseCaseImpl) bind OAuthUseCase::class

    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::AuthUseCaseImpl) bind AuthUseCase::class
}
