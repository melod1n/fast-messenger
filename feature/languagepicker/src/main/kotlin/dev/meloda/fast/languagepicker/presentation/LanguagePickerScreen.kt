package dev.meloda.fast.languagepicker.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.languagepicker.LanguagePickerViewModel
import dev.meloda.fast.languagepicker.LanguagePickerViewModelImpl
import dev.meloda.fast.languagepicker.model.LanguagePickerScreenState
import dev.meloda.fast.languagepicker.model.SelectableLanguage
import dev.meloda.fast.ui.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun LanguagePickerRoute(
    onBack: () -> Unit,
    viewModel: LanguagePickerViewModel = koinViewModel<LanguagePickerViewModelImpl>()
) {
    LifecycleResumeEffect(true) {
        viewModel.updateCurrentLocale(AppCompatDelegate.getApplicationLocales().toLanguageTags())
        onPauseOrDispose {}
    }

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LanguagePickerScreen(
        screenState = screenState,
        onBack = onBack,
        onLanguagePicked = viewModel::onLanguagePicked,
        onApplyButtonClicked = viewModel::onApplyButtonClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerScreen(
    screenState: LanguagePickerScreenState = LanguagePickerScreenState.EMPTY,
    onBack: () -> Unit = {},
    onLanguagePicked: (SelectableLanguage) -> Unit = {},
    onApplyButtonClicked: () -> Unit = {}
) {
    val context = LocalContext.current

    val isButtonEnabled by remember(screenState) {
        derivedStateOf {
            screenState.currentLanguage != null &&
                    screenState.languages.isNotEmpty() &&
                    screenState.languages.find(SelectableLanguage::isSelected)?.key != screenState.currentLanguage
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            var dropDownMenuExpanded by remember {
                mutableStateOf(false)
            }

            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_application_language),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.round_arrow_back_24px),
                            contentDescription = "Navigate back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        return@LargeTopAppBar
                    }

                    IconButton(
                        onClick = {
                            dropDownMenuExpanded = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_more_vert_24px),
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                        expanded = dropDownMenuExpanded,
                        onDismissRequest = {
                            dropDownMenuExpanded = false
                        },
                        offset = DpOffset(x = (-10).dp, y = (-60).dp)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                dropDownMenuExpanded = false

                                context.startActivity(
                                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                        data = Uri.fromParts(
                                            "package",
                                            context.packageName,
                                            null
                                        )
                                    }
                                )
                            },
                            text = {
                                Text(text = stringResource(id = R.string.open_system_language_picker))
                            }
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = screenState.languages.toList(),
                    key = SelectableLanguage::key
                ) { item ->
                    LanguageItem(
                        item = item,
                        onClick = onLanguagePicked
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .height(64.dp)
                            .navigationBarsPadding()
                            .padding(bottom = 4.dp)
                    )
                }
            }

            Button(
                onClick = onApplyButtonClicked,
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 4.dp)
                    .align(Alignment.BottomCenter)
                    .height(64.dp)
            ) {
                Text(text = stringResource(id = R.string.action_apply))
            }
        }
    }
}

@Composable
fun LanguageItem(
    item: SelectableLanguage,
    onClick: (item: SelectableLanguage) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick(item) }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = item.isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = item.language.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(0.7f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.local
            )
        }
    }
}
