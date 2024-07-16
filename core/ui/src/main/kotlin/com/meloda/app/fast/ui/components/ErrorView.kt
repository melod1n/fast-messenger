package dev.meloda.fast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    text: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )

        buttonText?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onButtonClick?.invoke() }
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Preview
@Composable
private fun ErrorViewPreview() {
    ErrorView(
        text = "Some error occurred",
        buttonText = "Restart"
    )
}
