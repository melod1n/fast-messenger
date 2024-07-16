package dev.meloda.fast.auth.userbanned.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.auth.userbanned.model.UserBannedScreenState
import dev.meloda.fast.ui.R as UiR

@Preview
@Composable
fun UserBannedScreenPreview() {
    UserBannedScreen(
        screenState = UserBannedScreenState(
            userName = "Andre Shultz",
            message = "Bruteforce"
        )
    )
}

@Composable
fun UserBannedRoute(
    onBack: () -> Unit,
    userName: String,
    message: String
) {
    val screenState = remember(userName, message) {
        UserBannedScreenState(
            userName = userName,
            message = message
        )
    }

    UserBannedScreen(
        screenState = screenState,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBannedScreen(
    screenState: UserBannedScreenState = UserBannedScreenState.EMPTY,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(text = stringResource(id = UiR.string.warning))
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
                text = stringResource(id = UiR.string.account_temporarily_blocked),
                style = MaterialTheme.typography.titleLarge,

                )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(stringResource(id = UiR.string.user_name))
                        append(": ")
                    }

                    append(screenState.userName)
                }
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(stringResource(id = UiR.string.blocking_reason_title))
                        append(": ")
                    }
                    append(screenState.message)
                }
            )
        }
    }
}
