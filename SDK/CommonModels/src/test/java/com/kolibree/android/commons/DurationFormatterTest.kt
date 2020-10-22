/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DurationFormatterTest {

    private lateinit var formatter: DurationFormatter

    @Before
    fun setup() {
        formatter = DurationFormatter()
    }

    @Test
    fun `for 0 elapsed seconds method returns 00-00`() {
        assertEquals("00:00", formatter.format(0))
    }

    @Test
    fun `for 9 elapsed seconds method returns 00-09`() {
        assertEquals("00:09", formatter.format(9))
    }

    @Test
    fun `for 59 elapsed seconds method returns 00-59`() {
        assertEquals("00:59", formatter.format(59))
    }

    @Test
    fun `for 60 elapsed seconds method returns 01-00`() {
        assertEquals("01:00", formatter.format(60))
    }

    @Test
    fun `for 61 elapsed seconds method returns 01-01`() {
        assertEquals("01:01", formatter.format(61))
    }

    @Test
    fun `method without first leading zero for 9 elapsed seconds returns 0-09`() {
        assertEquals("0:09", formatter.format(9, false))
    }

    @Test
    fun `SECONDS_IN_MINUTE is 60`() {
        assertEquals(60, SECONDS_IN_MINUTE)
    }

    @Test
    fun `TWO_DIGITS is 10`() {
        assertEquals(10, TWO_DIGITS)
    }

    @Test
    fun `LEADING_ZERO is '0'`() {
        assertEquals("0", LEADING_ZERO)
    }
}
