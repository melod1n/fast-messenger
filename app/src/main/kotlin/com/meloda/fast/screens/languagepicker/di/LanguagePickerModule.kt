package com.meloda.fast.screens.languagepicker.di

import com.meloda.fast.screens.languagepicker.LanguagePickerViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val languagePickerModule = module {
    viewModelOf(::LanguagePickerViewModelImpl)
}
