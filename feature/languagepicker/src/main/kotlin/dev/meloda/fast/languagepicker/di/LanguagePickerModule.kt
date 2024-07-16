package dev.meloda.fast.languagepicker.di

import dev.meloda.fast.languagepicker.LanguagePickerViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val languagePickerModule = module {
    viewModelOf(::LanguagePickerViewModelImpl)
}
