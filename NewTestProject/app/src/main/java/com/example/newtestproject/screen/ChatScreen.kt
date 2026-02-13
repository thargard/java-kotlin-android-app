package com.example.newtestproject.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newtestproject.BuildConfig
import com.example.newtestproject.R
import com.example.newtestproject.RetrofitClient
import com.example.newtestproject.chat.ChatSocketManager
import com.example.newtestproject.components.EncodeJwt
import com.example.newtestproject.model.MessageDto
import com.example.newtestproject.model.SendMessageRequest
import com.example.newtestproject.util.SessionPrefs
import com.example.newtestproject.util.ChatScrollStore
import com.example.newtestproject.util.MessageBadgeStore
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatScreen(
    otherUserId: Long,
    otherUserName: String?,
    onBack: () -> Unit,
    notConnectedMessage: String = stringResource(R.string.not_connected),
    loginRequiredMessage: String = stringResource(R.string.login_required),
    unknownErrorMessage: String = stringResource(R.string.unknownError),
    networkErrorMessage: String = stringResource(R.string.network_error),
    sendFailedMessage: String = stringResource(R.string.send_failed)
) {
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newMessage by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val token = SessionPrefs.getServerToken(context)
    val authHeader = token?.let { "Bearer $it" }
    val meId = token?.let { EncodeJwt(it)?.id }

    val socketManager = remember { ChatSocketManager(BuildConfig.BASE_URL) }
    val currentMessages by rememberUpdatedState(messages)
    val restoredPosition = remember(otherUserId) {
        ChatScrollStore.get(otherUserId) ?: (0 to 0)
    }
    val listState = rememberLazyListState(restoredPosition.first, restoredPosition.second)
    var hasNewMessages by rememberSaveable { mutableStateOf(false) }
    var didInitialScroll by rememberSaveable { mutableStateOf(false) }
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= messages.lastIndex - 1
        }
    }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(token) {
        if (token != null) {
            socketManager.connect(
                token = token,
                userId = meId,
                onMessage = { msg ->
                    val sender = msg.senderId
                    val receiver = msg.receiverId
                    if (sender == null || receiver == null || meId == null) return@connect
                    val isSameThread =
                        (sender == meId && receiver == otherUserId) ||
                            (sender == otherUserId && receiver == meId)
                        if (isSameThread) {
                            if (currentMessages.none { it.id == msg.id }) {
                                messages = currentMessages + msg
                            }
                            if (receiver == meId && msg.id != null && msg.isRead != true && authHeader != null) {
                            RetrofitClient.api.markMessageAsRead(authHeader, msg.id)
                                .enqueue(object : Callback<Map<String, Any>> {
                                    override fun onResponse(
                                        call: Call<Map<String, Any>>,
                                        response: Response<Map<String, Any>>
                                    ) {
                                        // no-op
                                    }

                                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                        // no-op
                                    }
                                })
                            MessageBadgeStore.decrement(1)
                        }
                    }
                },
                onError = { throwable ->
                    errorMessage = throwable.message ?: notConnectedMessage
                },
                onConnected = {
                    errorMessage = null
                },
                onDisconnected = {
                    errorMessage = notConnectedMessage
                }
            )
        }

        onDispose {
            ChatScrollStore.save(
                otherUserId,
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
            socketManager.disconnect()
        }
    }

    LaunchedEffect(otherUserId, authHeader) {
        if (authHeader == null) {
            isLoading = false
            errorMessage = loginRequiredMessage
            return@LaunchedEffect
        }
        RetrofitClient.api.getConversation(authHeader, otherUserId)
            .enqueue(object : Callback<com.example.newtestproject.model.ConversationMessagesResponse> {
                override fun onResponse(
                    call: Call<com.example.newtestproject.model.ConversationMessagesResponse>,
                    response: Response<com.example.newtestproject.model.ConversationMessagesResponse>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        messages = response.body()?.messages ?: emptyList()
                        val unread = messages.filter { it.receiverId == meId && it.isRead != true }
                        unread.forEach { msg ->
                            val id = msg.id ?: return@forEach
                            RetrofitClient.api.markMessageAsRead(authHeader, id)
                                .enqueue(object : Callback<Map<String, Any>> {
                                    override fun onResponse(
                                        call: Call<Map<String, Any>>,
                                        response: Response<Map<String, Any>>
                                    ) {
                                        // no-op
                                    }

                                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                        // no-op
                                    }
                                })
                        }
                        if (unread.isNotEmpty()) {
                            MessageBadgeStore.decrement(unread.size)
                        }
                    } else {
                        errorMessage = "${unknownErrorMessage}: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<com.example.newtestproject.model.ConversationMessagesResponse>, t: Throwable) {
                    isLoading = false
                    errorMessage = "${networkErrorMessage}: ${t.message}"
                }
            })
    }

    LaunchedEffect(otherUserId) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                ChatScrollStore.save(otherUserId, index, offset)
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !didInitialScroll) {
            if (restoredPosition.first == 0 && restoredPosition.second == 0) {
                listState.scrollToItem(messages.lastIndex)
            } else {
                listState.scrollToItem(restoredPosition.first, restoredPosition.second)
            }
            didInitialScroll = true
        }
        if (messages.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(messages.lastIndex)
            hasNewMessages = false
        } else if (messages.isNotEmpty() && !isAtBottom) {
            hasNewMessages = true
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            hasNewMessages = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (otherUserName.isNullOrBlank()) {
                "${stringResource(id = R.string.chat_with)} $otherUserId"
            } else {
                "${stringResource(id = R.string.chat_with)} $otherUserName"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: stringResource(id = R.string.unknownError),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState
                    ) {
                        itemsIndexed(messages) { _, msg ->
                            MessageBubble(msg = msg, isMine = msg.senderId == meId)
                        }
                    }
                    if (hasNewMessages) {
                        Button(
                            onClick = {
                                if (messages.isNotEmpty()) {
                                    hasNewMessages = false
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(messages.lastIndex)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        ) {
                            Text(stringResource(id = R.string.new_messages))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newMessage,
            onValueChange = { newMessage = it },
            label = { Text(stringResource(id = R.string.type_message)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (authHeader == null || sending) {
                    return@Button
                }
                val content = newMessage.trim()
                if (content.isEmpty()) return@Button
                sending = true
                RetrofitClient.api.sendMessage(
                    authHeader,
                    SendMessageRequest(receiverId = otherUserId, content = content)
                ).enqueue(object : Callback<MessageDto> {
                    override fun onResponse(call: Call<MessageDto>, response: Response<MessageDto>) {
                        sending = false
                        if (response.isSuccessful) {
                            val msg = response.body()
                            if (msg != null && messages.none { it.id == msg.id }) {
                                messages = messages + msg
                            }
                            newMessage = ""
                        } else {
                            Toast.makeText(context, sendFailedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MessageDto>, t: Throwable) {
                        sending = false
                        Toast.makeText(context, sendFailedMessage, Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.send))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.back))
        }
    }
}

@Composable
private fun MessageBubble(msg: MessageDto, isMine: Boolean) {
    val align = if (isMine) Alignment.End else Alignment.Start
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = if (isMine) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isMine && !msg.senderName.isNullOrBlank()) {
                    Text(
                        text = msg.senderName,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = msg.content ?: "",
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(msg.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
