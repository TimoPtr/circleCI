/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId.systemDefault
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class LastSyncDateFormatterImplTest : BaseUnitTest() {

    private val expectedPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val doubleCheckFormatter =
        DateTimeFormatter.ofPattern(expectedPattern).withZone(systemDefault())

    private val lastSyncDateFormatter = LastSyncDateFormatterImpl()

    @Test
    fun `formatter is symmetric`() {
        val now = ZonedDateTime.now()
        val dateTimeString = lastSyncDateFormatter.format(now)

        assertEquals(now, lastSyncDateFormatter.parse(dateTimeString))
    }

    @Test
    fun `format returns identical string to the one returned from string-pattern based formatter`() {
        val now = ZonedDateTime.now()

        assertEquals(doubleCheckFormatter.format(now), lastSyncDateFormatter.format(now))
    }

    @Test
    fun `parse is backward-compatible with old string-pattern based formatter`() {
        val now = ZonedDateTime.now()
        val dateString = doubleCheckFormatter.format(now)
        val parsedNow = lastSyncDateFormatter.parse(dateString)

        assertEquals(now, parsedNow)
    }

    @Test
    fun `parse handles case with 0 seconds the same way as old string-pattern based formatter`() {
        val now = ZonedDateTime.of(
            LocalDate.now(),
            LocalTime.of(12, 4, 0),
            systemDefault()
        )
        val dateString = doubleCheckFormatter.format(now)
        val parsedNow = lastSyncDateFormatter.parse(dateString)

        assertEquals(now, parsedNow)
        assertEquals(dateString, lastSyncDateFormatter.format(now))
    }

    @Test
    fun `parse handles case with 0 milliseconds the same way as old string-pattern based formatter`() {
        val now = ZonedDateTime.of(
            LocalDate.now(),
            LocalTime.of(12, 4, 1, 0),
            systemDefault()
        )
        val dateString = doubleCheckFormatter.format(now)
        val parsedNow = lastSyncDateFormatter.parse(dateString)

        assertEquals(now, parsedNow)
        assertEquals(dateString, lastSyncDateFormatter.format(now))
    }

    @Test
    fun `parse handles case with 100 milliseconds the same way as old string-pattern based formatter`() {
        val now = ZonedDateTime.of(
            LocalDate.now(),
            LocalTime.of(12, 4, 1, 100 * 1000 * 1000),
            systemDefault()
        )
        val dateString = doubleCheckFormatter.format(now)
        val parsedNow = lastSyncDateFormatter.parse(dateString)

        assertEquals(now, parsedNow)
        assertEquals(dateString, lastSyncDateFormatter.format(now))
    }
}
