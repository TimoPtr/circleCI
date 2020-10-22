/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.mapper

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

class PersonalChallengePeriodMapperTest : BaseUnitTest() {

    @Test
    fun `all values return toJsonDuration as number of days`() {
        PersonalChallengePeriod.values().forEach {
            assertEquals(it.duration.toDays(), it.stringifyDuration())
        }
    }

    @Test
    fun `all values return toJsonUnit as day`() {
        PersonalChallengePeriod.values().forEach {
            assertEquals("day", it.stringifyUnit())
        }
    }

    @Test
    fun `FIVE_DAYS holds 5 days duration`() {
        assertEquals(Duration.ofDays(5), PersonalChallengePeriod.FIVE_DAYS.duration)
        assertEquals(ChronoUnit.DAYS, PersonalChallengePeriod.FIVE_DAYS.unit)
        assertEquals(5, PersonalChallengePeriod.FIVE_DAYS.stringifyDuration())
        assertEquals("day", PersonalChallengePeriod.FIVE_DAYS.stringifyUnit())
    }

    @Test
    fun `SEVEN_DAYS holds 7 days duration`() {
        assertEquals(Duration.ofDays(7), PersonalChallengePeriod.SEVEN_DAYS.duration)
        assertEquals(ChronoUnit.DAYS, PersonalChallengePeriod.SEVEN_DAYS.unit)
        assertEquals(7, PersonalChallengePeriod.SEVEN_DAYS.stringifyDuration())
        assertEquals("day", PersonalChallengePeriod.SEVEN_DAYS.stringifyUnit())
    }

    @Test
    fun `FOURTEEN_DAYS holds 14 days duration`() {
        assertEquals(Duration.ofDays(14), PersonalChallengePeriod.FOURTEEN_DAYS.duration)
        assertEquals(ChronoUnit.DAYS, PersonalChallengePeriod.FOURTEEN_DAYS.unit)
        assertEquals(14, PersonalChallengePeriod.FOURTEEN_DAYS.stringifyDuration())
        assertEquals("day", PersonalChallengePeriod.FOURTEEN_DAYS.stringifyUnit())
    }

    @Test
    fun `THIRTY_DAYS holds 30 days duration`() {
        assertEquals(Duration.ofDays(30), PersonalChallengePeriod.THIRTY_DAYS.duration)
        assertEquals(ChronoUnit.DAYS, PersonalChallengePeriod.THIRTY_DAYS.unit)
        assertEquals(30, PersonalChallengePeriod.THIRTY_DAYS.stringifyDuration())
        assertEquals("day", PersonalChallengePeriod.THIRTY_DAYS.stringifyUnit())
    }

    @Test
    fun `periodFromJsonString(7, 'day') returns SEVEN_DAYS`() {
        assertEquals(PersonalChallengePeriod.SEVEN_DAYS, periodFromStringedData(7, "day"))
    }

    @Test
    fun `periodFromJsonString(14, 'day') returns FOURTEEN_DAYS`() {
        assertEquals(PersonalChallengePeriod.FOURTEEN_DAYS, periodFromStringedData(14, "day"))
    }

    @Test
    fun `periodFromJsonString(30, 'day') returns THIRTY_DAYS`() {
        assertEquals(PersonalChallengePeriod.THIRTY_DAYS, periodFromStringedData(30, "day"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `periodFromJsonString(31, 'day') throws exception`() {
        periodFromStringedData(31, "day")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `periodFromJsonString(30, 'month') throws exception`() {
        periodFromStringedData(30, "month")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `from(30, 'hour') throws exception`() {
        periodFromStringedData(30, "hour")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `from(30, 'minute') throws exception`() {
        periodFromStringedData(30, "minute")
    }
}
