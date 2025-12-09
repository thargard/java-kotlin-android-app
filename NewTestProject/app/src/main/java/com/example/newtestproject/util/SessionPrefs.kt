package com.example.newtestproject.util

import android.content.Context

object SessionPrefs {
    private const val PREFS = "session_prefs"
    private const val KEY_SERVER_TOKEN = "server_token"
    private const val KEY_ID_TOKEN = "google_id_token"

    fun saveTokens(context: Context, serverToken: String?, idToken: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SERVER_TOKEN, serverToken)
            .putString(KEY_ID_TOKEN, idToken)
            .apply()
    }

    fun getServerToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SERVER_TOKEN, null)

    fun getGoogleIdToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ID_TOKEN, null)
}


