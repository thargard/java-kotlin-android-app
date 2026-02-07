package com.example.newtestproject.util

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.newtestproject.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)

object CartStore {
    private const val PREFS = "cart_prefs"
    private const val KEY_CART = "cart_items"

    private val gson = Gson()
    private val itemsState = mutableStateListOf<CartItem>()
    private var isInitialized = false
    private var appContext: Context? = null

    val items: SnapshotStateList<CartItem>
        get() = itemsState

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        val saved = loadSavedItems()
        if (saved.isNotEmpty()) {
            itemsState.clear()
            itemsState.addAll(saved)
        }
        isInitialized = true
    }

    fun add(product: Product) {
        val productId = product.id
        if (productId == null) {
            itemsState.add(CartItem(product))
            persist()
            return
        }
        val existing = itemsState.firstOrNull { it.product.id == productId }
        if (existing != null) {
            existing.quantity += 1
        } else {
            itemsState.add(CartItem(product))
        }
        persist()
    }

    fun remove(productId: Long?) {
        if (productId == null) return
        itemsState.removeAll { it.product.id == productId }
        persist()
    }

    fun clear() {
        itemsState.clear()
        persist()
    }

    fun totalPrice(): Double {
        return itemsState.sumOf { item -> (item.product.price ?: 0.0) * item.quantity }
    }

    private fun persist() {
        val context = appContext ?: return
        val json = gson.toJson(itemsState.toList())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CART, json)
            .apply()
    }

    private fun loadSavedItems(): List<CartItem> {
        val context = appContext ?: return emptyList()
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CART, null)
            ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson<List<CartItem>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
