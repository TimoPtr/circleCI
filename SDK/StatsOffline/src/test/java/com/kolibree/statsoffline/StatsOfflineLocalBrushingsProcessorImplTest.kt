/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.PlaqlessCheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.Percentage
import com.kolibree.kml.PlaqueAggregate
import com.kolibree.kml.PlaqueStatus
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.integrityseal.IntegritySeal
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.StatsPlaqueAggregate
import com.kolibree.statsoffline.test.DEFAULT_DURATION
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.createAverageCheckup
import com.kolibree.statsoffline.test.createMonthAggregatedStatEntity
import com.kolibree.statsoffline.test.createMonthWithDayStats
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.mockCheckupData
import com.kolibree.statsoffline.test.mockIBrushing
import com.kolibree.statsoffline.test.toYearMonth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.threeten.bp.LocalDate

@ExperimentalCoroutinesApi
class StatsOfflineLocalBrushingsProcessorImplTest : BaseUnitTest() {

    private val checkupCalculator: CheckupCalculator = mock()
    private val featureToggle: StatsOfflineFeatureToggle = mock()
    private val statsOfflineDao: StatsOfflineDao = mock()
    private val integritySeal: IntegritySeal = mock()

    private val statsOfflineProcessor =
        spy(
            StatsOfflineLocalBrushingsProcessorImpl(
                statsOfflineDao,
                checkupCalculator,
                featureToggle,
                integritySeal
            )
        )

    /*
    ON SINGLE BRUSHING CREATED
     */

    @Test
    fun `onBrushingCreated does nothing if statsOffline feature is disabled`() = runBlockingTest {
        setFeatureToggleValue(false)

        statsOfflineProcessor.onBrushingCreated(mock())

        verify(statsOfflineDao, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `onBrushingCreated creates a list and invokes onBrushingsCreated`() = runBlockingTest {
        setFeatureToggleValue(true)

        doReturn(Unit).whenever(statsOfflineProcessor).onBrushingsCreated(any())

        val expectedBrushing: IBrushing = mock()
        statsOfflineProcessor.onBrushingCreated(expectedBrushing)

        argumentCaptor<List<IBrushing>> {
            verify(statsOfflineProcessor).onBrushingsCreated(capture())

            assertEquals(expectedBrushing, firstValue.single())
        }
    }

    private fun setFeatureToggleValue(value: Boolean) {
        whenever(featureToggle.value).thenReturn(value)
    }

    /*
    ON BRUSHINGS CREATED
     */

    @Test
    fun `onBrushingsCreated does nothing if statsOffline feature is disabled`() = runBlockingTest {
        setFeatureToggleValue(false)

        statsOfflineProcessor.onBrushingsCreated(listOf(mock()))

        verify(statsOfflineDao, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `onBrushingsCreated does nothing if list is empty`() = runBlockingTest {
        setFeatureToggleValue(true)

        statsOfflineProcessor.onBrushingsCreated(listOf())

        verify(statsOfflineDao, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `onBrushingsCreated invokes integritySeal before any other method`() =
        runBlockingTest {
            setFeatureToggleValue(true)

            val expectedMap: Map<LocalDate, List<BrushingSessionStatsEntity>> = mapOf()
            val initialList: List<IBrushing> = listOf(mock())
            doReturn(expectedMap).whenever(statsOfflineProcessor)
                .mapAndGroupSessionsPerDay(initialList)

            val expectedMonthList: List<MonthWithDayStats> = listOf()
            doReturn(expectedMonthList).whenever(statsOfflineProcessor)
                .updatedMonthsFromNewSessions(initialList, expectedMap)

            val expectedWeekList: List<WeekWithDayStats> = listOf()
            doReturn(expectedWeekList).whenever(statsOfflineProcessor)
                .updatedWeeksFromNewSessions(initialList, expectedMap)

            doNothing().whenever(statsOfflineProcessor).updateDatabase(any(), any())

            statsOfflineProcessor.onBrushingsCreated(initialList)

            inOrder(integritySeal, statsOfflineProcessor) {
                verify(integritySeal).validateIntegrity()

                verify(statsOfflineProcessor).mapAndGroupSessionsPerDay(initialList)
                verify(statsOfflineProcessor).updatedMonthsFromNewSessions(initialList, expectedMap)
                verify(statsOfflineProcessor).updatedWeeksFromNewSessions(initialList, expectedMap)

                verify(statsOfflineProcessor).updateDatabase(expectedMonthList, expectedWeekList)
            }
        }

    @Test
    fun `onBrushingsCreated invokes update on statsOfflineDao with updated stats`() =
        runBlockingTest {
            setFeatureToggleValue(true)

            val expectedMap: Map<LocalDate, List<BrushingSessionStatsEntity>> = mapOf()
            val initialList: List<IBrushing> = listOf(mock())
            doReturn(expectedMap).whenever(statsOfflineProcessor)
                .mapAndGroupSessionsPerDay(initialList)

            val expectedMonthList: List<MonthWithDayStats> = listOf()
            doReturn(expectedMonthList).whenever(statsOfflineProcessor)
                .updatedMonthsFromNewSessions(initialList, expectedMap)

            val expectedWeekList: List<WeekWithDayStats> = listOf()
            doReturn(expectedWeekList).whenever(statsOfflineProcessor)
                .updatedWeeksFromNewSessions(initialList, expectedMap)

            doNothing().whenever(statsOfflineProcessor).updateDatabase(any(), any())

            statsOfflineProcessor.onBrushingsCreated(initialList)

            verify(statsOfflineProcessor).updateDatabase(expectedMonthList, expectedWeekList)
        }

    /*
    mapAndGroupSessionsPerDay
     */
    @Test
    fun `mapAndGroupSessionsPerDay returns empty list for empty input`() {
        assertTrue(statsOfflineProcessor.mapAndGroupSessionsPerDay(listOf()).isEmpty())
    }

    @Test
    fun `mapAndGroupSessionsPerDay returns list with BrushingSessionStats grouped by Kolibree Day`() {
        val today = TrustedClock.getNowOffsetDateTime().withHour(23)
        val yesterday = today.minusDays(1)
        val twoDaysAgoKolibreeDay = yesterday.withHour(1)

        val brushingToday1 = mockIBrushing(date = today)
        val durationToday2 = 657L
        val processedDataTodayBrushing2 = "threeeeee"
        val brushingToday2 = mockIBrushing(
            date = today,
            duration = durationToday2,
            processedData = processedDataTodayBrushing2
        )

        val duration2 = 654L
        val profileId2 = 343L
        val processedData2 = "hola"
        val brushingYesterday = mockIBrushing(
            date = yesterday,
            duration = duration2,
            profileId = profileId2,
            processedData = processedData2
        )

        val duration3 = 14L
        val profileId3 = 53L
        val processedData3 = "adios"
        val brushingTwoDaysAgoKolibreeDay = mockIBrushing(
            date = twoDaysAgoKolibreeDay,
            duration = duration3,
            profileId = profileId3,
            processedData = processedData3
        )

        val surfacePercentageToday1 = 42
        val zoneSurfaceMapToday1 = createAverageCheckup(mapOf(MouthZone16.LoIncExt to 57f))
        val checkupDataToday1 = mockCheckupData(
            surface = surfacePercentageToday1,
            zoneCheckupMap = zoneSurfaceMapToday1
        )

        val surfacePercentageToday2 = 12
        val zoneSurfaceMapToday2 = createAverageCheckup(mapOf(MouthZone16.LoIncExt to 13f))
        val checkupDataToday2 = mockCheckupData(
            surface = surfacePercentageToday2,
            zoneCheckupMap = zoneSurfaceMapToday2
        )

        val surfacePercentageYesterday = 2788
        val zoneSurfaceMapYesterday = createAverageCheckup(mapOf(MouthZone16.UpMolLeInt to 9999f))
        val checkupDataYesterday = mockCheckupData(
            surface = surfacePercentageYesterday,
            zoneCheckupMap = zoneSurfaceMapYesterday
        )

        val surfacePercentageTwoDaysAgo = 567
        val zoneSurfaceMapTwoDaysAgo = createAverageCheckup(mapOf(MouthZone16.LoMolLeExt to 654f))
        val checkupDataTwoDaysAgo = mockCheckupData(
            surface = surfacePercentageTwoDaysAgo,
            zoneCheckupMap = zoneSurfaceMapTwoDaysAgo
        )

        whenever(checkupCalculator.calculateCheckup(brushingToday1)).thenReturn(checkupDataToday1)
        whenever(checkupCalculator.calculateCheckup(brushingToday2)).thenReturn(checkupDataToday2)
        whenever(checkupCalculator.calculateCheckup(brushingYesterday)).thenReturn(
            checkupDataYesterday
        )
        whenever(checkupCalculator.calculateCheckup(brushingTwoDaysAgoKolibreeDay)).thenReturn(
            checkupDataTwoDaysAgo
        )

        val brushingSessionsMap =
            statsOfflineProcessor.mapAndGroupSessionsPerDay(
                listOf(
                    brushingToday1,
                    brushingYesterday,
                    brushingToday2,
                    brushingTwoDaysAgoKolibreeDay
                )
            )

        assertEquals(3, brushingSessionsMap.size)

        val expectedSessionStatToday1 = createSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            duration = DEFAULT_DURATION,
            creationTime = today.toLocalDateTime(),
            checkup = zoneSurfaceMapToday1,
            averageSurface = surfacePercentageToday1,
            assignedDate = today.toLocalDate()
        )

        val expectedSessionStatToday2 = createSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            duration = durationToday2,
            creationTime = today.toLocalDateTime(),
            checkup = zoneSurfaceMapToday2,
            averageSurface = surfacePercentageToday2,
            assignedDate = today.toLocalDate()
        )

        val expectedBrushingSessionStatYesterday =
            createSessionStatsEntity(
                profileId = profileId2,
                duration = duration2,
                creationTime = yesterday.toLocalDateTime(),
                checkup = zoneSurfaceMapYesterday,
                averageSurface = surfacePercentageYesterday,
                assignedDate = yesterday.toLocalDate()
            )

        val expectedBrushingSessionStatTwoDaysAgo =
            createSessionStatsEntity(
                profileId = profileId3,
                duration = duration3,
                creationTime = twoDaysAgoKolibreeDay.toLocalDateTime(),
                checkup = zoneSurfaceMapTwoDaysAgo,
                averageSurface = surfacePercentageTwoDaysAgo,
                assignedDate = today.minusDays(2).toLocalDate()
            )

        val todaySessions = brushingSessionsMap.getValue(today.toLocalDate())
        assertEquals(2, todaySessions.size)
        assertTrue(
            "Expected ${listOf(
                expectedSessionStatToday1,
                expectedSessionStatToday2
            )}, was $todaySessions",
            todaySessions.containsAll(listOf(expectedSessionStatToday1, expectedSessionStatToday2))
        )

        assertEquals(
            expectedBrushingSessionStatYesterday,
            brushingSessionsMap.getValue(yesterday.toLocalDate()).single()
        )

        assertEquals(
            expectedBrushingSessionStatTwoDaysAgo,
            brushingSessionsMap.getValue(today.minusDays(2).toLocalDate()).single()
        )
    }

    /*
    UPDATED STATS FOR NEW SESSIONS
     */
    @Test
    fun `updatedStatsFromNewSessions returns empty list for empty brushings`() {
        assertTrue(statsOfflineProcessor.updatedMonthsFromNewSessions(listOf(), mapOf()).isEmpty())
    }

    @Test
    fun `updatedStatsFromNewSessions returns empty list for empty sessions`() {
        assertTrue(
            statsOfflineProcessor.updatedMonthsFromNewSessions(
                listOf(mock()),
                mapOf()
            ).isEmpty()
        )
    }

    @Test
    fun `updatedStatsFromNewSessions invokes getOrCreateMonth for every different month in sessions`() {
        val currentMonthDate = TrustedClock.getNowZonedDateTime().withHour(5).toLocalDateTime()
        val twoMonthsAgoDate = currentMonthDate.minusMonths(2)

        val currentMonthStat = createSessionStatsEntity(creationTime = currentMonthDate)
        val twoMonthsAgoStat = createSessionStatsEntity(creationTime = twoMonthsAgoDate)

        val brushings: List<IBrushing> = listOf(mock())

        doReturn(DEFAULT_PROFILE_ID).whenever(statsOfflineProcessor).extractProfileId(brushings)

        mockGetOrCreateMonthStats()

        val sessionStatsGroupedPerDay =
            mutableMapOf<LocalDate, List<BrushingSessionStatsEntity>>().apply {
                put(currentMonthDate.toLocalDate(), listOf(currentMonthStat))
                put(twoMonthsAgoDate.toLocalDate(), listOf(twoMonthsAgoStat))
            }

        statsOfflineProcessor.updatedMonthsFromNewSessions(brushings, sessionStatsGroupedPerDay)

        verify(statsOfflineDao).getOrCreateMonthStats(
            DEFAULT_PROFILE_ID,
            currentMonthDate.toYearMonth()
        )
        verify(statsOfflineDao).getOrCreateMonthStats(
            DEFAULT_PROFILE_ID,
            twoMonthsAgoDate.toYearMonth()
        )
    }

    @Test
    fun `updatedStatsFromNewSessions invokes withNewSessions on returned MonthWithDaySessions`() {
        val currentMonthDate = TrustedClock.getNowZonedDateTime().toLocalDateTime()

        val currentMonthStat1 =
            createSessionStatsEntity(creationTime = currentMonthDate.withHour(6))
        val currentMonthStat2 =
            createSessionStatsEntity(creationTime = currentMonthDate.withHour(7))

        val brushings: List<IBrushing> = listOf(mock())

        doReturn(DEFAULT_PROFILE_ID).whenever(statsOfflineProcessor).extractProfileId(brushings)

        val mockWithDayStats =
            spy(
                createMonthWithDayStats(
                    monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                        yearMonth = currentMonthDate.toYearMonth()
                    )
                )
            )
        whenever(statsOfflineDao.getOrCreateMonthStats(eq(DEFAULT_PROFILE_ID), any())).thenReturn(
            mockWithDayStats
        )

        val monthSessions = listOf(currentMonthStat1, currentMonthStat2)
        val sessionStatsGroupedPerDay =
            mutableMapOf<LocalDate, List<BrushingSessionStatsEntity>>().apply {
                put(currentMonthDate.toLocalDate(), monthSessions)
            }

        statsOfflineProcessor.updatedMonthsFromNewSessions(brushings, sessionStatsGroupedPerDay)

        verify(mockWithDayStats).withNewSessions(currentMonthDate.toLocalDate(), monthSessions)
    }

    @Test
    fun `updatedStatsFromNewSessions contains all expected sessions per date`() {
        val currentMonthDate = TrustedClock.getNowZonedDateTime().toLocalDateTime().withHour(12)
        val previousMonthDate = currentMonthDate.minusMonths(2)

        val currentMonthStat1 = createSessionStatsEntity(creationTime = currentMonthDate)
        val currentMonthStat2 =
            createSessionStatsEntity(creationTime = currentMonthDate.plusHours(1))
        val previousMonthStat1 = createSessionStatsEntity(creationTime = previousMonthDate)
        val previousMonthStat2 =
            createSessionStatsEntity(creationTime = previousMonthDate.minusHours(1))

        val brushings: List<IBrushing> = listOf(mock())

        doReturn(DEFAULT_PROFILE_ID).whenever(statsOfflineProcessor).extractProfileId(brushings)

        mockGetOrCreateMonthStats()

        val sessionStatsGroupedPerDay =
            mutableMapOf<LocalDate, List<BrushingSessionStatsEntity>>().apply {
                put(currentMonthDate.toLocalDate(), listOf(currentMonthStat1, currentMonthStat2))
                put(previousMonthDate.toLocalDate(), listOf(previousMonthStat1, previousMonthStat2))
            }

        val newMonthStats =
            statsOfflineProcessor.updatedMonthsFromNewSessions(brushings, sessionStatsGroupedPerDay)

        val currentMonthStats = newMonthStats.first { it.month == currentMonthDate.toYearMonth() }
        assertEquals(
            listOf(currentMonthStat1, currentMonthStat2),
            currentMonthStats.dayStats.getValue(currentMonthDate.toLocalDate()).brushingSessions
        )

        val previousMonthStats = newMonthStats.first { it.month == previousMonthDate.toYearMonth() }
        assertEquals(
            listOf(previousMonthStat1, previousMonthStat2),
            previousMonthStats.dayStats.getValue(previousMonthDate.toLocalDate()).brushingSessions
        )
    }

    /*
    EXTRACT PROFILE ID
     */
    @Test(expected = IllegalArgumentException::class)
    fun `extractProfileId throws IllegalArgumentException if brushings list is empty`() {
        statsOfflineProcessor.extractProfileId(listOf())
    }

    @Test
    fun `extractProfileId returns profileId from first brushing`() {
        val expectedProfileId = 5454L
        val brushing = mockIBrushing(profileId = expectedProfileId)

        assertEquals(expectedProfileId, statsOfflineProcessor.extractProfileId(listOf(brushing)))
    }

    /*
    ON BRUSHING REMOVED
     */
    @Test
    fun `onBrushingRemoved does nothing if stats offline feature is disabled`() = runBlockingTest {
        setFeatureToggleValue(false)

        statsOfflineProcessor.onBrushingRemoved(mock())

        verify(statsOfflineDao, never()).removeBrushingSessionStat(any())
    }

    @Test
    fun `onBrushingRemoved invokes removeBrushingSessionStat if feature is enabled`() =
        runBlockingTest {
            setFeatureToggleValue(true)

            val expectedCheckupData = mockCheckupData()
            val brushing = prepareIBrushingToBeDeleted(expectedCheckupData)

            statsOfflineProcessor.onBrushingRemoved(brushing)

            val expectedSessionStat = createSessionStatsEntity(
                profileId = brushing.profileId,
                creationTime = brushing.dateTime.toLocalDateTime(),
                duration = brushing.duration,
                averageSurface = expectedCheckupData.surfacePercentage,
                checkup = expectedCheckupData.zoneSurfaceMap
            )

            verify(statsOfflineDao).removeBrushingSessionStat(expectedSessionStat)
        }

    @Test
    fun `onBrushingRemoved checks if MonthStats exists transforming brushing's date to UTC LocalDateTime`() =
        runBlockingTest {
            setFeatureToggleValue(true)

            val brushing = prepareIBrushingToBeDeleted()

            statsOfflineProcessor.onBrushingRemoved(brushing)

            verify(statsOfflineDao).monthWithDays(
                brushing.profileId,
                brushing.dateTime.toLocalDateTime().toYearMonth()
            )
        }

    @Test
    fun `onBrushingRemoved passes empty lists if MonthStats didn't exist for IBrushing's month`() =
        runBlockingTest {
            setFeatureToggleValue(true)

            val brushing = prepareIBrushingToBeDeleted()

            whenever(
                statsOfflineDao.monthWithDays(
                    brushing.profileId,
                    brushing.dateTime.toLocalDateTime().toYearMonth()
                )
            )
                .thenReturn(null)

            whenever(
                statsOfflineDao.weekWithDays(
                    brushing.profileId,
                    brushing.dateTime.toLocalDate().toYearWeek()
                )
            )
                .thenReturn(null)

            statsOfflineProcessor.onBrushingRemoved(brushing)

            verify(statsOfflineDao).update(listOf(), listOf(), listOf(), listOf())
        }

    @Test
    fun `onBrushingRemoved updates DB if MonthStats exists for IBrushing's month`() =
        runBlockingTest {
            setFeatureToggleValue(true)

            val brushing = prepareIBrushingToBeDeleted()

            val monthWithDayStats = createMonthWithDayStats()
            whenever(
                statsOfflineDao.monthWithDays(
                    brushing.profileId,
                    brushing.dateTime.toLocalDateTime().toYearMonth()
                )
            )
                .thenReturn(monthWithDayStats)

            doNothing().whenever(statsOfflineProcessor).updateDatabase(any(), any())

            statsOfflineProcessor.onBrushingRemoved(brushing)

            verify(statsOfflineProcessor).updateDatabase(listOf(monthWithDayStats), listOf())
        }

    /*
    toBrushingSessionStat
     */
    @Test
    fun `toBrushingSessionStat maps an IBrushing to BrushingSessionStatsEntity`() {
        val expectedDate = TrustedClock.getNowOffsetDateTime().minusMinutes(65)
        val expectedDuration = 543L
        val expectedProcessedData = "dada"
        val expectedProfileId = 543L

        val brushing = mockIBrushing(
            date = expectedDate,
            duration = expectedDuration,
            processedData = expectedProcessedData,
            profileId = expectedProfileId
        )

        val expectedSurfacePercentage = 42
        val expectedZoneSurfaceMap = createAverageCheckup(mapOf(MouthZone16.LoIncExt to 57f))

        val expectedCleanPercent = 43
        val expectedMissedPercent = 213
        val expectedPlaqueLeftPercent = 98

        val expectedLoIncExtPercentage = 54
        val expectedUpIncExtPercentage = 87
        val plaqueAggregateLoIncExt =
            mockPlaqueAggregate(PlaqueStatus.Missed, expectedLoIncExtPercentage)
        val plaqueAggregateUpIncExt =
            mockPlaqueAggregate(PlaqueStatus.PlaqueLeft, expectedUpIncExtPercentage)

        val plaqueAggregate = mapOf(
            MouthZone16.LoIncExt to plaqueAggregateLoIncExt,
            MouthZone16.UpIncExt to plaqueAggregateUpIncExt
        )

        val checkupData = mockCheckupData(
            surface = expectedSurfacePercentage,
            zoneCheckupMap = expectedZoneSurfaceMap,
            cleanPercent = expectedCleanPercent,
            missedPercent = expectedMissedPercent,
            plaqueLeftPercent = expectedPlaqueLeftPercent,
            plaqueAggregate = plaqueAggregate
        )
        whenever(checkupCalculator.calculateCheckup(brushing)).thenReturn(checkupData)

        val entity = statsOfflineProcessor.run { brushing.toBrushingSessionStat() }

        assertEquals(expectedDate.toLocalDateTime(), entity.creationTime)
        assertEquals(expectedDuration.toInt(), entity.duration)
        assertEquals(expectedZoneSurfaceMap, entity.averageCheckup)
        assertEquals(expectedSurfacePercentage, entity.averageSurface)
        assertEquals(expectedCleanPercent, entity.cleanPercent)
        assertEquals(expectedMissedPercent, entity.missedPercent)
        assertEquals(expectedPlaqueLeftPercent, entity.plaqueLeftPercent)

        assertEquals(checkupData.plaqueAggregateStats(), entity.plaqueAggregate)
    }

    @Test
    fun `toBrushingSessionStat sets plaqless fields to null if PlaqlessCheckup is null`() {
        val brushing = mockIBrushing()

        val checkupData = mockCheckupData()
        whenever(checkupData.plaqlessCheckupData).thenReturn(null)
        whenever(checkupCalculator.calculateCheckup(brushing)).thenReturn(checkupData)

        val entity = statsOfflineProcessor.run { brushing.toBrushingSessionStat() }

        assertNull(entity.cleanPercent)
        assertNull(entity.missedPercent)
        assertNull(entity.plaqueLeftPercent)
        assertNull(entity.plaqueAggregate)
    }

    @Test
    fun `toBrushingSessionStat sets kml kpi fields to 0 if they are 0`() {
        val brushing = mockIBrushing()

        val checkupData = mockCheckupData()

        assertEquals(0.0, checkupData.correctMovementAverage())
        assertEquals(0.0, checkupData.underSpeedAverage())
        assertEquals(0.0, checkupData.correctSpeedAverage())
        assertEquals(0.0, checkupData.overSpeedAverage())
        assertEquals(0.0, checkupData.correctOrientationAverage())
        assertEquals(0.0, checkupData.overpressureAverage())

        whenever(checkupCalculator.calculateCheckup(brushing)).thenReturn(checkupData)

        val entity = statsOfflineProcessor.run { brushing.toBrushingSessionStat() }

        assertEquals(0.0, entity.correctMovementAverage)
        assertEquals(0.0, entity.underSpeedAverage)
        assertEquals(0.0, entity.correctSpeedAverage)
        assertEquals(0.0, entity.overSpeedAverage)
        assertEquals(0.0, entity.correctOrientationAverage)
        assertEquals(0.0, entity.overPressureAverage)
    }

    @Test
    fun `toBrushingSessionStat sets kml kpi fields to null if they are not null`() {
        val brushing = mockIBrushing()

        val expectedCorrectMovementAverage = 11.1
        val expectedUnderSpeedAverage = 12.2
        val expectedCorrectSpeedAverage = 13.3
        val expectedOverSpeedAverage = 14.4
        val expectedCorrectOrientationAverage = 15.5
        val expectedOverPressureAverage = 16.6

        val checkupData = mockCheckupData(
            correctMovementAverage = expectedCorrectMovementAverage,
            underSpeedAverage = expectedUnderSpeedAverage,
            correctSpeedAverage = expectedCorrectSpeedAverage,
            overSpeedAverage = expectedOverSpeedAverage,
            correctOrientationAverage = expectedCorrectOrientationAverage,
            overPressureAverage = expectedOverPressureAverage
        )

        whenever(checkupCalculator.calculateCheckup(brushing)).thenReturn(checkupData)

        val entity = statsOfflineProcessor.run { brushing.toBrushingSessionStat() }

        assertEquals(expectedCorrectMovementAverage, entity.correctMovementAverage)
        assertEquals(expectedUnderSpeedAverage, entity.underSpeedAverage)
        assertEquals(expectedCorrectSpeedAverage, entity.correctSpeedAverage)
        assertEquals(expectedOverSpeedAverage, entity.overSpeedAverage)
        assertEquals(expectedCorrectOrientationAverage, entity.correctOrientationAverage)
        assertEquals(expectedOverPressureAverage, entity.overPressureAverage)
    }

    /*
    plaqueAggregateStats
     */
    @Test
    fun `plaqueAggregateStats returns null if plaqlessCheckup is null`() {
        val checkupData: CheckupData = mock()
        whenever(checkupData.plaqlessCheckupData).thenReturn(null)

        assertNull(checkupData.plaqueAggregateStats())
    }

    @Test
    fun `plaqueAggregateStats maps plaqueAggregate to plaqueAggregateStats`() {
        val expectedLoIncExtPercentage = 5
        val expectedUpIncExtPercentage = 87
        val plaqueAggregateLoIncExt =
            mockPlaqueAggregate(PlaqueStatus.Missed, expectedLoIncExtPercentage)
        val plaqueAggregateUpIncExt =
            mockPlaqueAggregate(PlaqueStatus.PlaqueLeft, expectedUpIncExtPercentage)

        val checkupData: CheckupData = mock()
        val plaqlessCheckupData: PlaqlessCheckupData = mock()
        whenever(checkupData.plaqlessCheckupData).thenReturn(plaqlessCheckupData)
        whenever(plaqlessCheckupData.plaqueAggregate).thenReturn(
            mapOf(
                MouthZone16.LoIncExt to plaqueAggregateLoIncExt,
                MouthZone16.UpIncExt to plaqueAggregateUpIncExt
            )
        )

        val returnedMap = checkupData.plaqueAggregateStats()

        val expectedMap = mapOf(
            MouthZone16.LoIncExt to StatsPlaqueAggregate(
                status = PlaqueStatus.Missed,
                cleannessPercent = expectedLoIncExtPercentage
            ),
            MouthZone16.UpIncExt to StatsPlaqueAggregate(
                status = PlaqueStatus.PlaqueLeft,
                cleannessPercent = expectedUpIncExtPercentage
            )
        )

        assertEquals(expectedMap, returnedMap)
    }

    /*
    UTILS
     */

    private fun mockPlaqueAggregate(
        status: PlaqueStatus,
        cleannessPercentage: Int
    ): PlaqueAggregate {
        val plaqueAggregate = mock<PlaqueAggregate>()
        whenever(plaqueAggregate.plaqueStatus).thenReturn(status)

        val percentage = mock<Percentage>()
        whenever(percentage.value()).thenReturn(cleannessPercentage)
        whenever(plaqueAggregate.cleannessPercentage).thenReturn(percentage)

        return plaqueAggregate
    }

    private fun prepareIBrushingToBeDeleted(expectedCheckupData: CheckupData = mockCheckupData()): IBrushing {
        val brushing = mockIBrushing()
        whenever(checkupCalculator.calculateCheckup(brushing)).thenReturn(expectedCheckupData)
        return brushing
    }

    private fun mockGetOrCreateMonthStats() {
        whenever(statsOfflineDao.getOrCreateMonthStats(eq(DEFAULT_PROFILE_ID), any())).thenAnswer {
            createMonthWithDayStats(
                monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                    yearMonth = it.getArgument(1)
                )
            )
        }
    }
}
