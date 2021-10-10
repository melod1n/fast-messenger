package com.meloda.fast.api.model.attachments

import androidx.room.Ignore
import com.meloda.fast.api.model.base.attachments.BaseVkPhoto
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
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

    @Ignore
    @IgnoredOnParcel
    private val sizesChars = Stack<Char>()

    init {
        sizesChars.push('s')
        sizesChars.push('m')
        sizesChars.push('x')
        sizesChars.push('o')
        sizesChars.push('p')
        sizesChars.push('q')
        sizesChars.push('r')
        sizesChars.push('y')
        sizesChars.push('z')
        sizesChars.push('w')
    }

    @IgnoredOnParcel
    val className: String = this::class.java.name

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
        val photoStack = sizesChars.clone() as Stack<Char>

        val sizeIndex = photoStack.search(type)

        if (sizeIndex == -1) return null

        for (i in 0 until sizeIndex) {
            photoStack.pop()
        }

        for (i in 0 until photoStack.size) {
            val size = getSizeOrNull(photoStack.peek())

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