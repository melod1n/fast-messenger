package dev.meloda.fast.settings.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.settings.model.HapticType
import dev.meloda.fast.settings.model.SettingsScreenState
import dev.meloda.fast.settings.model.UiItem
import dev.meloda.fast.settings.presentation.item.ListItem
import dev.meloda.fast.settings.presentation.item.SwitchItem
import dev.meloda.fast.settings.presentation.item.TextFieldItem
import dev.meloda.fast.settings.presentation.item.TitleItem
import dev.meloda.fast.settings.presentation.item.TitleTextItem
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.R


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun SettingsScreen(
    screenState: SettingsScreenState = SettingsScreenState.EMPTY,
    hapticType: HapticType? = null,
    onBack: () -> Unit = {},
    onHapticPerformed: () -> Unit = {},
    onSettingsItemClicked: (key: String) -> Unit = {},
    onSettingsItemLongClicked: (key: String) -> Unit = {},
    onSettingsItemValueChanged: (key: String, newValue: Any?) -> Unit = { _, _ -> }
) {
    val view = LocalView.current

    LaunchedEffect(hapticType) {
        if (hapticType != null) {
            if (AppSettings.General.enableHaptic) {
                view.performHapticFeedback(hapticType.getHaptic())
            }
            onHapticPerformed()
        }
    }

    val themeConfig = LocalThemeConfig.current

    val hazeState = remember { HazeState(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_back_24),
                            contentDescription = "Back button"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = if (themeConfig.enableBlur) 0f else 1f
                    )
                ),
                modifier = Modifier
                    .then(
                        if (themeConfig.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.regular()
                            )
                        } else {
                            Modifier
                        }
                    )
                    .fillMaxWidth()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .then(
                    if (themeConfig.enableBlur) {
                        Modifier.hazeSource(state = hazeState)
                    } else Modifier
                )
                .fillMaxWidth()
                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            }

            items(
                items = screenState.settings,
                key = UiItem::key,
                contentType = { item ->
                    when (item) {
                        is UiItem.Title -> "title"
                        is UiItem.TitleText -> "title_text"
                        is UiItem.Switch -> "switch"
                        is UiItem.TextField -> "text_field"
                        is UiItem.List<*> -> "list"
                    }
                }
            ) { item ->
                when (item) {
                    is UiItem.Title -> {
                        TitleItem(item = item)
                    }

                    is UiItem.TitleText -> {
                        TitleTextItem(
                            item = item,
                            onClick = onSettingsItemClicked,
                            onLongClick = onSettingsItemLongClicked
                        )
                    }

                    is UiItem.Switch -> {
                        SwitchItem(
                            item = item,
                            onClick = { onSettingsItemClicked(item.key) },
                            onLongClick = { onSettingsItemLongClicked(item.key) },
                            onChanged = { onSettingsItemValueChanged(item.key, it) }
                        )
                    }

                    is UiItem.TextField -> {
                        TextFieldItem(
                            item = item,
                            onClick = { onSettingsItemClicked(item.key) },
                            onLongClick = { onSettingsItemLongClicked(item.key) },
                            onChanged = { onSettingsItemValueChanged(item.key, it) }
                        )
                    }

                    is UiItem.List<*> -> {
                        ListItem(
                            item = item,
                            onClick = { onSettingsItemClicked(item.key) },
                            onLongClick = { onSettingsItemLongClicked(item.key) },
                            onChanged = { onSettingsItemValueChanged(item.key, it) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
