package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkMiniApp(
    val title: String,
    val description: String,
    val app: App,
    val images: List<Image>?,
    @SerializedName("button_text")
    val buttonText: String
) : Parcelable {

    @Parcelize
    data class App(
        val type: String,
        val id: Int,
        val title: String,
        @SerializedName("author_owner_id")
        val authorOwnerId: Int,
        @SerializedName("are_notifications_enabled")
        val areNotificationsEnabled: Boolean,
        @SerializedName("is_favorite")
        val isFavorite: Boolean,
        @SerializedName("is_installed")
        val isInstalled: Boolean,
        @SerializedName("track_code")
        val trackCode: String,
        @SerializedName("share_url")
        val shareUrl: String,
        @SerializedName("webview_url")
        val webViewUrl: String,
        @SerializedName("hide_tabbar")
        val hideTabBar: Int,
        @SerializedName("icon_75")
        val icon75: String?,
        @SerializedName("icon_139")
        val icon139: String?,
        @SerializedName("icon_150")
        val icon150: String?,
        @SerializedName("icon_278")
        val icon278: String?,
        @SerializedName("icon_576")
        val icon576: String?,
        @SerializedName("open_in_external_browser")
        val openInExternalBrowser: Boolean,
        @SerializedName("need_policy_confirmation")
        val needPolicyConfirmation: Boolean,
        @SerializedName("is_vkui_internal")
        val isVkUiInternal: Boolean,
        @SerializedName("has_vk_connect")
        val hasVkConnect: Boolean,
        @SerializedName("need_show_bottom_menu_tooltip_on_close")
        val needShowBottomMenuTooltipOnClose: Boolean
    ) : Parcelable

    @Parcelize
    data class Image(
        val height: Int,
        val width: Int,
        val url: String
    ) : Parcelable

}