package com.example.newtestproject.util

object ChatScrollStore {
    private val positions = mutableMapOf<Long, Pair<Int, Int>>()

    fun save(otherUserId: Long, index: Int, offset: Int) {
        positions[otherUserId] = index to offset
    }

    fun get(otherUserId: Long): Pair<Int, Int>? = positions[otherUserId]
}
