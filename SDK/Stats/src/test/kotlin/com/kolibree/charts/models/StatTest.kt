package com.kolibree.charts.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.charts.getTimestamp
import com.kolibree.charts.persistence.models.StatInternal
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by guillaumeagis on 22/05/2018.
 */
class StatTest : BaseUnitTest() {

    private val clock = TrustedClock.systemClock()

    @Test
    fun init_empty_setsNullValues() {
        val stat = Stat(1, 0, 0, clock)

        assertEquals(0, stat.duration)
        assertEquals(0, stat.timestamp)
        assertEquals(0, stat.surface())
        assertEquals("", stat.processedData)
        assertFalse(stat.hasProcessedData())
    }

    @Test
    fun init_empty_setsNullValues2() {
        val ignoredSurface = 34
        val stat = Stat(2, 0, 0, clock, ignoredSurface, "")

        assertEquals(0, stat.duration)
        assertEquals(0, stat.timestamp)
        assertEquals(0, stat.surface())
        assertEquals("", stat.processedData)
        assertFalse(stat.hasProcessedData())
    }

    @Test
    fun init_WithValues() {
        val expectedSurface = 34
        val date = TrustedClock.getNowOffsetDateTime()
        val currentDateMilli = date.toInstant().toEpochMilli()
        val stat = Stat(3, DURATION, currentDateMilli, clock, expectedSurface, PROCESSED_DATA)

        assertEquals(DURATION, stat.duration)
        assertEquals(currentDateMilli, stat.timestamp)
        assertEquals(date, stat.date)
        assertEquals(PROCESSED_DATA, stat.processedData)
        assertEquals(expectedSurface, stat.surface())
        assertTrue(stat.hasProcessedData())
    }

    @Test
    fun surface_noProcessedData_returns0() {
        val stat = Stat(2, 0, 0, clock, 34, "")

        assertFalse(stat.hasProcessedData())
        assertEquals(0, stat.surface())
    }

    @Test
    fun surface_withProcessedData_returns0() {
        val expectedSurface = 34
        val stat = Stat(2, 0, 0, clock, expectedSurface, "{}")

        assertTrue(stat.hasProcessedData())
        assertEquals(expectedSurface, stat.surface())
    }

    @Test
    fun compareTwoStatObjects() {

        val morningDate = TrustedClock.getNowZonedDateTime()
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(1)

        val afternoonDate = TrustedClock.getNowZonedDateTime()
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(13)

        val morningStat = Stat(4, DURATION, morningDate.getTimestamp(), clock, 7, PROCESSED_DATA)
        val afternoonStat =
            Stat(5, DURATION, afternoonDate.getTimestamp(), clock, 8, PROCESSED_DATA)
        val afternoonAgainStat =
            Stat(6, DURATION, afternoonDate.getTimestamp(), clock, 9, PROCESSED_DATA)

        assertEquals(-1, morningStat.compareTo(afternoonStat))
        assertEquals(0, afternoonAgainStat.compareTo(afternoonStat))
        assertEquals(1, afternoonStat.compareTo(morningStat))
    }

    @Test
    fun creationStatObjectFromStatInteral() {

        val morningDate = TrustedClock.getNowZonedDateTime()
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(1)

        val statInternal =
            StatInternal(PROFILE_ID, DURATION, morningDate.getTimestamp(), clock, PROCESSED_DATA)

        val averageSurface = 87

        val stat =
            Stat(7, DURATION, morningDate.getTimestamp(), clock, averageSurface, PROCESSED_DATA)

        val checkupData: CheckupData = mock()
        whenever(checkupData.surfacePercentage).thenReturn(averageSurface)

        assertEquals(stat, Stat.fromStatInternal(statInternal, checkupData))
    }

    companion object {
        private const val DURATION = 12L
        private const val PROCESSED_DATA = "test"
        private const val PROFILE_ID = 54L
    }
}
