package dev.meloda.fast.domain.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.StringAnnotation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.conena.nanokt.collections.indexOfFirstOrNull
import dev.meloda.fast.common.extensions.collidesWith
import dev.meloda.fast.common.extensions.minus
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.model.api.domain.FormatDataType
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.model.vk.MentionIndex
import dev.meloda.fast.ui.model.vk.MessageUiItem

fun emptyAnnotatedString(): AnnotatedString = AnnotatedString(text = "")

fun AnnotatedString?.orEmpty(): AnnotatedString = this ?: emptyAnnotatedString()

fun String.annotated(): AnnotatedString = AnnotatedString(text = this)

fun isAccount(id: Long) = id == UserConfig.userId

fun extractTextWithVisualizedMentions(
    isOut: Boolean,
    originalText: String?,
    formatData: VkMessage.FormatData?
): AnnotatedString? {
    if (originalText == null) return null

    val annotations =
        mutableListOf<AnnotatedString.Range<out androidx.compose.ui.text.AnnotatedString.Annotation>>()

    val regex = """\[(id|club)(\d+)\|([^]]+)]""".toRegex()

    val mentions = mutableListOf<MentionIndex>()

    var currentIndex = 0
    val replacements = mutableListOf<Pair<IntRange, String>>()

    val newText = regex.replace(originalText) { matchResult ->
        val idPrefix = matchResult.groups[1]?.value.orEmpty()
        val startIndex = matchResult.range.first
        val endIndex = matchResult.range.last

        val id = matchResult.groups[2]?.value ?: ""

        val replaced = matchResult.groups[3]?.value.orEmpty()

        val indexRange =
            (startIndex + currentIndex)..startIndex + currentIndex + replaced.length

        replacements.add(indexRange to replaced)

        mentions += MentionIndex(
            id = id.toLongOrNull() ?: -1,
            idPrefix = idPrefix,
            indexRange = indexRange
        )

        currentIndex += replaced.length - (endIndex - startIndex + 1)

        replaced
    }

    mentions.forEach { mention ->
        val startIndex = mention.indexRange.first
        val endIndex = mention.indexRange.last

        annotations += if (isOut) {
            AnnotatedString.Range(
                item = SpanStyle(textDecoration = TextDecoration.Underline),
                start = startIndex,
                end = endIndex
            )
        } else {
            AnnotatedString.Range(
                item = SpanStyle(color = Color.Red),
                start = startIndex,
                end = endIndex
            )
        }

        annotations += AnnotatedString.Range(
            item = StringAnnotation(mention.id.toString()),
            tag = mention.idPrefix,
            start = startIndex,
            end = endIndex
        )
    }

    if (formatData == null) {
        return AnnotatedString(text = newText, annotations = annotations)
    }

    var current = 0

    val newOffsets = formatData.items.map { (offset, length) ->
        val r = replacements.filter { (range, _) ->
            (range - current) collidesWith (offset..<offset + length) || offset > range.first
        }

        current = r.sumOf { (range, _) -> range.last - range.first - 1 }

        offset + current
    }

    formatData.items.forEachIndexed { index, item ->
        val offset = newOffsets[index]

        val spanStyle = when (item.type) {
            FormatDataType.BOLD -> {
                SpanStyle(fontWeight = FontWeight.SemiBold)
            }

            FormatDataType.ITALIC -> {
                SpanStyle(fontStyle = FontStyle.Italic)
            }

            FormatDataType.UNDERLINE -> {
                SpanStyle(textDecoration = TextDecoration.Underline)
            }

            FormatDataType.URL -> {
                annotations += AnnotatedString.Range(
                    item = StringAnnotation(item.url.orEmpty()),
                    start = offset,
                    end = offset + item.length,
                    tag = newText.substring(offset, offset + item.length)
                )

                if (isOut) {
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )

                } else {
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Red
                    )
                }
            }
        }

        annotations += AnnotatedString.Range(
            item = spanStyle,
            start = offset,
            end = offset + item.length
        )
    }

    return AnnotatedString(text = newText, annotations = annotations)
}


fun List<MessageUiItem>.firstMessage(): MessageUiItem.Message =
    filterIsInstance<MessageUiItem.Message>().first()

fun List<MessageUiItem>.firstMessageOrNull(): MessageUiItem.Message? =
    filterIsInstance<MessageUiItem.Message>().firstOrNull()

fun List<MessageUiItem>.indexOfMessageById(messageId: Long): Int =
    indexOfFirst { it.id == messageId }

fun List<MessageUiItem>.findMessageById(messageId: Long): MessageUiItem.Message? =
    firstOrNull { it.id == messageId } as MessageUiItem.Message?

fun List<MessageUiItem>.indexOfMessageByCmId(cmId: Long): Int? =
    indexOfFirstOrNull { it.cmId == cmId }

fun List<MessageUiItem>.findMessageByCmId(cmId: Long): MessageUiItem.Message =
    first { it.cmId == cmId } as MessageUiItem.Message
