package dev.meloda.fast.convos.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.vk.UiFriend


@Composable
fun CreateChatItem(
    modifier: Modifier = Modifier,
    friend: UiFriend,
    maxLines: Int,
    isSelected: Boolean,
    onItemClicked: (Long) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClicked(friend.userId) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))

        val friendAvatar = friend.avatar?.extractUrl()

        Box(modifier = Modifier.size(56.dp)) {
            if (friendAvatar == null) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.ic_account_circle_cut),
                    contentDescription = "Avatar",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            } else {
                AsyncImage(
                    model = friendAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.ic_account_circle_cut)
                )
            }

            if (friend.onlineStatus.isOnline()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = friend.title,
            minLines = 1,
            maxLines = maxLines,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onItemClicked(friend.userId) },
        )

        Spacer(modifier = Modifier.width(16.dp))
    }
}

