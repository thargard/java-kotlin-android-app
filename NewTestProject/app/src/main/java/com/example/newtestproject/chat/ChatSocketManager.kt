package com.example.newtestproject.chat

import com.example.newtestproject.model.MessageDto
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class ChatSocketManager(
    private val baseUrl: String
) {
    private val gson = Gson()
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor()
    private var reconnectScheduled = false

    fun connect(
        token: String,
        userId: Long?,
        onMessage: (MessageDto) -> Unit,
        onError: (Throwable) -> Unit,
        onConnected: (() -> Unit)? = null,
        onDisconnected: (() -> Unit)? = null
    ) {
        if (stompClient != null) return
        val wsUrl = toWebSocketUrl(baseUrl)
        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
        client.withClientHeartbeat(10000).withServerHeartbeat(10000)
        stompClient = client

        val headers = listOf(
            StompHeader("Authorization", "Bearer $token"),
            StompHeader("heart-beat", "10000,10000")
        )

        val destinations = mutableListOf<String>()
        destinations.add("/user/queue/messages")
        if (userId != null) {
            destinations.add("/topic/messages/$userId")
        }
        destinations.forEach { destination ->
            disposables.add(
                client.topic(destination)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ stompMessage ->
                        try {
                            val dto = gson.fromJson(stompMessage.payload, MessageDto::class.java)
                            onMessage(dto)
                        } catch (e: Exception) {
                            onError(e)
                        }
                    }, { throwable ->
                        onError(throwable)
                    })
            )
        }

        disposables.add(
            client.lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    when (event.type) {
                        LifecycleEvent.Type.OPENED -> {
                            onConnected?.invoke()
                        }
                        LifecycleEvent.Type.ERROR -> {
                            onError(event.exception ?: RuntimeException("WebSocket error"))
                            cleanupClient()
                            scheduleReconnect(token, userId, onMessage, onError, onConnected, onDisconnected)
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            onDisconnected?.invoke()
                            cleanupClient()
                            scheduleReconnect(token, userId, onMessage, onError, onConnected, onDisconnected)
                        }
                        else -> Unit
                    }
                }, { throwable ->
                    onError(throwable)
                    cleanupClient()
                    scheduleReconnect(token, userId, onMessage, onError, onConnected, onDisconnected)
                })
        )

        client.connect(headers)
    }

    fun disconnect() {
        disposables.clear()
        cleanupClient()
        reconnectExecutor.shutdownNow()
    }

    private fun toWebSocketUrl(httpBaseUrl: String): String {
        val base = httpBaseUrl.trimEnd('/')
        val wsBase = base
            .replace("https://", "wss://")
            .replace("http://", "ws://")
        return "$wsBase/ws-plain"
    }

    private fun scheduleReconnect(
        token: String,
        userId: Long?,
        onMessage: (MessageDto) -> Unit,
        onError: (Throwable) -> Unit,
        onConnected: (() -> Unit)?,
        onDisconnected: (() -> Unit)?
    ) {
        if (reconnectScheduled) return
        reconnectScheduled = true
        reconnectExecutor.schedule({
            reconnectScheduled = false
            if (stompClient == null) {
                connect(token, userId, onMessage, onError, onConnected, onDisconnected)
            }
        }, 3, TimeUnit.SECONDS)
    }

    private fun cleanupClient() {
        try {
            stompClient?.disconnect()
        } catch (_: Exception) {
            // no-op
        } finally {
            disposables.clear()
            stompClient = null
        }
    }
}
