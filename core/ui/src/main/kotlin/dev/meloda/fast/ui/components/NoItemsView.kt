package dev.meloda.fast.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.common.FastPreview
import dev.meloda.fast.ui.theme.AppTheme

@Composable
fun NoItemsView(
    modifier: Modifier = Modifier,
    customText: String? = null,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = customText ?: stringResource(R.string.no_items),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
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

@FastPreview
@Composable
private fun NoItemsViewPreview() {
    AppTheme(useDarkTheme = isSystemInDarkTheme(), useDynamicColors = true) {
        Surface {
            NoItemsView(
                customText = "Nothing here...",
                buttonText = "Refresh"
            )
        }
    }
}
