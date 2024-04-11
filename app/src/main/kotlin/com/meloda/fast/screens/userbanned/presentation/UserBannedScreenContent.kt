package com.meloda.fast.screens.userbanned.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.meloda.fast.R
import com.meloda.fast.ui.AppTheme

@Preview
@Composable
fun UserBannedScreenPreview() {
    AppTheme {
        UserBannedScreenContent(
            onBackClick = {},
            name = "Calvin Harris",
            message = "Eto konets",
            isLastScreenLogin = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBannedScreenContent(
    onBackClick: () -> Unit,
    name: String,
    message: String,
    isLastScreenLogin: Boolean
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (isLastScreenLogin) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close button"
                            )
                        }
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.warning))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.account_temporarily_blocked),
                style = MaterialTheme.typography.titleLarge,

                )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(stringResource(id = R.string.user_name))
                        append(": ")
                    }

                    append(name)
                }
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(stringResource(id = R.string.blocking_reason_title))
                        append(": ")
                    }
                    append(message)
                }
            )
        }
    }
}
