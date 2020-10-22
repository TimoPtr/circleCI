/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.chart.formatter

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.chart.formatter.DayValueFormatter
import java.util.Locale
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.DayOfWeek

class DayValueFormatterTests : BaseUnitTest() {

    @Test
    fun `getFormattedValue in Monday first local returns valid values`() {
        val formatter = DayValueFormatter(locale = Locale.FRANCE)

        assertEquals("", formatter.getFormattedValue((-1).toFloat()))
        assertEquals("lun.", formatter.getFormattedValue(0.toFloat()))
        assertEquals("mar.", formatter.getFormattedValue(1.toFloat()))
        assertEquals("mer.", formatter.getFormattedValue(2.toFloat()))
        assertEquals("jeu.", formatter.getFormattedValue(3.toFloat()))
        assertEquals("ven.", formatter.getFormattedValue(4.toFloat()))
        assertEquals("sam.", formatter.getFormattedValue(5.toFloat()))
        assertEquals("dim.", formatter.getFormattedValue(6.toFloat()))
        assertEquals("", formatter.getFormattedValue(7.toFloat()))
    }

    @Test
    fun `getFormattedValue in Sunday first local returns valid values`() {
        val formatter = DayValueFormatter(locale = Locale.US)

        assertEquals("", formatter.getFormattedValue((-1).toFloat()))
        assertEquals("Sun", formatter.getFormattedValue(0.toFloat()))
        assertEquals("Mon", formatter.getFormattedValue(1.toFloat()))
        assertEquals("Tue", formatter.getFormattedValue(2.toFloat()))
        assertEquals("Wed", formatter.getFormattedValue(3.toFloat()))
        assertEquals("Thu", formatter.getFormattedValue(4.toFloat()))
        assertEquals("Fri", formatter.getFormattedValue(5.toFloat()))
        assertEquals("Sat", formatter.getFormattedValue(6.toFloat()))
        assertEquals("", formatter.getFormattedValue(7.toFloat()))
    }

    @Test
    fun `indexOfDayOfWeek in Monday first local returns valid indexes`() {
        val locale = Locale.FRANCE
        assertEquals(0, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.MONDAY, locale))
        assertEquals(1, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.TUESDAY, locale))
        assertEquals(2, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.WEDNESDAY, locale))
        assertEquals(3, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.THURSDAY, locale))
        assertEquals(4, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.FRIDAY, locale))
        assertEquals(5, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.SATURDAY, locale))
        assertEquals(6, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.SUNDAY, locale))
    }

    @Test
    fun `indexOfDayOfWeek in Sunday first local returns valid values`() {
        val locale = Locale.US
        assertEquals(0, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.SUNDAY, locale))
        assertEquals(1, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.MONDAY, locale))
        assertEquals(2, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.TUESDAY, locale))
        assertEquals(3, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.WEDNESDAY, locale))
        assertEquals(4, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.THURSDAY, locale))
        assertEquals(5, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.FRIDAY, locale))
        assertEquals(6, DayValueFormatter.indexOfDayOfWeek(DayOfWeek.SATURDAY, locale))
    }
}
