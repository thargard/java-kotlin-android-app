package com.example.newtestproject.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {

    fun applyLanguage(context: Context, languageTag: String?) {
        val locales = if (languageTag.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }

        AppCompatDelegate.setApplicationLocales(locales)
        LanguagePrefs.saveLanguage(context, languageTag)

        (context.applicationContext as? com.example.newtestproject.MainActivity)?.recreate()
    }

    fun setLanguage(languageTag: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun currentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "" else locales.toLanguageTags()
    }
}