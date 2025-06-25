package dev.meloda.fast.messageshistory.presentation

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MessageTextContainer(
    modifier: Modifier = Modifier,
    text: AnnotatedString?,
    isOut: Boolean,
    isSelected: Boolean,
) {
    if (text == null) return

    if (isSelected) {
        SelectionContainer {
            MessageText(
                modifier = modifier,
                text = text,
                isOut = isOut,
            )
        }
    } else {
        MessageText(
            modifier = modifier,
            text = text,
            isOut = isOut,
        )
    }
}

@Composable
fun MessageText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    isOut: Boolean,
) {
    val replacedColor = if (isOut) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    val newText = remember(text) {
        val builder = AnnotatedString.Builder(text)

        text.spanStyles.map { spanStyleRange ->
            val updatedSpanStyle =
                if (spanStyleRange.item.color == Color.Red) {
                    spanStyleRange.item.copy(color = replacedColor)
                } else {
                    spanStyleRange.item
                }

            builder.addStyle(
                style = updatedSpanStyle,
                start = spanStyleRange.start,
                end = spanStyleRange.end
            )
        }

        text.paragraphStyles.forEach { style ->
            builder.addStyle(
                style = style.item,
                start = style.start,
                end = style.end
            )
        }

        builder.toAnnotatedString()
    }

    Text(
        text = newText,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun MessageTextPreview() {
    MessageTextContainer(
        modifier = Modifier,
        text = AnnotatedString("Some cool text"),
        isOut = true,
        isSelected = false
    )
}
