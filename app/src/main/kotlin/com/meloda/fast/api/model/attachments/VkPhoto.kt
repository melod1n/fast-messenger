package com.meloda.fast.api.model.attachments

import androidx.room.Ignore
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.base.attachments.BaseVkPhoto
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*


data class VkPhoto(
    val albumId: Int,
    val date: Int,
    val id: Int,
    val ownerId: Int,
    val hasTags: Boolean,
    val accessKey: String?,
    val sizes: List<BaseVkPhoto.Size>,
    val text: String?,
    val userId: Int?
) : VkAttachment() {

    companion object {
        const val SIZE_TYPE_75 = 's'
        const val SIZE_TYPE_130 = 'm'
        const val SIZE_TYPE_604 = 'x'
        const val SIZE_TYPE_807 = 'y'
        const val SIZE_TYPE_1080_1024 = 'z'
        const val SIZE_TYPE_2560_2048 = 'w'
    }

    private val sizesChars = Stack<Char>()

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

    fun getMaxSize(): BaseVkPhoto.Size? {
        return getSizeOrSmaller(sizesChars.peek())
    }

    fun getSizeOrNull(type: Char): BaseVkPhoto.Size? {
        for (size in sizes) {
            if (size.type == type.toString()) return size
        }

        return null
    }

    fun getSizeOrSmaller(type: Char): BaseVkPhoto.Size? {
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

}
