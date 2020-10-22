/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.charts.StatBuilder
import java.util.Arrays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.OffsetDateTime

/**
 * Created by guillaumeagis on 22/05/18.
 */
class DayStatTest : BaseUnitTest() {

    @Test
    fun init_empty_setsNullValues() {
        val dayStat = DayStat(currentTime(), emptyList())

        assertEquals(0, dayStat.count().toLong())

        assertFalse(dayStat.isEvening())
        assertFalse(dayStat.isMorning())
        assertFalse(dayStat.isNoon())
    }

    @Test
    fun init_1StatAfter4PM_storesAsEvening() {
        val currentTime = currentTime()

        val date = currentTime.withHour(16)

        val statEvening = StatBuilder.create().withDateTime(date).build()

        val dayStat = DayStat(
            currentTime(),
            listOf(statEvening)
        )

        assertTrue(dayStat.isEvening())

        assertFalse(dayStat.isMorning())
        assertFalse(dayStat.isNoon())
    }

    @Test
    fun init_1StatBefore4PM_storesAsMorning() {
        val currentTime = currentTime()

        val date = currentTime.withHour(15).withMinute(59)

        val Stat = StatBuilder.create().withDateTime(date).build()

        val dayStat = DayStat(
            currentTime(),
            listOf(Stat)
        )

        assertTrue(dayStat.isMorning())

        assertFalse(dayStat.isEvening())
        assertFalse(dayStat.isNoon())
    }

    @Test
    fun init_2StatsAfter4PM_storesAsNoonAndEvening() {
        val currentTime = currentTime()

        val date1 = currentTime.withHour(20)
        val date2 = currentTime.withHour(21)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()
        val Stat2 = StatBuilder.create().withDateTime(date2).build()

        val dayStat = DayStat(
            currentTime(),
            Arrays.asList(Stat1, Stat2)
        )

        assertTrue(dayStat.isEvening())
        assertTrue(dayStat.isNoon())

        assertFalse(dayStat.isMorning())
    }

    @Test
    fun init_2StatsBefore4PM_storesAsNoonAndMorning() {
        val currentTime = currentTime()

        val date1 = currentTime.withHour(12)
        val date2 = currentTime.withHour(13)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()
        val Stat2 = StatBuilder.create().withDateTime(date2).build()

        val dayStat = DayStat(
            currentTime(),
            Arrays.asList(Stat1, Stat2)
        )

        assertTrue(dayStat.isMorning())
        assertTrue(dayStat.isNoon())

        assertFalse(dayStat.isEvening())
    }

    @Test
    fun init_2StatsFirstBefore4PM_storesAsEveningAndMorning() {
        val currentTime = currentTime()

        val date1 = currentTime.withHour(20)
        val date2 = currentTime.withHour(7)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()
        val StatMorning = StatBuilder.create().withDateTime(date2).build()

        val dayStat = DayStat(
            currentTime(),
            Arrays.asList(Stat1, StatMorning)
        )

        assertTrue(dayStat.isEvening())
        assertTrue(dayStat.isMorning())

        assertFalse(dayStat.isNoon())
    }

    @Test
    fun init_3StatsLastAfter4PM_setsValues() {
        val currentTime = currentTime()

        val date1 = currentTime.withHour(3)
        val date2 = currentTime.withHour(12)
        val date3 = currentTime.withHour(21)

        val StatMorning = StatBuilder.create().withDateTime(date1).build()
        val StatNoon = StatBuilder.create().withDateTime(date2).build()
        val StatEvening = StatBuilder.create().withDateTime(date3).build()

        val dayStat = DayStat(
            currentTime(),
            Arrays.asList(StatEvening, StatNoon, StatMorning)
        )

        assertTrue(dayStat.isMorning())
        assertTrue(dayStat.isNoon())
        assertTrue(dayStat.isEvening())
    }

    @Test
    fun init_3StatsLastBefore4PM_setsValues_noEveningStat() {
        val currentTime = currentTime()

        val date1 = currentTime.withHour(3)
        val date2 = currentTime.withHour(12)
        val date3 = currentTime.withHour(15)

        val statMorning = StatBuilder.create().withDateTime(date1).build()
        val statNoon = StatBuilder.create().withDateTime(date2).build()
        val statEvening = StatBuilder.create().withDateTime(date3).build()

        val dayStat = DayStat(
            currentTime(),
            Arrays.asList(statEvening, statNoon, statMorning)
        )

        assertTrue(dayStat.isMorning())
        assertTrue(dayStat.isNoon())
        assertFalse(dayStat.isEvening())
    }

    private fun currentTime(): OffsetDateTime {
        return TrustedClock.getNowOffsetDateTime()
    }
}
