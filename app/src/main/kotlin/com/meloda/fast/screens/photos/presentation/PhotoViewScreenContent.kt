package com.meloda.fast.screens.photos.presentation

import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
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
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.meloda.fast.R
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.getImage
import com.meloda.fast.screens.photos.PhotoViewViewModel
import com.meloda.fast.screens.photos.PhotoViewViewModelImpl
import com.meloda.fast.ui.AppTheme

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun PhotoViewScreenContent(
    onBackClick: () -> Unit,
    images: List<UiImage>,
    viewModel: PhotoViewViewModel
) {
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
                images = images
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
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
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
    images: List<UiImage>
) {
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
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                placeholder = ColorPainter(Color.DarkGray),
                error = ColorPainter(Color.Red)
            )
        }
    }
}

@Preview
@Composable
fun PhotoViewScreenContentPreview() {
    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_logo_big)

    val links = remember {
        listOf(
            "https://randompicturegenerator.com/img/cat-generator/g0da280343fac29a84fbf3dd8b50d8ce8dffcb92b9d8b8aa0af14c10409eed64367e042a5fec9642fc117abaf2e7bc529_640.jpg",
            "https://randompicturegenerator.com/img/cat-generator/gdcc35fa367a4f18c23f14c6a336b5c644f993ab6b29f0df0bea189a571335e0944a717d096c0fc2d0f24ff8c3f634581_640.jpg",
            "https://randompicturegenerator.com/img/cat-generator/gaa28b24267c9c888d116f1f26c63cbf60c312f80add206905ea059fea409bb0b80043fbd0f8dc6e79d19829876ec2f75_640.jpg",
            "https://randompicturegenerator.com/img/cat-generator/g3773e595a8ef5a3349ff896ac0b42959bd4c1f7136715552cabc8b9f1c2d42add537ace78a88b27b6fa6123c7f76eaa0_640.jpg",
        )
    }
    val resources = remember {
        listOf(
            R.drawable.test_captcha,
            R.drawable.ic_fast_logo,
        )
    }

    val images =
        List(links.size) { index ->
            UiImage.Url(url = links[index])
        } + List(resources.size) { index ->
            UiImage.Resource(resId = resources[index])
        } + UiImage.Color(Color.Cyan.toArgb())

    AppTheme(
        useDarkTheme = true,
        useDynamicColors = false
    ) {
        PhotoViewScreenContent(
            onBackClick = {},
            images = images,
            viewModel = PhotoViewViewModelImpl()
        )
    }
}
