package dev.meloda.fast.profile.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.meloda.fast.model.api.domain.OnlineStatus
import dev.meloda.fast.profile.model.ProfileScreenState
import dev.meloda.fast.ui.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileRoute(
    screenState: ProfileScreenState,
    onBack: (() -> Unit)? = null,
    onSendMessageClicked: (userId: Long) -> Unit = {},
    onSettingsButtonClicked: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    ProfileScreen(
        screenState = screenState,
        onBack = onBack,
        onSendMessageClicked = onSendMessageClicked,
        onSettingsButtonClicked = onSettingsButtonClicked,
        onPhotoClicked = onPhotoClicked,
        onRefresh = onRefresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    screenState: ProfileScreenState = ProfileScreenState.EMPTY,
    onBack: (() -> Unit)? = null,
    onSendMessageClicked: (userId: Long) -> Unit = {},
    onSettingsButtonClicked: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (onBack != null && screenState.user != null) {
                        Text(
                            text = screenState.user.firstName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back_round_24),
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (screenState.isCurrentUser) {
                        IconButton(onClick = onSettingsButtonClicked) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_round_24),
                                contentDescription = "Settings"
                            )
                        }
                    } else if (screenState.user != null) {
                        val profileUrl = "https://vk.com/${screenState.user.screenName ?: ("id" + screenState.user.id)}"
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, profileUrl)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, screenState.user.fullName))
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_forward_round_24),
                                contentDescription = "Share"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .padding(bottom = padding.calculateBottomPadding()),
            state = pullToRefreshState,
            isRefreshing = screenState.isLoading,
            onRefresh = onRefresh
        ) {
            val user = screenState.user

            if (user == null && screenState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (user != null) {
                val status = user.status
                val birthday = user.birthday
                val about = user.about
                val site = user.site
                val followersCount = user.followersCount

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar with online badge
                    Box(modifier = Modifier.size(120.dp)) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .clickable {
                                    val photo = user.photo400Orig ?: user.photo200 ?: user.photo100
                                    if (!photo.isNullOrBlank()) {
                                        onPhotoClicked(photo)
                                    }
                                },
                            model = user.photo400Orig ?: user.photo200 ?: user.photo100,
                            contentDescription = user.fullName,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_account_circle_fill_round_24)
                        )

                        if (user.onlineStatus.isOnline()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(3.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name + Verified
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (user.verified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_round_24),
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .padding(2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Online / Last Seen text
                    val onlineText = formatOnlineStatus(user.onlineStatus, user.lastSeen, user.lastSeenStatus, user.sex)
                    Text(
                        text = onlineText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (user.onlineStatus.isOnline()) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Status Quote
                    if (!status.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!screenState.isCurrentUser) {
                            Button(
                                onClick = { onSendMessageClicked(user.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mail_fill_round_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Написать")
                            }

                            val profileUrl = "https://vk.com/${user.screenName ?: ("id" + user.id)}"
                            FilledTonalButton(
                                onClick = {
                                    copyToClipboard(context, profileUrl, "Ссылка на профиль скопирована")
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_content_copy_round_24),
                                    contentDescription = "Копировать ссылку",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = onSettingsButtonClicked,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_settings_round_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.title_settings))
                            }

                            val profileUrl = "https://vk.com/${user.screenName ?: ("id" + user.id)}"
                            FilledTonalButton(
                                onClick = {
                                    copyToClipboard(context, profileUrl, "Ссылка на профиль скопирована")
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_content_copy_round_24),
                                    contentDescription = "Копировать ссылку",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Information Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // User ID / Screen Name
                            val username = user.screenName ?: "id${user.id}"
                            ProfileInfoItem(
                                icon = painterResource(id = R.drawable.ic_account_circle_round_24),
                                title = "ID / Никнейм",
                                value = "@$username",
                                onClick = {
                                    copyToClipboard(context, username, "ID скопирован")
                                }
                            )

                            // Birthday
                            if (!birthday.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                ProfileInfoItem(
                                    icon = painterResource(id = R.drawable.ic_cake_fill_round_24),
                                    title = "День рождения",
                                    value = birthday
                                )
                            }

                            // City / Country
                            val location = listOfNotNull(user.city, user.country).joinToString(", ")
                            if (location.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                ProfileInfoItem(
                                    icon = painterResource(id = R.drawable.ic_map_fill_round_24),
                                    title = "Город",
                                    value = location
                                )
                            }

                            // About
                            if (!about.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                ProfileInfoItem(
                                    icon = painterResource(id = R.drawable.ic_info_round_24),
                                    title = "О себе",
                                    value = about
                                )
                            }

                            // Site
                            if (!site.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                ProfileInfoItem(
                                    icon = painterResource(id = R.drawable.ic_language_round_24),
                                    title = "Сайт",
                                    value = site,
                                    onClick = {
                                        val url = if (!site.startsWith("http://") && !site.startsWith("https://")) {
                                            "https://$site"
                                        } else site
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    }
                                )
                            }

                            // Followers count
                            if (followersCount != null && followersCount > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                                ProfileInfoItem(
                                    icon = painterResource(id = R.drawable.ic_group_fill_round_24),
                                    title = "Подписчики",
                                    value = "$followersCount"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else if (screenState.isError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Не удалось загрузить профиль",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRefresh) {
                            Text(text = "Повторить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: Painter,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("profile", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}

private fun formatOnlineStatus(
    status: OnlineStatus,
    lastSeenTime: Int?,
    lastSeenStatus: String?,
    sex: Int?
): String {
    val isFemale = (sex == 1)

    return when (status) {
        is OnlineStatus.OnlineMobile -> "В сети (моб.)"
        is OnlineStatus.Online -> "В сети"
        OnlineStatus.Recently -> if (isFemale) "Была недавно" else "Был недавно"
        OnlineStatus.LastWeek -> if (isFemale) "Была на этой неделе" else "Был на этой неделе"
        OnlineStatus.LastMonth -> if (isFemale) "Была в этом месяце" else "Был в этом месяце"
        OnlineStatus.Offline -> {
            if (lastSeenTime != null && lastSeenTime > 0) {
                val date = Date(lastSeenTime.toLong() * 1000)
                val format = SimpleDateFormat("dd.MM.yyyy в HH:mm", Locale.getDefault())
                val prefix = if (isFemale) "Была в сети" else "Был в сети"
                "$prefix ${format.format(date)}"
            } else if (lastSeenStatus != null) {
                when (lastSeenStatus) {
                    "last_week" -> if (isFemale) "Была на этой неделе" else "Был на этой неделе"
                    "last_month" -> if (isFemale) "Была в этом месяце" else "Был в этом месяце"
                    else -> if (isFemale) "Была недавно" else "Был недавно"
                }
            } else {
                "Не в сети"
            }
        }
    }
}
