package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkMiniAppDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkMiniAppData(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "app") val app: App,
    @Json(name = "images") val images: List<Image>?,
    @Json(name = "button_text") val buttonText: String
) {

    @JsonClass(generateAdapter = true)
    data class App(
        @Json(name = "type") val type: String,
        @Json(name = "id") val id: Int,
        @Json(name = "title") val title: String,
        @Json(name = "author_owner_id") val authorOwnerId: Int,
        @Json(name = "is_favorite") val isFavorite: Boolean,
        @Json(name = "share_url") val shareUrl: String,
        @Json(name = "webview_url") val webViewUrl: String,
        @Json(name = "icon_75") val icon75: String?,
        @Json(name = "icon_139") val icon139: String?,
        @Json(name = "icon_150") val icon150: String?,
        @Json(name = "icon_278") val icon278: String?,
        @Json(name = "icon_576") val icon576: String?,
    )

    @JsonClass(generateAdapter = true)
    data class Image(
        @Json(name = "height") val height: Int,
        @Json(name = "width") val width: Int,
        @Json(name = "url") val url: String
    )

    fun toDomain() = VkMiniAppDomain(link = app.shareUrl)
}
