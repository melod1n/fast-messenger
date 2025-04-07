package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.PhotoSize
import dev.meloda.fast.model.api.data.AttachmentType
import java.util.Stack


// TODO: 11/04/2024, Danil Nikolaev: review
data class VkPhotoDomain(
    val albumId: Long,
    val date: Int?,
    val id: Long,
    val ownerId: Long,
    val hasTags: Boolean,
    val accessKey: String?,
    val sizes: List<PhotoSize>,
    val text: String?,
    val userId: Long?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.PHOTO

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

    fun getMaxSize(): PhotoSize? {
        return getSizeOrSmaller(sizesChars.peek())
    }

    fun getDefault(): PhotoSize? {
        return getSizeOrSmaller(SIZE_TYPE_1080_1024)
    }

    fun getSizeOrNull(type: Char): PhotoSize? {
        for (size in sizes) {
            if (size.type == type.toString()) return size
        }

        return null
    }

    fun getSizeOrSmaller(type: Char): PhotoSize? {
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

    override fun toString(): String {
        val result = StringBuilder(type.value).append(ownerId).append('_').append(id)
        if (!accessKey.isNullOrBlank()) {
            result.append('_')
            result.append(accessKey)
        }
        return result.toString()
    }
}
