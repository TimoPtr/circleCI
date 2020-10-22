package com.kolibree.charts.models

import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.charts.StatBuilder
import com.nhaarman.mockitokotlin2.whenever
import java.util.ArrayList
import java.util.Arrays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.spy
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by guillaumeagis on 22/05/2018.
 */
class WeeklyStatTest : BaseUnitTest() {

    @Test
    fun init_calculateAverageStattime_emptyData_setsZeroAverageStattime() {
        val weeklyStat = WeeklyStat(emptyList(), TrustedClock.systemClock())

        assertEquals(0, weeklyStat.averageBrushingTime)
    }

    @Test
    fun init_calculateDayStats_emptyData_createsFixedLengthDayStatArrayWithEmptyData() {
        val weeklyStat = WeeklyStat(emptyList(), TrustedClock.systemClock())

        val data = weeklyStat.data

        assertEquals(WeeklyStat.DAY_COUNT, data.size)

        for (dayStat in data) {
            assertEquals(0, dayStat.count())
        }
    }

    @Test
    fun init_calculateAverageStattime_withData_calculatesTotalDurationMean() {
        val duration1: Long = 60
        val duration2: Long = 30
        val duration3: Long = 120
        val stat1 = StatBuilder.create().withDuration(duration1).build()
        val stat2 = StatBuilder.create().withDuration(duration2).build()
        val stat3 = StatBuilder.create().withDuration(duration3).build()

        val expectedAverageStatTime = ((duration1 + duration2 + duration3) / 3).toInt()

        val weeklyStat = WeeklyStat(
            Arrays.asList<Stat>(stat1, stat2, stat3), TrustedClock.systemClock()
        )

        assertEquals(expectedAverageStatTime, weeklyStat.averageBrushingTime)
    }

    @Test
    fun init_calculateAverageStattime_withDataOnConsecutiveDays_assignsDays() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusHours(3)
        val date2 = currentTime.minusDays(1).minusHours(3)
        val date3 = currentTime.minusDays(2).minusHours(3)

        val stat1 = StatBuilder.create().withDateTime(date1).build()
        val stat2 = StatBuilder.create().withDateTime(date2).build()
        val stat3 = StatBuilder.create().withDateTime(date3).build()

        val stats = arrayOf<Stat>(stat3, stat2, stat1)
        val weeklyStat = WeeklyStat(Arrays.asList<Stat>(*stats), TrustedClock.systemClock())

        val data = weeklyStat.data

        var i = 0
        while (i < 4) {
            assertEquals(0, data[i].count())
            i++
        }

        for (stat in stats) {
            assertEquals(1, data[i].count())
            assertEquals(stat, data[i].data[0])

            i++
        }
    }

    @Test
    fun init_calculateAverageStattime_withData_ignoresDaysInTheFuture() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.plusDays(1).minusHours(3)
        val date2 = currentTime.plusDays(2).minusHours(3)
        val date3 = currentTime.plusDays(3).minusHours(3)

        val stat1 = StatBuilder.create().withDateTime(date1).build()
        val stat2 = StatBuilder.create().withDateTime(date2).build()
        val stat3 = StatBuilder.create().withDateTime(date3).build()

        val weeklyStat = WeeklyStat(
            Arrays.asList<Stat>(stat1, stat2, stat3), TrustedClock.systemClock()
        )

        val data = weeklyStat.data

        for (dayStat in data) {
            assertEquals(0, dayStat.count())
        }
    }

    @Test
    fun init_calculateAverageStattime_withData_ignoresDaysInDistantPast() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusDays(11)
        val date2 = currentTime.minusDays(12)
        val date3 = currentTime.minusDays(13)

        val stat1 = StatBuilder.create().withDateTime(date1).build()
        val stat2 = StatBuilder.create().withDateTime(date2).build()
        val stat3 = StatBuilder.create().withDateTime(date3).build()

        val weeklyStat = WeeklyStat(
            Arrays.asList<Stat>(stat1, stat2, stat3), TrustedClock.systemClock()
        )

        val data = weeklyStat.data

        for (dayStat in data) {
            assertEquals(0, dayStat.count())
        }
    }

    @Test
    fun init_calculateAverageStattime_withDataOnSameDay_assignsDays() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusHours(3)
        val date2 = currentTime.minusHours(1)
        val date3 = currentTime.minusDays(1).minusHours(3)

        val stat1 = StatBuilder.create().withDateTime(date1).build()
        val stat2 = StatBuilder.create().withDateTime(date2).build()
        val stat3 = StatBuilder.create().withDateTime(date3).build()

        val weeklyStat = WeeklyStat(
            Arrays.asList<Stat>(stat1, stat2, stat3), TrustedClock.systemClock()
        )

        val data = weeklyStat.data

        var i = 0
        while (i < 5) {
            assertEquals(0, data[i].count())
            i++
        }

        // index at yesterday

        assertEquals(1, data[i].count())
        assertEquals(stat3, data[i].data[0])

        i++ // index at today

        assertEquals(2, data[i].count())

        val dayStats = arrayListOf<Stat>()
        dayStats.addAll(data[i].data)
        assertTrue(dayStats.contains(stat1))
        assertTrue(dayStats.contains(stat2))
    }

    @Test
    fun init_calculateAverageStattime_withData6DaysAgoAt8_acceptsStat() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusDays(6).withHour(8)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()

        val weeklyStat = WeeklyStat(
            listOf<Stat>(Stat1),
            TrustedClock.systemClock()
        )

        val data = weeklyStat.data

        var i = 1
        while (i < 7) {
            assertEquals(0, data[i].count())
            i++
        }

        assertEquals(1, data[0].count())
        assertEquals(Stat1, data[0].data[0])
    }

    /*
  This is due to the weird concept of "Kolibree Day", which starts at 4AM
   */
    @Test
    fun init_calculateAverageStattime_withData6DaysAgoAt3AM_ignoresStat() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusDays(6).withHour(3)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()

        val weeklyStat = WeeklyStat(
            listOf<Stat>(Stat1),
            TrustedClock.systemClock()
        )

        val data = weeklyStat.data

        for (i in 0..6) {
            assertEquals(0, data[i].count())
        }
    }

    /*
  GET AVERAGE Stat COUNT
   */
    @Test
    fun getAverageStatCount_emptyDayStats_returnsZero() {
        val weeklyStat = WeeklyStat(emptyList(), TrustedClock.systemClock())

        assertEquals(0f, weeklyStat.getAverageBrushingCount(TrustedClock.getNowOffsetDateTime()), 0.001f)
    }

    @Test
    fun getAverageStatCount_withDayStatsAfterCreationDate_returnsStatsPerDay() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusDays(1).minusHours(3)
        val date2 = currentTime.minusDays(2).minusHours(3)
        val date3 = currentTime.minusDays(3).minusHours(3)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()
        val Stat2 = StatBuilder.create().withDateTime(date2).build()
        val Stat3 = StatBuilder.create().withDateTime(date3).build()

        val Stats = Arrays.asList<Stat>(Stat1, Stat2, Stat3)
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(Stats, TrustedClock.systemClock()))

        val expectedAverageStatCount = Math.round(10f * Stats.size / WeeklyStat.DAY_COUNT) / 10f

        assertEquals(
            expectedAverageStatCount,
            weeklyStat.getAverageBrushingCount(currentTime.minusMonths(1)), 0.01f
        )
    }

    @Test
    fun getAverageStatCount_withRecentProfile_withOlderStatsAssigned_takesIntoAccountStatsBeforeProfileCreation() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        TrustedClock.setFixedDate(currentTime)

        val profileCreatedDaysAgo = 4
        val oldestStatDaysAgo = profileCreatedDaysAgo + 1

        val profileCreationDate = currentTime.minusDays(profileCreatedDaysAgo.toLong())

        val date1 = currentTime.minusDays(1).minusHours(3)
        val date2 = currentTime.minusDays(2).minusHours(3)
        val date5 = profileCreationDate.minusHours(1)
        val date6 = currentTime
            .minusDays(oldestStatDaysAgo.toLong()) // should be taken into account

        val Stat1 = StatBuilder.create().withDateTime(date1).build()
        val Stat2 = StatBuilder.create().withDateTime(date2).build()
        val Stat5 = StatBuilder.create().withDateTime(date5).build()
        val Stat6 = StatBuilder.create().withDateTime(date6).build()

        val Stats = Arrays.asList<Stat>(Stat1, Stat2, Stat5, Stat6)
        val weeklyStat = WeeklyStat(Stats, TrustedClock.systemClock())

        /*
    if oldest Stat was 4 days ago, we need to take today into account too
     */
        val expectedDividend = oldestStatDaysAgo + 1
        val expectedAverageStatCount = (Math.round(10f * Stats.size / expectedDividend) / 10f)

        assertEquals(
            expectedAverageStatCount, weeklyStat.getAverageBrushingCount(profileCreationDate),
            0.01f
        )
    }

    @Test
    fun getAverageStatCount_withRecentProfile_withoutOlderStatsAssigned_takesIntoAccountProfileCreation() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        TrustedClock.setFixedDate(currentTime)

        val profileCreatedDaysAgo = 4

        val profileCreationDate = currentTime.minusDays(profileCreatedDaysAgo.toLong())

        val date1 = currentTime.minusDays(1).minusHours(3)
        val date2 = currentTime.minusDays(2).minusHours(3)
        val date5 = profileCreationDate.minusHours(1)

        val stat1 = StatBuilder.create().withDateTime(date1).build()
        val stat2 = StatBuilder.create().withDateTime(date2).build()
        val stat5 = StatBuilder.create().withDateTime(date5).build()

        val stats = Arrays.asList<Stat>(stat1, stat2, stat5)
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(stats, TrustedClock.systemClock()))

        /*
    if profile was created 4 days ago, we need to take today into account too
     */
        val expectedDividend = profileCreatedDaysAgo + 1
        val expectedAverageStatCount = (Math.round(10f * stats.size / expectedDividend) / 10f)

        assertEquals(
            expectedAverageStatCount, weeklyStat.getAverageBrushingCount(profileCreationDate),
            0.01f
        )
    }

    /*
  GET AVERAGE SURFACE
   */
    @Test
    fun getAverageSurface_emptyData_returnsZero() {
        val weeklyStat = WeeklyStat(emptyList(), TrustedClock.systemClock())

        assertEquals(0, weeklyStat.getAverageSurface())
    }

    @Test
    fun getAverageSurface_withDayStats_emptyProcessedData_returnsZero() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val date1 = currentTime.minusDays(1).minusHours(3)
        val date2 = currentTime.minusDays(2).minusHours(3)
        val date3 = currentTime.minusDays(3).minusHours(3)

        val Stat1 = StatBuilder.create().withDateTime(date1).build()
        val Stat2 = StatBuilder.create().withDateTime(date2).build()
        val Stat3 = StatBuilder.create().withDateTime(date3).build()

        val weeklyStat = spy<WeeklyStat>(
            WeeklyStat(Arrays.asList<Stat>(Stat1, Stat2, Stat3), TrustedClock.systemClock())
        )

        assertEquals(0, weeklyStat.getAverageSurface())
    }

    @Test
    fun getAverageSurface_withDayStats_returnsAverageSurface() {
        val currentTime = TrustedClock.getNowOffsetDateTime().withHour(12)

        val processedData = "processed_data"

        val date1 = currentTime.minusDays(1).minusHours(3)
        val date2 = currentTime.minusDays(2).minusHours(3)
        val date3 = currentTime.minusDays(3).minusHours(3)

        val stat1 = StatBuilder.create()
            .withProcessedData(processedData)
            .withAverageBrushedSurface(1)
            .withDateTime(date1).build()
        val stat2 = StatBuilder.create()
            .withProcessedData(processedData)
            .withAverageBrushedSurface(2)
            .withDateTime(date2).build()
        val stat3 = StatBuilder.create()
            .withProcessedData(processedData)
            .withAverageBrushedSurface(3)
            .withDateTime(date3).build()

        val weeklyStat = spy<WeeklyStat>(
            WeeklyStat(Arrays.asList<Stat>(stat1, stat2, stat3), TrustedClock.systemClock())
        )

        assertEquals(2, weeklyStat.getAverageSurface())
    }

    /*
  DAYS MATH DIVIDEND
   */
    @Test
    fun nbOfDaysDividendToCalculateAverageStats_accountReallyOld_returnsDAYS_COUNT() {
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList(), TrustedClock.systemClock()))

        val oldestStatDateTime = TrustedClock.getNowOffsetDateTime()
        val profileCreateDatetime = TrustedClock.getNowOffsetDateTime().minusYears(1)

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        val expectedDividend = WeeklyStat.DAY_COUNT.toLong()
        assertEquals(
            expectedDividend,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_acountCreatedBeforeOldestStat_returnsDaysToAccountCreatedDate() {
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList(), TrustedClock.systemClock()))

        val accountDaysAgo = 5
        val oldestStatDateTime = TrustedClock.getNowOffsetDateTime()
        val profileCreateDatetime = oldestStatDateTime.minusDays(accountDaysAgo.toLong())

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        val expectedDividend = accountDaysAgo + 1L
        assertEquals(
            expectedDividend,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_accountCreatedAfterOldestStat_returnsDaysToOldestStatDate() {
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList(), TrustedClock.systemClock()))

        val oldestStatDaysAgo = 3
        val profileCreateDatetime = TrustedClock.getNowOffsetDateTime()
        val oldestStatDateTime = profileCreateDatetime.minusDays(oldestStatDaysAgo.toLong())

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        val expectedDividend = oldestStatDaysAgo + 1L
        assertEquals(
            expectedDividend,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_accountCreatedToday_noStats_returns1() {
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList(), TrustedClock.systemClock()))

        val profileCreateDatetime = TrustedClock.getNowOffsetDateTime()
        val oldestStatDateTime = maxZonedDateTime()

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        assertEquals(
            1,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_accountCreatedToday_withStats_returns1() {
        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList(), TrustedClock.systemClock()))

        val profileCreateDatetime = TrustedClock.getNowOffsetDateTime()
        val oldestStatDateTime = profileCreateDatetime.plusMinutes(1)

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        assertEquals(
            1,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_nowIs12_accountCreatedYesterdayAt13_withStatsYesterday_returns2() {
        val now = TrustedClock.getNowOffsetDateTime().withHour(12) // yesterday in kolibreeDay terms
        TrustedClock.setFixedDate(now)

        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList<Stat>(), TrustedClock.systemClock()))

        val profileCreateDatetime = now.minusDays(1).plusHours(1)
        val oldestStatDateTime = profileCreateDatetime.plusMinutes(1)

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        assertEquals(
            2,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_nowIs12_accountCreatedYesterdayAt11_withStatsYesterday_returns2() {
        val now = TrustedClock.getNowOffsetDateTime().withHour(12) // yesterday in kolibreeDay terms
        TrustedClock.setFixedDate(now)

        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList<Stat>(), TrustedClock.systemClock()))

        val profileCreateDatetime = now.minusDays(1).minusHours(1)
        val oldestStatDateTime = profileCreateDatetime.plusMinutes(1)

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        assertEquals(
            2,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    @Test
    fun nbOfDaysDividendToCalculateAverageStats_nowIs3_accountCreatedYesterdayAt9_withStatsYesterday_returns1() {
        val now = TrustedClock.getNowOffsetDateTime().withHour(3) // yesterday in kolibreeDay terms
        TrustedClock.setFixedDate(now)

        val weeklyStat = spy<WeeklyStat>(WeeklyStat(emptyList(), TrustedClock.systemClock()))

        val profileCreateDatetime = now.withHour(9) // same kolibreeDay
        val oldestStatDateTime = profileCreateDatetime.plusMinutes(1)

        whenever(weeklyStat.oldestDayWithStats()).thenReturn(oldestStatDateTime)

        // returns 1 because from kolibree point of view, it's the same day
        assertEquals(
            1,
            weeklyStat.nbOfDaysDividendToCalculateAverageBrushings(profileCreateDatetime)
        )
    }

    /*
  OLDEST DAY WITH StatS
   */
    @Test
    fun oldestDayWithStats_noStats_returnsLocalDateTimeMax() {
        val weeklyStat = WeeklyStat(emptyList(), TrustedClock.systemClock())

        val expectedDate = maxZonedDateTime()

        assertEquals(expectedDate, weeklyStat.oldestDayWithStats())
    }

    @Test
    fun oldestDayWithStats_with1Stat_returnsStatDateTimeAdjustedToKolibreeDay() {
        val StatDate = TrustedClock.getNowOffsetDateTime().minusDays(6).withHour(8)

        val Stat1 = StatBuilder.create().withDateTime(StatDate).build()

        val weeklyStat = WeeklyStat(
            listOf<Stat>(Stat1),
            TrustedClock.systemClock()
        )

        val expectedDateTime = StatDate
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(4)

        assertEquals(expectedDateTime, weeklyStat.oldestDayWithStats())
    }

    @Test
    fun oldestDayWithStats_withMultipleStats_returnsOldestStatDateTimeAdjustedToKolibreeDay() {
        val oldestStatDateTime = TrustedClock.getNowOffsetDateTime().minusDays(6).withHour(8)
        val otherStatDateTime1 = oldestStatDateTime.plusHours(1)
        val otherStatDateTime2 = oldestStatDateTime.plusDays(3)

        val Stat1 = StatBuilder.create().withDateTime(oldestStatDateTime).build()
        val Stat2 = StatBuilder.create().withDateTime(otherStatDateTime1).build()
        val Stat3 = StatBuilder.create().withDateTime(otherStatDateTime2).build()

        val weeklyStat = WeeklyStat(
            Arrays.asList<Stat>(Stat1, Stat2, Stat3),
            TrustedClock.systemClock()
        )

        val expectedDateTime = oldestStatDateTime
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(4)

        assertEquals(expectedDateTime, weeklyStat.oldestDayWithStats())
    }

    /*
  INTEGRATION TEST
   */
    @Test
    fun accountCreatedYesterday9AmWithTwoBrushes_today9AmShouldReturnFrequency1() {
        val now = TrustedClock.getNowOffsetDateTime().withDayOfMonth(10).withMonth(4).withHour(9)
        TrustedClock.setFixedDate(now)

        val accountTimezone = ZoneId.of("America/Mexico_City")
        val accountCreationDate = TrustedClock.getNowZonedDateTimeUTC().withDayOfMonth(9).withMonth(4)
            .withHour(8).withZoneSameInstant(accountTimezone).toOffsetDateTime()

        val stat1Date = TrustedClock.getNowOffsetDateTime().withDayOfMonth(9).withMonth(4)
            .withHour(11)
            .withMinute(15)
            .withOffsetSameInstant(ZoneOffset.UTC)

        val stats = ArrayList<Stat>()
        stats.add(
            StatBuilder.create()
                .withDateTime(stat1Date)
                .build()
        )

        stats.add(
            StatBuilder.create()
                .withDateTime(stat1Date.plusMinutes(1))
                .build()
        )

        val weeklyStat = WeeklyStat(stats, TrustedClock.systemClock())

        // we brushed yesterday twice, we expect frequency to be 1
        val expectedFrequency = 1f
        assertEquals(
            expectedFrequency,
            weeklyStat
                .getAverageBrushingCount(accountCreationDate), 0.001f
        )
    }

    @Test
    fun testConstantValues() {
        assertTrue(WeeklyStat.DAY_COUNT > 0)
        assertTrue(KOLIBREE_DAY_START_HOUR > 0)
    }

    private fun maxZonedDateTime(): OffsetDateTime {
        return OffsetDateTime.of(LocalDateTime.MAX, ZoneOffset.MAX)
    }
}
