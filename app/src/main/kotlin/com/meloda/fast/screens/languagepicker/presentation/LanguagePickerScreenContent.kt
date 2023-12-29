package com.meloda.fast.screens.languagepicker.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.screens.languagepicker.LanguagePickerViewModel
import com.meloda.fast.screens.languagepicker.model.SelectableLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerScreenContent(
    onLanguagePicked: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: LanguagePickerViewModel
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val languages = screenState.languages

    val selectedLanguageKey by remember(languages) {
        derivedStateOf {
            languages.find(SelectableLanguage::isSelected)?.key.orEmpty()
        }
    }

    if (screenState.isNeedToChangeLanguage) {
        viewModel.onLanguageChanged()
        onLanguagePicked(selectedLanguageKey)
    }

    val isButtonEnabled by remember(screenState) {
        derivedStateOf {
            screenState.currentLanguage != null &&
                    languages.isNotEmpty() &&
                    languages.find(SelectableLanguage::isSelected)?.key != screenState.currentLanguage
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            var dropDownMenuExpanded by remember {
                mutableStateOf(false)
            }

            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.title_application_language)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
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
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                        expanded = dropDownMenuExpanded,
                        onDismissRequest = {
                            dropDownMenuExpanded = false
                        },
                        offset = DpOffset(x = (10).dp, y = (-60).dp)
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
                itemsIndexed(screenState.languages) { index, item ->
                    LanguageItem(
                        item = item,
                        onClick = { viewModel.onLanguagePicked(index) }
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
                onClick = viewModel::onApplyButtonClicked,
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
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.language,
            modifier = Modifier.weight(1f)
        )
        if (item.isSelected) {
            Icon(
                imageVector = Icons.Rounded.Done,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}
