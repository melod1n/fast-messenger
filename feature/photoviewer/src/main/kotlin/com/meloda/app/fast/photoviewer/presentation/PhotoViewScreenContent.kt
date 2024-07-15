package com.meloda.app.fast.photoviewer.presentation

import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.meloda.app.fast.common.model.UiImage
import com.meloda.app.fast.photoviewer.PhotoViewViewModel
import com.meloda.app.fast.photoviewer.model.PhotoViewState

@OptIn(
    ExperimentalFoundationApi::class,
)
@Composable
fun PhotoViewScreenContent(
    onBackClick: () -> Unit,
    viewModel: PhotoViewViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val images = state.images

    val pagerState = rememberPagerState(pageCount = { images.size })

    // TODO: 23/11/2023, Danil Nikolaev: заюзать штуку для цветов статус бара и навбара
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xffb00b69))
        ) {
            Spacer(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 56.dp)
            )
            Pager(
                pagerState = pagerState,
                state = state
            )
            Spacer(modifier = Modifier.navigationBarsPadding())
        }

        AppBar(onBackClick = onBackClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(onBackClick: () -> Unit) {
    val context = LocalContext.current

    var dropdownMenuShown by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back button"
                )
            }
        },
        actions = {
            IconButton(
                onClick = { dropdownMenuShown = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options"
                )
            }

            DropdownMenu(
                modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                expanded = dropdownMenuShown,
                onDismissRequest = { dropdownMenuShown = false },
                offset = DpOffset(x = (10).dp, y = (-60).dp)
            ) {
                DropdownMenuItem(
                    onClick = {
                        Toast.makeText(context, "Save clicked", Toast.LENGTH_SHORT).show()
                        dropdownMenuShown = false
                    },
                    text = { Text(text = "Save") },
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pager(
    pagerState: PagerState,
    padding: PaddingValues = PaddingValues(0.dp),
    state: PhotoViewState
) {
    val images = state.images

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        key = { index -> images[index].hashCode() }
    ) { page ->
        val model = images[page].getImage()
        if (model is Painter) {
            Image(
                painter = model,
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = model,
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize(),
                placeholder = ColorPainter(Color.DarkGray),
                error = ColorPainter(Color.Red)
            )
        }
    }
}

@Composable
fun UiImage.getImage(): Any {
    return when (this) {
        is UiImage.Color -> ColorDrawable(color)
        is UiImage.ColorResource -> ColorDrawable(colorResource(id = resId).toArgb())
        is UiImage.Resource -> painterResource(id = resId)
        is UiImage.Simple -> drawable
        is UiImage.Url -> url
    }
}
