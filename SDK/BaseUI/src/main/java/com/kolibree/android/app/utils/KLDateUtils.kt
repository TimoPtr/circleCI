package com.kolibree.android.app.utils

import android.annotation.SuppressLint
import java.util.Locale
import javax.inject.Inject

/**
 * @author mathilde
 * @version 28/08/2018
 *
 * Handle all date patterns
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
class KLDateUtils @Inject constructor(private val localeLanguage: Locale) {

    companion object {
        const val CHINESE_DATE_TIME = "yyyy 年 MM月dd日, hh:mm下午"
        const val FRENCH_DATE_TIME = "dd MMM yyyy, HH:mm"
        const val DEFAULT_DATE_TIME = "yyyy MMM dd, hh:mm a"

        const val CHINESE_DATE = "yyyy 年 MM月dd日"
        const val FRENCH_DATE = "dd-MM-yyyy"
        const val DEFAULT_DATE = "yyyy-MM-dd"
    }

    /**
     * Get a global pattern for a Date and Time, depend on the Local language
     */
    fun getPatternDateTimeFromLanguage(): String {
        return when {
            localeLanguage.language.toLowerCase(localeLanguage).startsWith("zh") -> CHINESE_DATE_TIME
            localeLanguage.language.toLowerCase(localeLanguage).startsWith("fr") -> FRENCH_DATE_TIME
            else -> DEFAULT_DATE_TIME
        }
    }

    /**
     * Get a global pattern for a Date, depend on the Local language
     */
    fun getPatternDateFromLanguage(): String {
        return when {
            localeLanguage.language.toLowerCase(localeLanguage).startsWith("zh") -> CHINESE_DATE
            localeLanguage.language.toLowerCase(localeLanguage).startsWith("fr") -> FRENCH_DATE
            else -> DEFAULT_DATE
        }
    }
}
