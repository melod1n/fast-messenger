package com.meloda.fast.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun TextFieldErrorText(
    modifier: Modifier = Modifier,
    text: String = "Field must not be empty",
    withSpacer: Boolean = true
) {
    Row {
        if (withSpacer) {
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = text,
            modifier = modifier,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
