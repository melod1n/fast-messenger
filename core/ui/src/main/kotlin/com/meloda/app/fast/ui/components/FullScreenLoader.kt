package dev.meloda.fast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FullScreenLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun FullScreenLoaderPreview() {
    FullScreenLoader()
}
