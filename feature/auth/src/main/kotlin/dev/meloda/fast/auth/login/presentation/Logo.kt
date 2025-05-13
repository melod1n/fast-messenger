package dev.meloda.fast.auth.login.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.theme.LocalSizeConfig

@Composable
fun Logo(
    modifier: Modifier = Modifier,
    onLogoClicked: () -> Unit = {}
) {
    val size = LocalSizeConfig.current

    val iconWidth by animateDpAsState(if (size.isWidthSmall) 110.dp else 134.dp)
    val appNameFontSize by animateIntAsState(if (size.isWidthSmall) 32 else 38)
    val bottomAdditionalPadding by animateDpAsState(if (size.isHeightSmall) 10.dp else 30.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 30.dp)
            .padding(horizontal = 30.dp)
            .padding(bottom = bottomAdditionalPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logo_big),
                contentDescription = "Application Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .width(iconWidth)
                    .combinedClickable(
                        interactionSource = null,
                        indication = null,
                        onLongClick = null,
                        onClick = onLogoClicked
                    )
            )

            Spacer(modifier = Modifier.height(46.dp))
            Text(
                text = stringResource(id = R.string.fast_messenger),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = appNameFontSize.sp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun LogoPreview() {
    Logo()
}
