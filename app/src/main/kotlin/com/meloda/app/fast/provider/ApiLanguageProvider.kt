package com.meloda.app.fast.provider

import com.meloda.app.fast.common.model.ApiLanguage
import com.meloda.app.fast.common.provider.Provider
import com.meloda.app.fast.datastore.UserSettings

class ApiLanguageProvider(private val userSettings: UserSettings) : Provider<ApiLanguage> {

    override fun provide(): ApiLanguage? {
        val language = userSettings.appLanguage.value

        return when {
            language == "ru-RU" -> "ru"
            language.startsWith("en") -> "en"
            language == "uk-UA" -> "ua"
            else -> null
        }?.let(::ApiLanguage)
    }

}
