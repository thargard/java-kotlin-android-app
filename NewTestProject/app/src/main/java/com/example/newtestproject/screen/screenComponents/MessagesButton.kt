package com.example.newtestproject.screen.screenComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.newtestproject.R
import com.example.newtestproject.util.MessageBadgeStore

@Composable
fun MessagesButton(
    navController: NavController,
    onClick: () -> Unit = { navController.navigate("messages") { launchSingleTop = true } }
) {
    val unread = MessageBadgeStore.totalUnread
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unread > 0) {
                    Badge {
                        androidx.compose.material3.Text(
                            text = if (unread > 99) "99+" else unread.toString(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = stringResource(id = R.string.messages)
            )
        }
    }
}
