package com.meloda.app.fast.languagepicker.di

import com.meloda.app.fast.languagepicker.LanguagePickerViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val languagePickerModule = module {
    viewModelOf(::LanguagePickerViewModelImpl)
}
