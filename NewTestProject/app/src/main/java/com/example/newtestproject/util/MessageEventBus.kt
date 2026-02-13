package com.example.newtestproject.util

import com.example.newtestproject.model.MessageDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object MessageEventBus {
    private val _events = MutableSharedFlow<MessageDto>(extraBufferCapacity = 64)
    val events: SharedFlow<MessageDto> = _events

    fun emit(message: MessageDto) {
        _events.tryEmit(message)
    }
}
