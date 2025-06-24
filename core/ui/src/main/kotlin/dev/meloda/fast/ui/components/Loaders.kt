package dev.meloda.fast.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun FullScreenContainedLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        ContainedLoader()
    }
}

@Preview
@Composable
fun FullScreenLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Loader()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun ContainedLoader(modifier: Modifier = Modifier) {
    ContainedLoadingIndicator(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun Loader(modifier: Modifier = Modifier) {
    LoadingIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary
    )
}

