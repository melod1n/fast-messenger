package dev.meloda.fast.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.R

@Composable
fun VkErrorView(
    modifier: Modifier = Modifier,
    baseError: BaseError,
    onButtonClick: () -> Unit = {}
) {
    when (baseError) {
        is BaseError.SessionExpired -> {
            ErrorView(
                modifier = modifier,
                text = stringResource(R.string.session_expired),
                buttonText = stringResource(R.string.action_log_out),
                onButtonClick = onButtonClick
            )
        }

        is BaseError.SimpleError -> {
            ErrorView(
                modifier = modifier,
                text = baseError.message,
                buttonText = stringResource(R.string.try_again),
                onButtonClick = onButtonClick
            )
        }

        BaseError.AccountBlocked -> {
            ErrorView(
                modifier = modifier,
                text = "Account blocked",
                buttonText = stringResource(R.string.action_log_out),
                onButtonClick = onButtonClick
            )
        }

        BaseError.ConnectionError -> {
            ErrorView(
                modifier = modifier,
                text = "Connection error",
                buttonText = stringResource(R.string.try_again),
                onButtonClick = onButtonClick
            )
        }

        BaseError.InternalError -> {
            ErrorView(
                modifier = modifier,
                text = "Internal error",
                buttonText = stringResource(R.string.try_again),
                onButtonClick = onButtonClick
            )
        }

        BaseError.UnknownError -> {
            ErrorView(
                modifier = modifier,
                text = "Unknown error",
                buttonText = stringResource(R.string.try_again),
                onButtonClick = onButtonClick
            )
        }
    }
}
