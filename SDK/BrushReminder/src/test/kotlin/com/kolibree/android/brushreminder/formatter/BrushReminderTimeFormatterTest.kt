/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.formatter

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalTime

class BrushReminderTimeFormatterTest : BaseUnitTest() {

    lateinit var brushReminderTimeFormatter: BrushReminderTimeFormatter

    private val androidTimeFormat: AndroidTimeFormat = mock()

    @Before
    fun setUp() {
        brushReminderTimeFormatter = BrushReminderTimeFormatter(androidTimeFormat)
    }

    @Test
    fun `when user has 24HourFormat then the right formatter should be used`() {
        val onePastThirtyOfTheAfternoon = LocalTime.of(13, 30)

        whenever(androidTimeFormat.is24HourFormat())
            .thenReturn(true)

        val formattedTime = brushReminderTimeFormatter.format(onePastThirtyOfTheAfternoon)

        assertEquals("13:30", formattedTime)
    }

    @Test
    fun `when user has 12HourFormat then the right formatter should be used`() {
        val onePastThirtyOfTheAfternoon = LocalTime.of(13, 30)

        whenever(androidTimeFormat.is24HourFormat())
            .thenReturn(false)

        val formattedTime = brushReminderTimeFormatter.format(onePastThirtyOfTheAfternoon)

        assertEquals("01:30 PM", formattedTime)
    }
}
