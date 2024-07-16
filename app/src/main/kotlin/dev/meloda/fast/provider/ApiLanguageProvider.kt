package dev.meloda.fast.provider

import dev.meloda.fast.common.model.ApiLanguage
import dev.meloda.fast.common.provider.Provider
import dev.meloda.fast.datastore.UserSettings

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
