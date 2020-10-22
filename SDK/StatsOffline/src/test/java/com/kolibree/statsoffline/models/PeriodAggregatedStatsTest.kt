package com.kolibree.statsoffline.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.calculateStatsAverage
import com.kolibree.statsoffline.roundOneDecimal
import com.kolibree.statsoffline.test.createAverageCheckup
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createDayWithSessions
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.randomListOfDouble
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class PeriodAggregatedStatsTest : BaseUnitTest() {
    /*
    constructor
     */

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if startDate and endDate are on the same day`() {
        val date = TrustedClock.getNowLocalDate()

        createPeriodAggregatedStats(startDate = date, endDate = date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if startDate is after endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.plusDays(1)

        createPeriodAggregatedStats(startDate = startDate, endDate = endDate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if daysWithSessions does not contain a value for every date between startDate and endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(2)

        createPeriodAggregatedStats(
            startDate = startDate,
            endDate = endDate,
            daysWithSessions = setOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate))
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if daysWithSessions contains more dates than the startdate and endDate range`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        createPeriodAggregatedStats(
            startDate = startDate,
            endDate = endDate,
            daysWithSessions = setOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(
                        day = startDate.minusDays(
                            1
                        )
                    )
                ),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate))
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if daysWithSessions contains dates before startdate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        createPeriodAggregatedStats(
            startDate = startDate,
            endDate = endDate,
            daysWithSessions = setOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(
                        day = startDate.minusDays(
                            1
                        )
                    )
                )
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if daysWithSessions contains dates after endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        createPeriodAggregatedStats(
            startDate = startDate,
            endDate = endDate,
            daysWithSessions = setOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate)),
                createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(
                        day = endDate.plusDays(
                            1
                        )
                    )
                )
            )
        )
    }

    /*
    fromDaysWithSessions
     */

    @Test(expected = IllegalArgumentException::class)
    fun `fromDaysWithSessions throws IllegalArgumentException if startDate and endDate are on the same day`() {
        val date = TrustedClock.getNowLocalDate()

        PeriodAggregatedStats.fromDaysWithSessions(1, date, date, listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromDaysWithSessions throws IllegalArgumentException if startDate is after endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.plusDays(1)

        PeriodAggregatedStats.fromDaysWithSessions(1, startDate, endDate, listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromDaysWithSessions throws IllegalArgumentException if daysWithSessions contains duplicated dates in range`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        PeriodAggregatedStats.fromDaysWithSessions(
            1,
            startDate,
            endDate,
            daysWithSessions = listOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate)),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate))
            )
        )
    }

    @Test
    fun `fromDaysWithSessions sanitizes parameters to avoid IllegalArgumentException if daysWithSessions does not contain a value for every date between startDate and endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(2)

        PeriodAggregatedStats.fromDaysWithSessions(
            1,
            startDate,
            endDate,
            daysWithSessions = listOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate))
            )
        )
    }

    @Test
    fun `fromDaysWithSessions sanitizes parameters to avoid IllegalArgumentException if daysWithSessions contains more dates than the startdate and endDate range`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        PeriodAggregatedStats.fromDaysWithSessions(
            1,
            startDate,
            endDate,
            daysWithSessions = listOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(
                        day = startDate.minusDays(
                            1
                        )
                    )
                ),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate))
            )
        )
    }

    @Test
    fun `fromDaysWithSessions sanitizes parameters to avoid IllegalArgumentException if daysWithSessions contains dates before startdate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        PeriodAggregatedStats.fromDaysWithSessions(
            1,
            startDate,
            endDate,
            daysWithSessions = listOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(
                        day = startDate.minusDays(
                            1
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `fromDaysWithSessions sanitizes parameters to avoid IllegalArgumentException if daysWithSessions contains dates after endDate`() {
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(1)

        PeriodAggregatedStats.fromDaysWithSessions(
            1,
            startDate,
            endDate,
            daysWithSessions = listOf(
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)),
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate)),
                createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(
                        day = endDate.plusDays(
                            1
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `fromDaysWithSessions returns PeriodAggregatedStats with calculated averages`() {
        val today = TrustedClock.getNowLocalDate()
        val twoDaysAgo = today.minusDays(2)

        val duration = arrayOf(50.0, 0.0, 70.0)
        val surface = arrayOf(30.0, 78.0, 12.0)

        val mouthZone1 = MouthZone16.UpMolRiOcc
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpIncExt
        val mouthZone4 = MouthZone16.UpIncInt
        val averageCheckupToday =
            createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))
        val averageCheckupYesterday = createAverageCheckup(mapOf(mouthZone1 to 12.4f))
        val averageCheckupTwoDaysAgo =
            createAverageCheckup(mapOf(mouthZone1 to 23f, mouthZone2 to 78f, mouthZone4 to 10f))

        val dayStatsToday = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = today,
                averageDuration = duration[0],
                averageSurface = surface[0],
                averageCheckup = averageCheckupToday
            ),
            sessions = listOf()
        )

        val dayStatsYesterday = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = today.minusDays(1),
                averageDuration = duration[1],
                averageSurface = surface[1],
                averageCheckup = averageCheckupYesterday
            ),
            sessions = listOf(
                createSessionStatsEntity(),
                createSessionStatsEntity()
            )
        )

        val dayStatsTwoDaysAgo = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = twoDaysAgo,
                averageDuration = duration[2],
                averageSurface = surface[2],
                averageCheckup = averageCheckupTwoDaysAgo
            ),
            sessions = listOf(
                createSessionStatsEntity(),
                createSessionStatsEntity(),
                createSessionStatsEntity()
            )
        )

        val profileId = 656L
        val periodAggregatedStats = PeriodAggregatedStats.fromDaysWithSessions(
            profileId,
            twoDaysAgo,
            today,
            daysWithSessions = listOf(dayStatsToday, dayStatsYesterday, dayStatsTwoDaysAgo)
        )

        val expectedAvgZone1 = 38.5f
        val expectedAvgZone2 = 27.7f
        val expectedAvgZone3 = 0.7f
        val expectedAvgZone4 = 3.3f

        val expectedAverageCheckup = createAverageCheckup(
            mapOf(
                mouthZone1 to expectedAvgZone1,
                mouthZone2 to expectedAvgZone2,
                mouthZone3 to expectedAvgZone3,
                mouthZone4 to expectedAvgZone4
            )
        )

        val expectedDuration = duration.average()
        val expectedSurface = surface.average()

        val expectedPeriodAggregatedStats = PeriodAggregatedStats(
            dateRange = SanitizedDateRange(twoDaysAgo, today),
            profileId = profileId,
            averageDuration = expectedDuration,
            averageSurface = expectedSurface,
            averageCheckup = expectedAverageCheckup,
            dayAggregatedStats = setOf(dayStatsToday, dayStatsYesterday, dayStatsTwoDaysAgo)
        )

        assertEquals(expectedDuration, periodAggregatedStats.averageDuration)
        assertEquals(expectedSurface, periodAggregatedStats.averageSurface)
        assertEquals(expectedAverageCheckup, periodAggregatedStats.averageCheckup)

        assertEquals(expectedPeriodAggregatedStats, periodAggregatedStats)

        assertEquals(5, periodAggregatedStats.totalSessions)
        val expectedBrushingsPerDay = (5.0 / 3).roundOneDecimal()
        assertEquals(expectedBrushingsPerDay, periodAggregatedStats.sessionsPerDay, 0.0)
    }

    @Test
    fun `fromDaysWithSessions returns new instance with kml kpis from DayWithSessions`() {
        val today = TrustedClock.getNowLocalDate()
        val twoDaysAgo = today.minusDays(2)

        val totalDays = 3
        val correctMovementAverages = randomListOfDouble(size = totalDays)
        val underSpeedAverages = randomListOfDouble(size = totalDays)
        val correctSpeedAverages = randomListOfDouble(size = totalDays)
        val overSpeedAverages = randomListOfDouble(size = totalDays)
        val correctOrientationAverages = randomListOfDouble(size = totalDays)
        val overPressureAverages = randomListOfDouble(size = totalDays)

        val dayStatsToday = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = today,
                averageDuration = 0.0,
                averageSurface = 0.0,
                averageCheckup = emptyAverageCheckup(),
                correctMovementAverage = correctMovementAverages[0],
                correctOrientationAverage = correctOrientationAverages[0],
                correctSpeedAverage = correctSpeedAverages[0],
                underSpeedAverage = underSpeedAverages[0],
                overSpeedAverage = overSpeedAverages[0],
                overPressureAverage = overPressureAverages[0]
            ),
            sessions = listOf()
        )

        val dayStatsYesterday = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = today.minusDays(1),
                averageDuration = 0.0,
                averageSurface = 0.0,
                averageCheckup = emptyAverageCheckup(),
                correctMovementAverage = correctMovementAverages[1],
                correctOrientationAverage = correctOrientationAverages[1],
                correctSpeedAverage = correctSpeedAverages[1],
                underSpeedAverage = underSpeedAverages[1],
                overSpeedAverage = overSpeedAverages[1],
                overPressureAverage = overPressureAverages[1]
            ),
            sessions = listOf(
                createSessionStatsEntity(),
                createSessionStatsEntity()
            )
        )

        val dayStatsTwoDaysAgo = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = twoDaysAgo,
                averageDuration = 0.0,
                averageSurface = 0.0,
                averageCheckup = emptyAverageCheckup(),
                correctMovementAverage = correctMovementAverages[2],
                correctOrientationAverage = correctOrientationAverages[2],
                correctSpeedAverage = correctSpeedAverages[2],
                underSpeedAverage = underSpeedAverages[2],
                overSpeedAverage = overSpeedAverages[2],
                overPressureAverage = overPressureAverages[2]
            ),
            sessions = listOf(
                createSessionStatsEntity(),
                createSessionStatsEntity(),
                createSessionStatsEntity()
            )
        )

        val profileId = 656L
        val periodAggregatedStats = PeriodAggregatedStats.fromDaysWithSessions(
            profileId,
            twoDaysAgo,
            today,
            daysWithSessions = listOf(dayStatsToday, dayStatsYesterday, dayStatsTwoDaysAgo)
        )

        val expectedPeriodAggregatedStats = PeriodAggregatedStats(
            dateRange = SanitizedDateRange(twoDaysAgo, today),
            profileId = profileId,
            averageDuration = 0.0,
            averageSurface = 0.0,
            averageCheckup = emptyAverageCheckup(),
            correctMovementAverage = correctMovementAverages.calculateStatsAverage(excludeZero = false),
            correctOrientationAverage = correctOrientationAverages.calculateStatsAverage(excludeZero = false),
            correctSpeedAverage = correctSpeedAverages.calculateStatsAverage(excludeZero = false),
            underSpeedAverage = underSpeedAverages.calculateStatsAverage(excludeZero = false),
            overSpeedAverage = overSpeedAverages.calculateStatsAverage(excludeZero = false),
            dayAggregatedStats = setOf(dayStatsTwoDaysAgo, dayStatsYesterday, dayStatsToday),
            overPressureAverage = overPressureAverages.calculateStatsAverage(excludeZero = false)
        )

        assertEquals(expectedPeriodAggregatedStats, periodAggregatedStats)
    }

    /*
    UTILS
     */

    /*
    Differs from StatsOfflineTestBuilder.createPeriodAggregatedStats in that default parameters aren't sanitized
     */
    private fun createPeriodAggregatedStats(
        profileId: Long = 1,
        startDate: LocalDate,
        endDate: LocalDate,
        daysWithSessions: Set<DayWithSessions> = setOf(),
        averageDuration: Double = 0.0,
        averageSurface: Double = 0.0,
        averageCheckup: AverageCheckup = emptyAverageCheckup()
    ): PeriodAggregatedStats {
        return PeriodAggregatedStats(
            profileId = profileId,
            dateRange = SanitizedDateRange(startDate, endDate),
            dayAggregatedStats = daysWithSessions,
            averageSurface = averageSurface,
            averageDuration = averageDuration,
            averageCheckup = averageCheckup
        )
    }
}
