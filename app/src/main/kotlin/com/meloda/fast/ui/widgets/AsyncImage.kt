package com.meloda.fast.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import coil.compose.AsyncImage
import coil.request.ImageRequest


/**
 * Simple wrapper for coil's AsyncImage for showing preview
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param model Either an [ImageRequest] or the [ImageRequest.data] value.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used
 * @param previewPainter Optional painter for preview
 */

@Composable
fun CoilImage(
    contentDescription: String?,
    modifier: Modifier,
    model: Any?,
    contentScale: ContentScale = ContentScale.Fit,
    previewPainter: Painter?
) {
    if (previewPainter != null && LocalView.current.isInEditMode) {
        Image(
            painter = previewPainter,
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
