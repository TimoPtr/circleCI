/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.view

import com.kolibree.android.app.test.BaseUnitTest
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

internal class WeekDayLabelsTest : BaseUnitTest() {

    @Test
    fun `Sunday is first day of week for Locale US`() {
        val weekLabels = WeekDayLabels.create(Locale.US)
        assertEquals("S", weekLabels.dayLabelAt(0))
        assertEquals("M", weekLabels.dayLabelAt(1))
        assertEquals("T", weekLabels.dayLabelAt(2))
        assertEquals("W", weekLabels.dayLabelAt(3))
        assertEquals("T", weekLabels.dayLabelAt(4))
        assertEquals("F", weekLabels.dayLabelAt(5))
        assertEquals("S", weekLabels.dayLabelAt(6))
    }

    @Test
    fun `Monday is first day of week for Locale UK`() {
        val weekLabels = WeekDayLabels.create(Locale.UK)
        assertEquals("M", weekLabels.dayLabelAt(0))
        assertEquals("T", weekLabels.dayLabelAt(1))
        assertEquals("W", weekLabels.dayLabelAt(2))
        assertEquals("T", weekLabels.dayLabelAt(3))
        assertEquals("F", weekLabels.dayLabelAt(4))
        assertEquals("S", weekLabels.dayLabelAt(5))
        assertEquals("S", weekLabels.dayLabelAt(6))
    }
}
