/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.feature.StatsOfflineFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.utils.forceLocale
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.integrityseal.IntegritySeal
import com.kolibree.statsoffline.integrityseal.IntegritySealTest.Companion.createIntegritySeal
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.BrushingSessionStatDao
import com.kolibree.statsoffline.persistence.DayAggregatedStatsDao
import com.kolibree.statsoffline.persistence.MonthAggregatedStatsDao
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase
import com.kolibree.statsoffline.persistence.StatsOfflineRoomModule
import com.kolibree.statsoffline.persistence.WeekAggregatedStatsDao
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.DEFAULT_SURFACE
import com.kolibree.statsoffline.test.DEFAULT_ZONE_MAP
import com.kolibree.statsoffline.test.createAverageCheckup
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.mockCheckupData
import com.kolibree.statsoffline.test.mockIBrushing
import com.kolibree.statsoffline.test.randomListOfDouble
import com.kolibree.statsoffline.test.toYearMonth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Locale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.Month.DECEMBER
import org.threeten.bp.Month.JANUARY
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

/*
Ugly and messy test class, sorry...
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class StatsOfflineLocalBrushingsProcessorIntegrationTest : BaseInstrumentationTest() {

    private lateinit var sessionStatDao: BrushingSessionStatDao
    private lateinit var monthStatDao: MonthAggregatedStatsDao
    private lateinit var weekStatDao: WeekAggregatedStatsDao
    private lateinit var dayStatDao: DayAggregatedStatsDao
    private lateinit var statsOfflineDatabase: StatsOfflineRoomAppDatabase

    private lateinit var statsOfflineDao: StatsOfflineDao

    private val checkupCalculator: CheckupCalculator = mock()
    private lateinit var featureToggle: StatsOfflineFeatureToggle

    private lateinit var integritySeal: IntegritySeal

    private lateinit var statsProcessor: StatsOfflineLocalBrushingsProcessorImpl

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        initRoom()

        statsOfflineDao = StatsOfflineRoomModule.providesStatsOfflineDao(statsOfflineDatabase)

        integritySeal = createIntegritySeal(context(), statsOfflineDao)

        setupFeatureToggle()

        statsProcessor = StatsOfflineLocalBrushingsProcessorImpl(
            statsOfflineDao,
            checkupCalculator,
            featureToggle,
            integritySeal
        )
    }

    private fun setupFeatureToggle() {
        featureToggle =
            StatsOfflineFeatureToggle(PersistentFeatureToggle(context(), StatsOfflineFeature))
        featureToggle.value = true
    }

    private fun initRoom() {
        statsOfflineDatabase =
            Room.inMemoryDatabaseBuilder(context(), StatsOfflineRoomAppDatabase::class.java)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        dayStatDao = statsOfflineDatabase.dayStatDao()
        monthStatDao = statsOfflineDatabase.monthStatDao()
        weekStatDao = statsOfflineDatabase.weekStatDao()
        sessionStatDao = statsOfflineDatabase.sessionStatDao()
    }

    @Test
    fun emptyDB_userBrushesInDifferentTimezones_weIgnoreTimezoneDifferences() = runBlockingTest {
        /*
        1. The user lives in Paris and sees stats. He's happy and goes to NY on vacation.

        2. On December 31st, Just before boarding a plane to Paris, he opens the app and brushes his teeth at 23:00

        3. After landing in Paris, he opens the app and brushes at 5AM

        First brushing should show on December 31st and the second on January 1st
         */

        TrustedClock.systemZone = ZoneId.of("America/New_York")
        val nyBrushingTime = TrustedClock.getNowZonedDateTime()
            .withYear(2018)
            .withMonth(12)
            .withDayOfMonth(31)
            .withHour(23)
        val nyLocalDate = nyBrushingTime.toLocalDate()
        TrustedClock.setFixedDate(nyBrushingTime)

        verifyDatabaseIsEmpty()

        mockCheckupCalculatorResults()

        val brushingNewYork = mockIBrushing(date = TrustedClock.getNowOffsetDateTime())

        statsProcessor.onBrushingCreated(brushingNewYork)

        TrustedClock.systemZone = ZoneId.of("Europe/London")
        val parisBrushingTime = TrustedClock.getNowOffsetDateTime()
            .withYear(2019)
            .withMonth(1)
            .withDayOfMonth(1)
            .withHour(5)
        val parisLocalDate = parisBrushingTime.toLocalDate()
        TrustedClock.setFixedDate(parisBrushingTime)

        val brushingParis = mockIBrushing(date = TrustedClock.getNowOffsetDateTime())

        statsProcessor.onBrushingCreated(brushingParis)

        // Verify aggregated data for December and January were created, and each brushing placed on the expected day

        val monthEntities = monthStatDao.readAll()
        assertEquals(2, monthEntities.size)

        val decemberMonth = YearMonth.of(2018, DECEMBER)
        val januaryMonth = YearMonth.of(2019, JANUARY)

        val decemberStats = getMonthWithDays(decemberMonth)
        val januaryStats = getMonthWithDays(januaryMonth)

        val expectedNYSessionStats =
            createSessionStatsEntity(creationTime = nyBrushingTime.toLocalDateTime())
        val expectedParisSessionStats =
            createSessionStatsEntity(creationTime = parisBrushingTime.toLocalDateTime())

        assertEquals(
            expectedNYSessionStats,
            decemberStats.dayStats.getValue(nyLocalDate).brushingSessions.single()
        )

        assertEquals(
            expectedParisSessionStats,
            januaryStats.dayStats.getValue(parisLocalDate).brushingSessions.single()
        )

        assertEquals(1, decemberStats.totalSessions)
        assertEquals(1, januaryStats.totalSessions)

        /*
        Verify Week Aggregated stats
         */

        val nyWeekStats = getWeekWithDays(nyLocalDate)
        val parisWeekStats = getWeekWithDays(parisLocalDate)

        assertNotSame(nyWeekStats, parisWeekStats)

        assertEquals(
            expectedNYSessionStats,
            nyWeekStats.dayStats.getValue(nyLocalDate).brushingSessions.single()
        )

        assertEquals(
            expectedParisSessionStats,
            parisWeekStats.dayStats.getValue(parisLocalDate).brushingSessions.single()
        )

        assertEquals(1, parisWeekStats.totalSessions)
        assertEquals(1, nyWeekStats.totalSessions)
    }

    @Test
    fun emptyDB_twoSessions_createsWeekAndDayStatsAndStoresAggregatedValues() = runBlockingTest {
        Locale.setDefault(Locale.FRANCE)

        /*
        Year 2017 has January 1st on Monday, which we should consider week 0
         */
        val dayOfMonth = 3
        val year = 2017

        val currentDate = TrustedClock.getNowOffsetDateTime()
            .withYear(year)
            .withMonth(JANUARY.value)
            .withDayOfMonth(dayOfMonth)
            .withHour(5)
        TrustedClock.setFixedDate(currentDate)

        val processedDataDay1 = "da"
        val processedDataDay2 = "yu"
        val processedDataFuture = "not taken into account"

        val duration = arrayOf(20L, 40L, 11L, 53L, 4567L)
        val surface = arrayOf(0, 11, 100, 33, 4567)

        val mouthZone1 = MouthZone16.UpMolRiOcc
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpIncExt
        val mouthZone4 = MouthZone16.UpIncInt
        val averageCheckupDay1 =
            createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))
        val averageCheckupFirstBrushingDay2 =
            createAverageCheckup(mapOf(mouthZone1 to 23f, mouthZone2 to 78f, mouthZone4 to 10f))
        val averageCheckupSecondBrushingDay2 = createAverageCheckup(mapOf(mouthZone4 to 76f))
        val averageCheckupDay3 = createAverageCheckup(mapOf(mouthZone1 to 44f, mouthZone4 to 61f))
        val averageCheckupFuture =
            createAverageCheckup(mapOf(mouthZone2 to 1f, mouthZone3 to 14f, mouthZone4 to 6f))

        val brushingDay1 = mockIBrushing(
            date = currentDate.minusDays(2),
            processedData = processedDataDay1,
            duration = duration[0]
        )

        val firstBrushingDay2 = mockIBrushing(
            date = currentDate.minusDays(1).withHour(4),
            processedData = processedDataDay2,
            duration = duration[1]
        )

        val secondBrushingDay2 = mockIBrushing(
            date = currentDate.minusDays(1).withHour(5),
            processedData = processedDataDay2,
            duration = duration[2]
        )

        val brushingDay3 = mockIBrushing(
            date = currentDate,
            processedData = processedDataDay2,
            duration = duration[3]
        )

        // this brushing should be ignored from average calculations
        val brushingFuture = mockIBrushing(
            date = currentDate.plusDays(1),
            processedData = processedDataFuture,
            duration = duration[4]
        )

        val correctMovementAverages = randomListOfDouble(size = 5)
        val underSpeedAverages = randomListOfDouble(size = 5)
        val correctSpeedAverages = randomListOfDouble(size = 5)
        val overSpeedAverages = randomListOfDouble(size = 5)
        val correctOrientationAverages = randomListOfDouble(size = 5)

        mockCheckupCalculatorResult(
            brushingDay1,
            surface = surface[0],
            zoneCheckupMap = averageCheckupDay1,
            correctMovementAverage = correctMovementAverages[0],
            correctOrientationAverage = correctOrientationAverages[0],
            correctSpeedAverage = correctSpeedAverages[0],
            underSpeedAverage = underSpeedAverages[0],
            overSpeedAverage = overSpeedAverages[0]
        )
        mockCheckupCalculatorResult(
            firstBrushingDay2,
            surface = surface[1],
            zoneCheckupMap = averageCheckupFirstBrushingDay2,
            correctMovementAverage = correctMovementAverages[1],
            correctOrientationAverage = correctOrientationAverages[1],
            correctSpeedAverage = correctSpeedAverages[1],
            underSpeedAverage = underSpeedAverages[1],
            overSpeedAverage = overSpeedAverages[1]
        )
        mockCheckupCalculatorResult(
            secondBrushingDay2,
            surface = surface[2],
            zoneCheckupMap = averageCheckupSecondBrushingDay2,
            correctMovementAverage = correctMovementAverages[2],
            correctOrientationAverage = correctOrientationAverages[2],
            correctSpeedAverage = correctSpeedAverages[2],
            underSpeedAverage = underSpeedAverages[2],
            overSpeedAverage = overSpeedAverages[2]
        )
        mockCheckupCalculatorResult(
            brushingDay3,
            surface = surface[3],
            zoneCheckupMap = averageCheckupDay3,
            correctMovementAverage = correctMovementAverages[3],
            correctOrientationAverage = correctOrientationAverages[3],
            correctSpeedAverage = correctSpeedAverages[3],
            underSpeedAverage = underSpeedAverages[3],
            overSpeedAverage = overSpeedAverages[3]
        )
        mockCheckupCalculatorResult(
            brushingFuture,
            surface = surface[4],
            zoneCheckupMap = averageCheckupFuture,
            correctMovementAverage = correctMovementAverages[4],
            correctOrientationAverage = correctOrientationAverages[4],
            correctSpeedAverage = correctSpeedAverages[4],
            underSpeedAverage = underSpeedAverages[4],
            overSpeedAverage = overSpeedAverages[4]
        )

        verifyDatabaseIsEmpty()

        assertEquals(Locale.FRANCE, Locale.getDefault())
        statsProcessor.onBrushingsCreated(
            listOf(
                brushingDay1,
                brushingDay3,
                firstBrushingDay2,
                secondBrushingDay2,
                brushingFuture
            )
        )

        val week0Stats = getWeekWithDays(brushingDay1.dateTime.toLocalDate()).calculateAverage()

        /*
        Week 0 should ignore previous year's values
         */
        val expectedAverageDurationWeek0 = duration[0].toDouble()
        val expectedAverageSurfaceWeek0 = surface[0].toDouble()
        val expectedCorrectOrientationWeek0 = correctOrientationAverages[0].roundOneDecimal()
        val expectedCorrectMovementWeek0 = correctMovementAverages[0].roundOneDecimal()
        val expectedCorrectSpeedWeek0 = correctSpeedAverages[0].roundOneDecimal()
        val expectedUnderSpeedWeek0 = underSpeedAverages[0].roundOneDecimal()
        val expectedOverSpeedWeek0 = overSpeedAverages[0].roundOneDecimal()

        assertEquals(expectedAverageDurationWeek0, week0Stats.averageDuration)
        assertEquals(expectedAverageSurfaceWeek0, week0Stats.averageSurface)
        assertEquals(expectedCorrectOrientationWeek0, week0Stats.correctOrientationAverage)
        assertEquals(expectedCorrectMovementWeek0, week0Stats.correctMovementAverage)
        assertEquals(expectedCorrectSpeedWeek0, week0Stats.correctSpeedAverage)
        assertEquals(expectedOverSpeedWeek0, week0Stats.overSpeedAverage)
        assertEquals(expectedUnderSpeedWeek0, week0Stats.underSpeedAverage)
        assertEquals(averageCheckupDay1, week0Stats.averageCheckup)

        val week1Stats = getWeekWithDays(firstBrushingDay2.dateTime.toLocalDate())

        assertNotSame(week0Stats.week, week1Stats.week)

        /*
        Today is day 2, thus
        - Week doesn't read session by session, but reads the values from DayWithSessions. Thus the (40+11)/2
        - We ignore future dates
        - We ignore past week brushing sessions
         */
        fun List<Double>.week1Averages() = listOf(
            listOf(this[1], this[2]).calculateStatsAverage(),
            this[3]
        ).calculateStatsAverage()

        fun Array<Long>.week1Averages() =
            ((((this[1] + this[2]) / 2f) + this[3]) / 2.0).roundOneDecimal()

        fun Array<Int>.week1Averages() =
            ((((this[1] + this[2]) / 2f) + this[3]) / 2.0).roundOneDecimal()

        val expectedAverageDurationWeek1 = duration.week1Averages()
        val expectedAverageSurfaceWeek1 = surface.week1Averages()

        val expectedCorrectOrientationWeek1 = correctOrientationAverages.week1Averages()
        val expectedCorrectMovementWeek1 = correctMovementAverages.week1Averages()
        val expectedCorrectSpeedWeek1 = correctSpeedAverages.week1Averages()
        val expectedUnderSpeedWeek1 = underSpeedAverages.week1Averages()
        val expectedOverSpeedWeek1 = overSpeedAverages.week1Averages()

        assertEquals(expectedAverageDurationWeek1, week1Stats.averageDuration)
        assertEquals(expectedAverageSurfaceWeek1, week1Stats.averageSurface)
        assertEquals(expectedCorrectOrientationWeek1, week1Stats.correctOrientationAverage)
        assertEquals(expectedCorrectMovementWeek1, week1Stats.correctMovementAverage)
        assertEquals(expectedCorrectSpeedWeek1, week1Stats.correctSpeedAverage)
        assertEquals(expectedUnderSpeedWeek1, week1Stats.underSpeedAverage)
        assertEquals(expectedOverSpeedWeek1, week1Stats.overSpeedAverage)

        val daysWithSessions = 2.0
        val expectedAvgZone1 = ((23 / 2f + 44f) / daysWithSessions).roundOneDecimalToFloat()
        val expectedAvgZone2 = ((78 / 2f) / daysWithSessions).roundOneDecimalToFloat()
        val expectedAvgZone4 =
            (((10f + 76f) / 2f + 61f) / daysWithSessions).roundOneDecimalToFloat()

        val expectedAverageCheckup = createAverageCheckup(
            mapOf(
                mouthZone1 to expectedAvgZone1,
                mouthZone2 to expectedAvgZone2,
                mouthZone4 to expectedAvgZone4
            )
        )
        assertEquals(expectedAvgZone1, week1Stats.averageCheckup.getValue(mouthZone1))
        assertEquals(expectedAvgZone2, week1Stats.averageCheckup.getValue(mouthZone2))
        assertEquals(expectedAvgZone4, week1Stats.averageCheckup.getValue(mouthZone4))
        assertEquals(expectedAverageCheckup, week1Stats.averageCheckup)

        statsProcessor.onBrushingRemoved(secondBrushingDay2)

        val week0StatsAfterRemoval = getWeekWithDays(brushingDay1.dateTime.toLocalDate())
        val week1StatsAfterRemoval = getWeekWithDays(firstBrushingDay2.dateTime.toLocalDate())

        assertEquals(week0Stats, week0StatsAfterRemoval)

        fun List<Double>.week1AveragesAfterRemoval() =
            listOf(this[1], this[3]).calculateStatsAverage()

        fun Array<Long>.week1AveragesAfterRemoval() = ((this[1] + this[3]) / 2.0).roundOneDecimal()
        fun Array<Int>.week1AveragesAfterRemoval() = ((this[1] + this[3]) / 2.0).roundOneDecimal()

        val expectedAverageDurationWeek1AfterRemoval = duration.week1AveragesAfterRemoval()
        val expectedAverageSurfaceWeek1AfterRemoval = surface.week1AveragesAfterRemoval()

        val expectedCorrectOrientationWeek1AfterRemoval =
            correctOrientationAverages.week1AveragesAfterRemoval()
        val expectedCorrectMovementWeek1AfterRemoval =
            correctMovementAverages.week1AveragesAfterRemoval()
        val expectedCorrectSpeedWeek1AfterRemoval = correctSpeedAverages.week1AveragesAfterRemoval()
        val expectedUnderSpeedWeek1AfterRemoval = underSpeedAverages.week1AveragesAfterRemoval()
        val expectedOverSpeedWeek1AfterRemoval = overSpeedAverages.week1AveragesAfterRemoval()

        assertEquals(
            expectedAverageDurationWeek1AfterRemoval,
            week1StatsAfterRemoval.averageDuration
        )
        assertEquals(expectedAverageSurfaceWeek1AfterRemoval, week1StatsAfterRemoval.averageSurface)

        assertEquals(
            expectedCorrectOrientationWeek1AfterRemoval,
            week1StatsAfterRemoval.correctOrientationAverage
        )
        assertEquals(
            expectedCorrectMovementWeek1AfterRemoval,
            week1StatsAfterRemoval.correctMovementAverage
        )
        assertEquals(
            expectedCorrectSpeedWeek1AfterRemoval,
            week1StatsAfterRemoval.correctSpeedAverage
        )
        assertEquals(expectedUnderSpeedWeek1AfterRemoval, week1StatsAfterRemoval.underSpeedAverage)
        assertEquals(expectedOverSpeedWeek1AfterRemoval, week1StatsAfterRemoval.overSpeedAverage)

        val expectedAvgZone1AfterRemoval = ((23f + 44f) / daysWithSessions).roundOneDecimalToFloat()
        val expectedAvgZone2AfterRemoval = (78f / daysWithSessions).roundOneDecimalToFloat()
        val expectedAvgZone4AfterRemoval = ((10f + 61f) / daysWithSessions).roundOneDecimalToFloat()

        val expectedAverageCheckupAfterRemoval = createAverageCheckup(
            mapOf(
                mouthZone1 to expectedAvgZone1AfterRemoval,
                mouthZone2 to expectedAvgZone2AfterRemoval,
                mouthZone4 to expectedAvgZone4AfterRemoval
            )
        )
        assertEquals(
            expectedAvgZone1AfterRemoval,
            week1StatsAfterRemoval.averageCheckup.getValue(mouthZone1)
        )
        assertEquals(
            expectedAvgZone2AfterRemoval,
            week1StatsAfterRemoval.averageCheckup.getValue(mouthZone2)
        )
        assertEquals(
            expectedAvgZone4AfterRemoval,
            week1StatsAfterRemoval.averageCheckup.getValue(mouthZone4)
        )
        assertEquals(expectedAverageCheckupAfterRemoval, week1StatsAfterRemoval.averageCheckup)
    }

    @Test
    fun emptyDB_twoSessions_createsMonthAndDayStatsAndStoresValues() = runBlockingTest {
        val dayOfMonth = 10

        // Wednesday, 10/07/2019
        val currentDate = TrustedClock.getNowOffsetDateTime()
            .withYear(2019)
            .withMonth(Month.JULY.value)
            .withDayOfMonth(dayOfMonth)
            .withHour(5)
        TrustedClock.setFixedDate(currentDate)

        val processedDataYesterday = "da"
        val processedDataTwoDaysAgo = "yu"
        val processedDataFuture = "not taken into account"

        val duration = arrayOf(20L, 40L, 4567L)
        val surface = arrayOf(0, 100, 4567)

        val mouthZone1 = MouthZone16.UpMolRiOcc
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpIncExt
        val mouthZone4 = MouthZone16.UpIncInt
        val averageCheckup1 =
            createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))
        val averageCheckup2 =
            createAverageCheckup(mapOf(mouthZone1 to 23f, mouthZone2 to 78f, mouthZone4 to 10f))

        val brushingYesterday = mockIBrushing(
            date = currentDate.minusDays(1),
            processedData = processedDataYesterday,
            duration = duration[0]
        )
        val brushingTwoDaysAgo = mockIBrushing(
            date = currentDate.minusDays(2),
            processedData = processedDataTwoDaysAgo,
            duration = duration[1]
        )

        // this brushing sould be ignored from average calculations
        val brushingFuture = mockIBrushing(
            date = currentDate.plusDays(1),
            processedData = processedDataFuture,
            duration = duration[2]
        )

        val brushingsToInsert = listOf(brushingYesterday, brushingTwoDaysAgo, brushingFuture)

        /*
        The last value of the list won't be taken into account because it'll belong to brushingFuture
         */
        val correctMovementAverages = randomListOfDouble(size = brushingsToInsert.size)
        val underSpeedAverages = randomListOfDouble(size = brushingsToInsert.size)
        val correctSpeedAverages = randomListOfDouble(size = brushingsToInsert.size)
        val overSpeedAverages = randomListOfDouble(size = brushingsToInsert.size)
        val correctOrientationAverages = randomListOfDouble(size = brushingsToInsert.size)

        mockCheckupCalculatorResult(
            brushingYesterday,
            surface = surface[0],
            zoneCheckupMap = averageCheckup1,
            correctMovementAverage = correctMovementAverages[0],
            correctOrientationAverage = correctOrientationAverages[0],
            correctSpeedAverage = correctSpeedAverages[0],
            underSpeedAverage = underSpeedAverages[0],
            overSpeedAverage = overSpeedAverages[0]
        )

        mockCheckupCalculatorResult(
            brushingTwoDaysAgo,
            surface = surface[1],
            zoneCheckupMap = averageCheckup2,
            correctMovementAverage = correctMovementAverages[1],
            correctOrientationAverage = correctOrientationAverages[1],
            correctSpeedAverage = correctSpeedAverages[1],
            underSpeedAverage = underSpeedAverages[1],
            overSpeedAverage = overSpeedAverages[1]
        )

        mockCheckupCalculatorResult(
            brushingFuture,
            surface = surface[2],
            zoneCheckupMap = averageCheckup2,
            correctMovementAverage = correctMovementAverages[2],
            correctOrientationAverage = correctOrientationAverages[2],
            correctSpeedAverage = correctSpeedAverages[2],
            underSpeedAverage = underSpeedAverages[2],
            overSpeedAverage = overSpeedAverages[2]
        )

        verifyDatabaseIsEmpty()

        statsProcessor.onBrushingsCreated(brushingsToInsert)

        // verify database records are created
        val insertedMonth = monthStatDao.readAll().single()

        assertEquals(YearMonth.from(currentDate), insertedMonth.month)
        assertEquals(DEFAULT_PROFILE_ID, insertedMonth.profileId)

        // assert we created a DayStatsEntity for each day of month
        val insertedDays = dayStatDao.readAll()
        assertEquals(insertedMonth.dates.size, insertedDays.size)

        val day1DateTime = brushingYesterday.dateTime
        val day2DateTime = brushingTwoDaysAgo.dateTime
        val futureDateTime = brushingFuture.dateTime
        val expectedSessionStat1 =
            createSessionStatsEntity(
                creationTime = day1DateTime.toLocalDateTime(),
                duration = duration[0],
                averageSurface = surface[0],
                checkup = averageCheckup1,
                correctMovementAverage = correctMovementAverages[0],
                correctOrientationAverage = correctOrientationAverages[0],
                correctSpeedAverage = correctSpeedAverages[0],
                underSpeedAverage = underSpeedAverages[0],
                overSpeedAverage = overSpeedAverages[0]
            )
        val expectedSessionStat2 =
            createSessionStatsEntity(
                creationTime = day2DateTime.toLocalDateTime(),
                duration = duration[1],
                averageSurface = surface[1],
                checkup = averageCheckup2,
                correctMovementAverage = correctMovementAverages[1],
                correctOrientationAverage = correctOrientationAverages[1],
                correctSpeedAverage = correctSpeedAverages[1],
                underSpeedAverage = underSpeedAverages[1],
                overSpeedAverage = overSpeedAverages[1]
            )
        val expectedSessionStatFuture =
            createSessionStatsEntity(
                creationTime = futureDateTime.toLocalDateTime(),
                duration = duration[2],
                averageSurface = surface[2],
                checkup = averageCheckup2,
                correctMovementAverage = correctMovementAverages[2],
                correctOrientationAverage = correctOrientationAverages[2],
                correctSpeedAverage = correctSpeedAverages[2],
                underSpeedAverage = underSpeedAverages[2],
                overSpeedAverage = overSpeedAverages[2]
            )

        val insertedSessions = sessionStatDao.readAll()
        assertEquals(3, insertedSessions.size)
        assertTrue(
            "Expected $expectedSessionStat1\n$expectedSessionStat2\nwas: $insertedSessions",
            insertedSessions.containsAll(
                listOf(
                    expectedSessionStat1,
                    expectedSessionStat2,
                    expectedSessionStatFuture
                )
            )
        )

        // verify statsOfflineDao returns expected data
        val monthWithDays = getMonthWithDays(YearMonth.from(currentDate))

        assertEquals(insertedMonth, monthWithDays.monthStats)
        val day1Stats = monthWithDays.dayStats.getValue(day1DateTime.toLocalDate())
        val day2Stats = monthWithDays.dayStats.getValue(day2DateTime.toLocalDate())
        val futureStats = monthWithDays.dayStats.getValue(futureDateTime.toLocalDate())

        assertEquals(expectedSessionStat1, day1Stats.brushingSessions.single())
        assertEquals(expectedSessionStat2, day2Stats.brushingSessions.single())
        assertEquals(expectedSessionStatFuture, futureStats.brushingSessions.single())

        assertEquals(
            (duration[0] + duration[1]) / listOf(
                duration[0],
                duration[1]
            ).filter { it != 0L }.size.toDouble(),
            monthWithDays.averageDuration
        )
        assertEquals(
            (surface[0] + surface[1]) / listOf(
                surface[0],
                surface[1]
            ).filter { it != 0 }.size.toDouble(),
            monthWithDays.averageSurface
        )

        val daysWithSessions = 2
        val expectedAvgZone1 = (80f + 23f) / daysWithSessions
        val expectedAvgZone2 = (5f + 78f) / daysWithSessions
        val expectedAvgZone3 = (2f + 0f) / daysWithSessions
        val expectedAvgZone4 = (0f + 10f) / daysWithSessions

        val expectedAverageCheckup = createAverageCheckup(
            mapOf(
                mouthZone1 to expectedAvgZone1,
                mouthZone2 to expectedAvgZone2,
                mouthZone3 to expectedAvgZone3,
                mouthZone4 to expectedAvgZone4
            )
        )
        assertEquals(expectedAverageCheckup, monthWithDays.averageCheckup)

        // skip the last value, it belongs to a brushing in the future
        fun List<Double>.monthAverage() = take(size - 1).calculateStatsAverage()

        val expectedCorrectOrientation = correctOrientationAverages.monthAverage()
        val expectedCorrectMovement = correctMovementAverages.monthAverage()
        val expectedCorrectSpeed = correctSpeedAverages.monthAverage()
        val expectedUnderSpeed = underSpeedAverages.monthAverage()
        val expectedOverSpeed = overSpeedAverages.monthAverage()

        assertEquals(expectedCorrectOrientation, monthWithDays.correctOrientationAverage)
        assertEquals(expectedCorrectMovement, monthWithDays.correctMovementAverage)
        assertEquals(expectedCorrectSpeed, monthWithDays.correctSpeedAverage)
        assertEquals(expectedUnderSpeed, monthWithDays.underSpeedAverage)
        assertEquals(expectedOverSpeed, monthWithDays.overSpeedAverage)
    }

    @Test
    fun dbWithPreExistingContent_createsMonthAndDayStatsTakingIntoAccountKolibreeDay() =
        runBlockingTest {
            TrustedClock.systemZone = ZoneOffset.ofHours(6) // Shanghai timezone

            val currentDateTime = TrustedClock.getNowOffsetDateTime()
                .withDayOfMonth(2)
                .withHour(12)
            TrustedClock.setFixedDate(currentDateTime)

            val yesterdayDateTime = currentDateTime.minusDays(1)
            val twoDaysAgoDateTime = currentDateTime.minusDays(2)

            val currentMonth = currentDateTime.toYearMonth()
            val previousMonth = currentMonth.minusMonths(1)

            statsOfflineDao.getOrCreateMonthStats(
                profileId = DEFAULT_PROFILE_ID,
                month = currentMonth
            )
            statsOfflineDao.getOrCreateMonthStats(
                profileId = DEFAULT_PROFILE_ID,
                month = previousMonth
            )

            val existingSessionForYesterday =
                createSessionStatsEntity(
                    creationTime = currentDateTime.minusDays(1).toLocalDateTime()
                )
            sessionStatDao.insert(listOf(existingSessionForYesterday))

            // verify DB content pre test
            val todayStats =
                getMonthWithDays(currentMonth).dayStats.getValue(currentDateTime.toLocalDate())
            val yesterdayStats =
                getMonthWithDays(currentMonth).dayStats.getValue(yesterdayDateTime.toLocalDate())
            val twoDaysAgoStats =
                getMonthWithDays(previousMonth).dayStats.getValue(twoDaysAgoDateTime.toLocalDate())

            assertEquals(existingSessionForYesterday, yesterdayStats.brushingSessions.single())
            assertTrue(todayStats.brushingSessions.isEmpty())
            assertTrue(twoDaysAgoStats.brushingSessions.isEmpty())

            mockCheckupCalculatorResults()

            // Let's work with edge values to detect timezone issues
            // Each new brushing should be inserted at the date it was created at translated to kolibree day
            val newBrushingYesterday1 = mockIBrushing(date = yesterdayDateTime.withHour(23))
            // this should be translated to two days ago kolibree day
            val newBrushingYesterday2 = mockIBrushing(date = yesterdayDateTime.withHour(3))
            val newBrushingTwoDaysAgo = mockIBrushing(date = twoDaysAgoDateTime)

            statsProcessor.onBrushingsCreated(
                listOf(
                    newBrushingYesterday1,
                    newBrushingYesterday2,
                    newBrushingTwoDaysAgo
                )
            )

            // verify DB content post test
            val newTodayStats =
                getMonthWithDays(currentMonth).dayStats.getValue(currentDateTime.toLocalDate())

            val newYesterdayStats =
                getMonthWithDays(currentMonth).dayStats.getValue(yesterdayDateTime.toLocalDate())

            val newTwoDaysAgoStats =
                getMonthWithDays(previousMonth).dayStats.getValue(twoDaysAgoDateTime.toLocalDate())

            assertTrue(newTodayStats.brushingSessions.isEmpty())

            val newExpectedSessionYesterday =
                createSessionStatsEntity(
                    creationTime = newBrushingYesterday1.dateTime.toLocalDateTime()
                )
            assertEquals(2, newYesterdayStats.brushingSessions.size)
            assertTrue(newYesterdayStats.brushingSessions.contains(existingSessionForYesterday))
            assertTrue(newYesterdayStats.brushingSessions.contains(newExpectedSessionYesterday))

            val newExpectedSessionTwoDaysAgo1 =
                createSessionStatsEntity(
                    creationTime = newBrushingYesterday2.dateTime.toLocalDateTime()
                )
            val newExpectedSessionTwoDaysAgo2 =
                createSessionStatsEntity(
                    creationTime = newBrushingTwoDaysAgo.dateTime.toLocalDateTime()
                )
            assertEquals(2, newTwoDaysAgoStats.brushingSessions.size)
            assertTrue(
                "Expected: $newExpectedSessionTwoDaysAgo1, was: ${newTwoDaysAgoStats.brushingSessions}",
                newTwoDaysAgoStats.brushingSessions.contains(newExpectedSessionTwoDaysAgo1)
            )
            assertTrue(newTwoDaysAgoStats.brushingSessions.contains(newExpectedSessionTwoDaysAgo2))

            assertTrue(newTodayStats.brushingSessions.isEmpty())

            assertEquals(0, newTodayStats.totalSessions)
            assertEquals(2, newYesterdayStats.totalSessions)
            assertEquals(2, newTwoDaysAgoStats.totalSessions)
        }

    @Test
    fun dbWithPreExistingContent_removeOneBrushing_deletesSessionEvenIfTimezoneChanged() =
        runBlockingTest {
            TrustedClock.systemZone = ZoneOffset.ofHours(6) // Shanghai timezone

            val currentDate = TrustedClock.getNowOffsetDateTime()
                .withMonth(10)
                .withDayOfMonth(1)
                .withHour(5)
            TrustedClock.setFixedDate(currentDate)

            val yesterdayDate = currentDate.minusDays(1)
            val currentMonth = YearMonth.from(currentDate)
            val previousMonth = YearMonth.from(yesterdayDate)

            val processedDataYesterday = "da"
            val processedDataToday = "toooday"

            val todayDuration = 865L
            val yesterdayDuration = 123L
            val todaySurface = 865
            val yesterdaySurface = 80
            val brushingYesterday = mockIBrushing(
                date = yesterdayDate,
                processedData = processedDataYesterday,
                duration = yesterdayDuration
            )
            val brushingToday = mockIBrushing(
                date = currentDate,
                processedData = processedDataToday,
                duration = todayDuration
            )
            mockCheckupCalculatorResult(brushingYesterday, surface = yesterdaySurface)
            mockCheckupCalculatorResult(brushingToday, surface = todaySurface)

            statsProcessor.onBrushingsCreated(listOf(brushingYesterday, brushingToday))

            // verify DB content pre test
            val currentMonthWithDays = getMonthWithDays(currentMonth)
            val previousMonthWithDays = getMonthWithDays(previousMonth)

            val previousMonthLength = 1.toDouble()
            assertEquals(
                (yesterdayDuration / previousMonthLength).roundOneDecimalToFloat(),
                previousMonthWithDays.averageDuration.roundOneDecimalToFloat()
            )
            assertEquals(
                (yesterdaySurface / previousMonthLength).roundOneDecimalToFloat(),
                previousMonthWithDays.averageSurface.roundOneDecimalToFloat()
            )

            assertEquals(todayDuration.toDouble(), currentMonthWithDays.averageDuration)
            assertEquals(todaySurface.toDouble(), currentMonthWithDays.averageSurface)

            val expectedExistingSessionToday =
                createSessionStatsEntity(
                    creationTime = currentDate.toLocalDateTime(),
                    duration = todayDuration,
                    averageSurface = todaySurface
                )
            val expectedExistingSessionYesterday = createSessionStatsEntity(
                creationTime = yesterdayDate.toLocalDateTime(),
                duration = yesterdayDuration,
                averageSurface = yesterdaySurface
            )

            val yesterdayStats =
                previousMonthWithDays.dayStats.getValue(yesterdayDate.toLocalDate())
            assertEquals(expectedExistingSessionYesterday, yesterdayStats.brushingSessions.single())

            val todayStats = currentMonthWithDays.dayStats.getValue(currentDate.toLocalDate())
            assertEquals(expectedExistingSessionToday, todayStats.brushingSessions.single())

            // 2019-07-10T11:00:53.397
            val brushingRemoved = mockIBrushing(date = currentDate)
            mockCheckupCalculatorResult(brushingRemoved)

            assertEquals(1, todayStats.totalSessions)
            assertEquals(1, currentMonthWithDays.totalSessions)

            val weekStats = getWeekWithDays(currentDate.toLocalDate())
            val weekSessions =
                if (expectedExistingSessionToday.assignedDate.toYearWeek() == expectedExistingSessionYesterday.assignedDate.toYearWeek()) 2 else 1
            assertEquals(weekSessions, weekStats.totalSessions)

            statsProcessor.onBrushingRemoved(brushingRemoved)

            // verify DB content post test
            val newCurrentMonthWithDays = getMonthWithDays(currentMonth)
            val newPreviousMonthWithDays = getMonthWithDays(previousMonth)

            assertEquals(newPreviousMonthWithDays, previousMonthWithDays)

            val newTodayStats = newCurrentMonthWithDays.dayStats.getValue(currentDate.toLocalDate())
            assertTrue(newTodayStats.brushingSessions.isEmpty())

            assertEquals(0.0, newCurrentMonthWithDays.averageDuration)
            assertEquals(0.0, newCurrentMonthWithDays.averageSurface)

            assertEquals(0, newTodayStats.totalSessions)
            assertEquals(0, newCurrentMonthWithDays.totalSessions)

            val newWeekStats = getWeekWithDays(currentDate.toLocalDate())
            assertEquals(weekSessions - 1, newWeekStats.totalSessions)
        }

    @Test
    fun emptyDB_firstWeekOf2019_doesNotIncludePastYearInAggregatedData() = runBlockingTest {
        Locale.setDefault(Locale.FRANCE)

        /*
        2019 January 1st is Tuesday. We shouldn't take into account past year's brushing for week aggregated states
         */
        val dayOfMonth = 1
        val year = 2019

        val currentDate = TrustedClock.getNowOffsetDateTime()
            .withYear(year)
            .withMonth(JANUARY.value)
            .withDayOfMonth(dayOfMonth)
            .withHour(5)
        TrustedClock.setFixedDate(currentDate)

        /**
         * Let's also verify that Kolibree Day is taken into account in WeekAggregatedStats
         */
        val january1st2018Duration = 12L
        val january1st2019Duration = 180L
        val january2ndDuration = 123L
        val january1st2018Surface = 87
        val january1st2019Surface = 32
        val january2ndSurface = 11

        val processedDataanuary1st2018 = "da"
        val processedDataanuary1st2019 = "toooday"
        val processedDataJanuary2nd = "trololo"

        val brushingJanuary1st2018 = mockIBrushing(
            date = currentDate.withHour(3), // force kolibreeDay = December 31st 2018
            processedData = processedDataanuary1st2018,
            duration = january1st2018Duration
        )
        val brushingJanuary1st2019 = mockIBrushing(
            date = currentDate,
            processedData = processedDataanuary1st2019,
            duration = january1st2019Duration
        )
        val brushingJanuary2nd = mockIBrushing(
            date = currentDate.plusDays(1),
            processedData = processedDataJanuary2nd,
            duration = january2ndDuration
        )

        mockCheckupCalculatorResult(brushingJanuary1st2018, surface = january1st2018Surface)
        mockCheckupCalculatorResult(brushingJanuary1st2019, surface = january1st2019Surface)
        mockCheckupCalculatorResult(brushingJanuary2nd, surface = january2ndSurface)

        verifyDatabaseIsEmpty()

        statsProcessor.onBrushingsCreated(
            listOf(
                brushingJanuary2nd,
                brushingJanuary1st2019,
                brushingJanuary1st2018
            )
        )

        val week53_2018 = getWeekWithDays(currentDate.minusDays(1).toLocalDate())
        val week1_2019 = getWeekWithDays(currentDate.toLocalDate())

        assertEquals(YearWeek.of(2018, 53), week53_2018.week)
        assertEquals(YearWeek.of(2019, 1), week1_2019.week)

        assertEquals(1, week53_2018.sessionsMap.keys.size)
        val totalSessionsWeek53 =
            week53_2018.sessionsMap.mapValues { it.value.sessions }.values.flatten()
        assertEquals(1, totalSessionsWeek53.size)

        assertEquals(6, week1_2019.sessionsMap.keys.size)
        val totalSessionsWeek1 =
            week1_2019.sessionsMap.mapValues { it.value.sessions }.values.flatten()
        assertEquals(2, totalSessionsWeek1.size)
    }

    @Test
    fun readByDate_usesKolibreeDayToFetchFromDB() = runBlockingTest {
        Locale.setDefault(Locale.FRANCE)

        /*
        2019 January 1st is Tuesday. We shouldn't take into account past year's brushing for week aggregated states
         */
        val dayOfMonth = 1
        val year = 2019

        val currentDate = TrustedClock.getNowOffsetDateTime()
            .withYear(year)
            .withMonth(JANUARY.value)
            .withDayOfMonth(dayOfMonth)
            .withHour(5)
        TrustedClock.setFixedDate(currentDate)

        // force kolibreeDay = December 31st 2018
        val yesterdayKolibreeDay = currentDate.withHour(3)
        val tomorrow = currentDate.plusDays(1)
        val brushingJanuary1st2018 = mockIBrushing(date = yesterdayKolibreeDay)
        val brushingJanuary1st2019 = mockIBrushing(date = currentDate)
        val brushingJanuary2nd = mockIBrushing(date = tomorrow)

        mockCheckupCalculatorResults()

        statsProcessor.onBrushingsCreated(
            listOf(
                brushingJanuary2nd,
                brushingJanuary1st2019,
                brushingJanuary1st2018
            )
        )

        assertEquals(3, sessionStatDao.readAll().size)

        assertEquals(
            tomorrow.toLocalDate(),
            sessionStatDao.readByDateTime(
                DEFAULT_PROFILE_ID,
                tomorrow.toLocalDateTime()
            ).single().assignedDate
        )

        val yesterday = currentDate.minusDays(1)
        assertEquals(
            yesterday.toLocalDate(),
            sessionStatDao.readByDateTime(
                DEFAULT_PROFILE_ID,
                yesterday.toLocalDateTime()
            ).single().assignedDate
        )

        assertEquals(
            currentDate.toLocalDate(),
            sessionStatDao.readByDateTime(
                DEFAULT_PROFILE_ID,
                currentDate.toLocalDateTime()
            ).single().assignedDate
        )
    }

    /**
     * https://kolibree.atlassian.net/browse/KLTB002-9941
     */
    @Test
    fun whenUserChangesFirstDayOfWeek_andWeInvokeOnBrushingsCreated_thenWeShouldntCrash() {
        val nov23 = TrustedClock.getNowOffsetDateTime()
            .withYear(2019)
            .withMonth(11)
            .withDayOfMonth(23)
            .withHour(12)

        val nov24 = nov23.plusDays(1)

        val mondayAsFirstDayOfWeekLocale = Locale.FRANCE
        val sundayAsFirstDayOfWeekLocale = Locale.US

        mockCheckupCalculatorResults()

        context().forceLocale(mondayAsFirstDayOfWeekLocale) {
            runBlockingTest {
                assertEquals(
                    nov23.toLocalDate().toYearWeek(),
                    nov24.toLocalDate().toYearWeek()
                )

                val brushingNov23 = mockIBrushing(date = nov23)
                val brushingNov24 = mockIBrushing(date = nov24)

                statsProcessor.onBrushingsCreated(listOf(brushingNov23, brushingNov24))

                val weekNov23 = getWeekWithDays(nov23.toLocalDate())
                val weekNov24 = getWeekWithDays(nov24.toLocalDate())

                assertEquals(weekNov23.week, weekNov24.week)

                context().forceLocale(sundayAsFirstDayOfWeekLocale) {
                    runBlockingTest {
                        assertNotSame(
                            nov23.toLocalDate().toYearWeek(),
                            nov24.toLocalDate().toYearWeek()
                        )

                        statsProcessor.onBrushingsCreated(listOf(brushingNov23, brushingNov24))
                    }
                }
            }
        }
    }

    /*
    UTILS
     */

    private fun mockCheckupCalculatorResults() {
        val checkupData = mockCheckupData()
        whenever(checkupCalculator.calculateCheckup(any<IBrushing>())).thenReturn(checkupData)
    }

    private fun mockCheckupCalculatorResult(
        brushing: IBrushing,
        surface: Int = DEFAULT_SURFACE,
        zoneCheckupMap: Map<MouthZone16, Float> = DEFAULT_ZONE_MAP,
        correctMovementAverage: Double = 0.0,
        underSpeedAverage: Double = 0.0,
        correctSpeedAverage: Double = 0.0,
        overSpeedAverage: Double = 0.0,
        correctOrientationAverage: Double = 0.0
    ) {
        val checkupData = mockCheckupData(
            surface = surface,
            zoneCheckupMap = zoneCheckupMap,
            correctMovementAverage = correctMovementAverage,
            underSpeedAverage = underSpeedAverage,
            correctSpeedAverage = correctSpeedAverage,
            overSpeedAverage = overSpeedAverage,
            correctOrientationAverage = correctOrientationAverage
        )

        whenever(checkupCalculator.calculateCheckup(brushing)).thenReturn(checkupData)
    }

    private fun verifyDatabaseIsEmpty() {
        assertTrue(sessionStatDao.readAll().isEmpty())
        assertTrue(dayStatDao.readAll().isEmpty())
        assertTrue(monthStatDao.readAll().isEmpty())
        assertTrue(weekStatDao.readAll().isEmpty())
    }

    private fun getMonthWithDays(currentMonth: YearMonth): MonthWithDayStats =
        statsOfflineDao.monthWithDays(DEFAULT_PROFILE_ID, currentMonth)!!

    private fun getWeekWithDays(day: LocalDate): WeekWithDayStats =
        statsOfflineDao.weekWithDays(DEFAULT_PROFILE_ID, day.toYearWeek())!!
}
