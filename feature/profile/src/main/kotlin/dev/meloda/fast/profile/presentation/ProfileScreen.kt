package dev.meloda.fast.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.meloda.fast.profile.model.ProfileScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.SegmentedButtonItem
import dev.meloda.fast.ui.components.SegmentedButtonsRow
import dev.meloda.fast.ui.util.buildImmutableList

@Composable
fun ProfileRoute(
    screenState: ProfileScreenState,
    onSettingsButtonClicked: () -> Unit,
    onPhotoClicked: (url: String) -> Unit,
) {
    ProfileScreen(
        screenState = screenState,
        onSettingsButtonClicked = onSettingsButtonClicked,
        onPhotoClicked = onPhotoClicked
    )
}

// TODO: 13/07/2024, Danil Nikolaev: handle expired session
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    screenState: ProfileScreenState = ProfileScreenState.EMPTY,
    onSettingsButtonClicked: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    val items = buildImmutableList {
                        add(SegmentedButtonItem("settings", R.drawable.ic_settings_round_24))
                    }

                    SegmentedButtonsRow(
                        modifier = Modifier.padding(end = 8.dp),
                        items = items,
                        onClick = { index ->
                            when (items[index].key) {
                                "settings" -> onSettingsButtonClicked()
                            }
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .padding(bottom = padding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (screenState.fullName == null && screenState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(24.dp))

                AsyncImage(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable {
                            onPhotoClicked(screenState.avatarUrl.orEmpty())
                        },
                    model = screenState.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_account_circle_fill_round_24)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = screenState.fullName.orEmpty(),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}
