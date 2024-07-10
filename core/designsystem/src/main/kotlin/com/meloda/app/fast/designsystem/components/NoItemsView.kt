package com.meloda.app.fast.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.meloda.app.fast.designsystem.R

@Composable
fun NoItemsView(
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = customText ?: stringResource(id = R.string.no_items),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
