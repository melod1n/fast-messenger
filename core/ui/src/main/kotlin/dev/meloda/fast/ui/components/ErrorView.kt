package dev.meloda.fast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import dev.meloda.fast.ui.R

@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    iconResId: Int? = R.drawable.round_error_24,
    text: String,
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
        iconResId?.let {
            Icon(
                modifier = Modifier.size(120.dp),
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        buttonText?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onButtonClick?.invoke() },
                shape = RoundedCornerShape(6.dp)
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
