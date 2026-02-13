package com.example.newtestproject.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.util.SessionPrefs
import com.example.newtestproject.components.EncodeJwt
import com.example.newtestproject.model.ConversationDto
import com.example.newtestproject.util.MessageBadgeStore
import com.example.newtestproject.util.MessageEventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MessagesScreen(
    onOpenChat: (Long, String?) -> Unit,
    onBack: () -> Unit,
    loginRequiredMessage: String = stringResource(R.string.login_required),
    unknownErrorMessage: String = stringResource(R.string.unknownError),
    networkErrorMessage: String = stringResource(R.string.network_error)
) {
    var conversations by remember { mutableStateOf<List<ConversationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val authHeader = token?.let { "Bearer $it" }
    val meId = token?.let { EncodeJwt(it)?.id }
    val currentConversations by rememberUpdatedState(conversations)

    LaunchedEffect(authHeader) {
        if (authHeader == null) {
            isLoading = false
            errorMessage = loginRequiredMessage
            return@LaunchedEffect
        }
        RetrofitClient.api.getConversations(authHeader)
            .enqueue(object : Callback<com.example.newtestproject.model.ConversationsResponse> {
                override fun onResponse(
                    call: Call<com.example.newtestproject.model.ConversationsResponse>,
                    response: Response<com.example.newtestproject.model.ConversationsResponse>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        conversations = response.body()?.conversations ?: emptyList()
                        MessageBadgeStore.setTotal(conversations.sumOf { it.unreadCount ?: 0 }.toInt())
                    } else {
                        errorMessage = "${unknownErrorMessage}: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<com.example.newtestproject.model.ConversationsResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "${networkErrorMessage}: ${t.message}"
                }
            })
    }

    LaunchedEffect(meId) {
        if (meId == null) return@LaunchedEffect
        MessageEventBus.events.collect { msg ->
            val sender = msg.senderId
            val receiver = msg.receiverId
            if (sender == null || receiver == null) return@collect
            if (sender != meId && receiver != meId) return@collect
            val otherId = if (sender == meId) receiver else sender
            val otherName = if (sender == meId) msg.receiverName else msg.senderName

                    val updated = currentConversations.toMutableList()
                    val idx = updated.indexOfFirst { it.otherUserId == otherId }
                    if (idx >= 0) {
                        val convo = updated[idx]
                        val nextUnread = if (receiver == meId) (convo.unreadCount ?: 0L) + 1L else (convo.unreadCount ?: 0L)
                        updated[idx] = ConversationDto(
                            otherUserId = convo.otherUserId,
                            otherUserName = convo.otherUserName,
                            lastMessage = msg.content,
                            lastMessageAt = msg.createdAt,
                            unreadCount = nextUnread,
                            isLastMessageFromMe = sender == meId
                        )
                    } else {
                        val unread = if (receiver == meId) 1L else 0L
                        updated.add(
                            ConversationDto(
                                otherUserId = otherId,
                                otherUserName = otherName ?: "ID $otherId",
                                lastMessage = msg.content,
                                lastMessageAt = msg.createdAt,
                                unreadCount = unread,
                                isLastMessageFromMe = sender == meId
                            )
                        )
                    }
            updated.sortByDescending { it.lastMessageAt ?: "" }
            conversations = updated
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.messages_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: stringResource(id = R.string.unknownError),
                    color = MaterialTheme.colorScheme.error
                )
            }
            conversations.isEmpty() -> {
                Text(text = stringResource(id = R.string.messages_empty))
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(conversations) { convo ->
                        ConversationCard(
                            convo = convo,
                            onOpenChat = { otherId, otherName, unread ->
                                if (unread > 0) {
                                    MessageBadgeStore.decrement(unread.toInt())
                                    convo.unreadCount = 0
                                }
                                onOpenChat(otherId, otherName)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.back))
        }
    }
}

@Composable
private fun ConversationCard(
    convo: ConversationDto,
    onOpenChat: (Long, String?, Long) -> Unit
) {
    val otherId = convo.otherUserId ?: return
    val name = convo.otherUserName?.takeIf { it.isNotBlank() } ?: "ID $otherId"
    val lastTime = formatTimestamp(convo.lastMessageAt)
    val unread = convo.unreadCount ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat(otherId, convo.otherUserName, unread) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (unread > 0) {
                    BadgedBox(badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(text = unread.toString(), fontSize = 11.sp)
                        }
                    }) {
                        Box(modifier = Modifier)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = convo.lastMessage ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastTime,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                if (unread > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(text = unread.toString(), fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        ""
    }
}
