package com.example.newtestproject.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max

object MessageBadgeStore {
    var totalUnread by mutableStateOf(0)
        private set

    fun setTotal(count: Int) {
        totalUnread = max(0, count)
    }

    fun increment(by: Int = 1) {
        totalUnread = max(0, totalUnread + by)
    }

    fun decrement(by: Int = 1) {
        totalUnread = max(0, totalUnread - by)
    }
}
