package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkPhotoData
import com.meloda.fast.model.base.UiText
import java.util.Stack


// TODO: 11/04/2024, Danil Nikolaev: review
data class VkPhotoDomain(
    val albumId: Int,
    val date: Int,
    val id: Int,
    val ownerId: Int,
    val hasTags: Boolean,
    val accessKey: String?,
    val sizes: List<VkPhotoData.Size>,
    val text: String?,
    val userId: Int?
) : VkMultipleAttachment {

    override val type: AttachmentType = AttachmentType.PHOTO

    private val sizesChars = Stack<Char>()

    override fun getUiText(size: Int): UiText =
        UiText.QuantityResource(R.plurals.attachment_photos, size)

    init {
        sizesChars.push(SIZE_TYPE_75)
        sizesChars.push(SIZE_TYPE_130)
        sizesChars.push(SIZE_TYPE_604)
        sizesChars.push('o')
        sizesChars.push('p')
        sizesChars.push('q')
        sizesChars.push('r')
        sizesChars.push(SIZE_TYPE_807)
        sizesChars.push(SIZE_TYPE_1080_1024)
        sizesChars.push(SIZE_TYPE_2560_2048)
    }

    val className: String = this::class.java.name

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )

    fun getMaxSize(): VkPhotoData.Size? {
        return getSizeOrSmaller(sizesChars.peek())
    }

    fun getSizeOrNull(type: Char): VkPhotoData.Size? {
        for (size in sizes) {
            if (size.type == type.toString()) return size
        }

        return null
    }

    fun getSizeOrSmaller(type: Char): VkPhotoData.Size? {
        val photoStack = sizesChars.clone() as Stack<*>

        val sizeIndex = photoStack.search(type)

        if (sizeIndex == -1) return null

        for (i in 0 until sizeIndex) {
            photoStack.pop()
        }

        for (i in 0 until photoStack.size) {
            val size = getSizeOrNull(photoStack.peek() as Char)

            if (size == null) {
                photoStack.pop()
                continue
            } else {
                return size
            }
        }

        return null
    }

    companion object {
        const val SIZE_TYPE_75 = 's'
        const val SIZE_TYPE_130 = 'm'
        const val SIZE_TYPE_604 = 'x'
        const val SIZE_TYPE_807 = 'y'
        const val SIZE_TYPE_1080_1024 = 'z'
        const val SIZE_TYPE_2560_2048 = 'w'
    }
}
