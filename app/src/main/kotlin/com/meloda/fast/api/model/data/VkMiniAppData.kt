package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkMiniAppDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkMiniAppData(
    val title: String,
    val description: String,
    val app: App,
    val images: List<Image>?,
    @Json(name = "button_text") val buttonText: String
) {

    @JsonClass(generateAdapter = true)
    data class App(
        val type: String,
        val id: Int,
        val title: String,
        @Json(name = "author_owner_id")
        val authorOwnerId: Int,
        @Json(name = "are_notifications_enabled")
        val areNotificationsEnabled: Boolean,
        @Json(name = "is_favorite")
        val isFavorite: Boolean,
        @Json(name = "is_installed")
        val isInstalled: Boolean,
        @Json(name = "track_code")
        val trackCode: String,
        @Json(name = "share_url")
        val shareUrl: String,
        @Json(name = "webview_url")
        val webViewUrl: String,
        @Json(name = "hide_tabbar")
        val hideTabBar: Int,
        @Json(name = "icon_75")
        val icon75: String?,
        @Json(name = "icon_139")
        val icon139: String?,
        @Json(name = "icon_150")
        val icon150: String?,
        @Json(name = "icon_278")
        val icon278: String?,
        @Json(name = "icon_576")
        val icon576: String?,
        @Json(name = "open_in_external_browser")
        val openInExternalBrowser: Boolean,
        @Json(name = "need_policy_confirmation")
        val needPolicyConfirmation: Boolean,
        @Json(name = "is_vkui_internal")
        val isVkUiInternal: Boolean,
        @Json(name = "has_vk_connect")
        val hasVkConnect: Boolean,
        @Json(name = "need_show_bottom_menu_tooltip_on_close")
        val needShowBottomMenuTooltipOnClose: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class Image(
        val height: Int,
        val width: Int,
        val url: String
    )

    fun toDomain() = VkMiniAppDomain(link = app.shareUrl)
}
