package dev.meloda.fast.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.R

@Composable
fun TextFieldErrorText(
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.error_empty_field),
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

@Preview
@Composable
private fun TextFieldErrorPreview() {
    TextFieldErrorText(text = "Error")
}
