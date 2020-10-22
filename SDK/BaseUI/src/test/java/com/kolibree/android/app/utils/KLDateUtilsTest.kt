package com.kolibree.android.app.utils

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author mathilde
 * @version 28/08/2018
 */
class KLDateUtilsTest : BaseUnitTest() {
    private lateinit var KLDateUtil: KLDateUtils
    private var locale = mock<Locale>()

    override fun setup() {
        super.setup()

        KLDateUtil = KLDateUtils(locale)
    }

    @Test
    fun date_time_format_for_chinese_language() {
        whenever(locale.language).thenReturn(Locale.CHINA.language)
        assertEquals(KLDateUtils.CHINESE_DATE_TIME, KLDateUtil.getPatternDateTimeFromLanguage())
    }

    @Test
    fun date_time_format_for_french_language() {
        whenever(locale.language).thenReturn(Locale.FRANCE.language)
        assertEquals(KLDateUtils.FRENCH_DATE_TIME, KLDateUtil.getPatternDateTimeFromLanguage())
    }

    @Test
    fun date_time_format_for_default_language() {
        whenever(locale.language).thenReturn(Locale.GERMAN.language)
        assertEquals(KLDateUtils.DEFAULT_DATE_TIME, KLDateUtil.getPatternDateTimeFromLanguage())
    }

    @Test
    fun date_format_for_chinese_language() {
        whenever(locale.language).thenReturn(Locale.CHINA.language)
        assertEquals(KLDateUtils.CHINESE_DATE, KLDateUtil.getPatternDateFromLanguage())
    }

    @Test
    fun date_format_for_french_language() {
        whenever(locale.language).thenReturn(Locale.FRANCE.language)
        assertEquals(KLDateUtils.FRENCH_DATE, KLDateUtil.getPatternDateFromLanguage())
    }

    @Test
    fun date_format_for_default_language() {
        whenever(locale.language).thenReturn(Locale.GERMAN.language)
        assertEquals(KLDateUtils.DEFAULT_DATE, KLDateUtil.getPatternDateFromLanguage())
    }
}
