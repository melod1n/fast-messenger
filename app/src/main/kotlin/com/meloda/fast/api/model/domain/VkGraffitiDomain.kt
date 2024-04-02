package com.meloda.fast.api.model.domain

data class VkGraffitiDomain(
    val id: Int,
    val ownerId: Int,
    val url: String,
    val width: Int,
    val height: Int,
    val accessKey: String
) : VkAttachment {

    val className: String = this::class.java.name
}
