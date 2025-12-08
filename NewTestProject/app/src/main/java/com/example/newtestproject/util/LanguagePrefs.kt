package com.example.newtestproject.util

import android.content.Context

object LanguagePrefs {

    private const val PREFS = "app_prefs"
    private const val KEY_LANG = "pref_language"

    fun saveLanguage(context: Context, tag: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, tag)
            .apply()
    }

    fun loadLanguage(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null)
    }
}