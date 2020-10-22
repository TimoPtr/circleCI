package com.kolibree.android.translationssupport

import androidx.annotation.Keep
import java.util.Locale

private typealias LanguageTranslations = /* @StringRes */ Map<Int, String>

/**
 * Thread Safe
 */
@Keep
class TranslationsProvider {
    private val translationsMap: MutableMap<Locale, LanguageTranslations> = mutableMapOf()

    fun addLanguageSupport(locale: Locale, translations: LanguageTranslations) {

        synchronized(this) {
            if (translationsMap.containsKey(locale)) {
                throw IllegalArgumentException("Local $locale already added")
            }

            if (translations.isEmpty()) {
                throw IllegalArgumentException("Translations must have at least 1 key")
            }

            translationsMap[locale] = translations
        }
    }

    fun translations(): Map<Locale, LanguageTranslations> {
        synchronized(this) {
            return translationsMap.toMap()
        }
    }
}
