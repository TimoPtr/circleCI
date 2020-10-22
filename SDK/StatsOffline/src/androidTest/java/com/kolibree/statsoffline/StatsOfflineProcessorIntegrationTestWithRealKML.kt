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
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.kml.Kml
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.integrityseal.IntegritySealTest.Companion.createIntegritySeal
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.persistence.BrushingSessionStatDao
import com.kolibree.statsoffline.persistence.DayAggregatedStatsDao
import com.kolibree.statsoffline.persistence.MonthAggregatedStatsDao
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase
import com.kolibree.statsoffline.persistence.StatsOfflineRoomModule
import com.kolibree.statsoffline.persistence.WeekAggregatedStatsDao
import com.kolibree.statsoffline.test.DEFAULT_DURATION
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.toYearMonth
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.YearMonth

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class StatsOfflineProcessorIntegrationTestWithRealKML : BaseInstrumentationTest() {
    init {
        Kml.init()
    }

    private lateinit var sessionStatDao: BrushingSessionStatDao
    private lateinit var monthStatDao: MonthAggregatedStatsDao
    private lateinit var weekStatDao: WeekAggregatedStatsDao
    private lateinit var dayStatDao: DayAggregatedStatsDao
    private lateinit var statsOfflineDatabase: StatsOfflineRoomAppDatabase

    private lateinit var statsOfflineDao: StatsOfflineDao

    @Inject
    lateinit var checkupCalculator: CheckupCalculator

    private lateinit var featureToggle: StatsOfflineFeatureToggle

    private lateinit var statsProcessorStatsOffline: StatsOfflineLocalBrushingsProcessorImpl

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        initRoom()

        statsOfflineDao = StatsOfflineRoomModule.providesStatsOfflineDao(statsOfflineDatabase)

        setupFeatureToggle()

        DaggerStatsOfflineIntegrationTestComponent.factory().create(context()).inject(this)

        statsProcessorStatsOffline = StatsOfflineLocalBrushingsProcessorImpl(
            statsOfflineDao,
            checkupCalculator,
            featureToggle,
            createIntegritySeal(context(), statsOfflineDao)
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
    fun emptyDB_insertManualBrushing() = runBlockingTest {

        /*
        {
            "id": 77478,
            "duration": 13,
            "goal_duration": 120,
            "datetime": "2019-11-02T12:08:29+0000",
            "profile": 435,
            "coins": 0,
            "quality": 0,
            "points": 6,
            "game": "ti",
            "associated_date": "2019-11-02",
            "initial_coverage": null,
            "processed_data": null,
            "account_id": 364
        }
         */

        val manualBrushing = createIBrushing(processedData = null)

        statsProcessorStatsOffline.onBrushingCreated(manualBrushing)

        val insertedMonth = getMonthWithDays(manualBrushing.dateTime.toYearMonth())

        insertedMonth.dayStats.getValue(manualBrushing.dateTime.toLocalDate()).brushingSessions.single()
    }

    private fun getMonthWithDays(currentMonth: YearMonth): MonthWithDayStats =
        statsOfflineDao.monthWithDays(DEFAULT_PROFILE_ID, currentMonth)!!
}

@ExperimentalCoroutinesApi
@Component(modules = [ProcessedBrushingsModule::class])
private interface StatsOfflineIntegrationTestComponent {
    fun inject(test: StatsOfflineProcessorIntegrationTestWithRealKML)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): StatsOfflineIntegrationTestComponent
    }
}

internal fun createIBrushing(
    creationDate: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
    processedData: String?
): IBrushing {
    return object : IBrushing {
        override val duration: Long = DEFAULT_DURATION
        override val goalDuration: Int = 120
        override val dateTime: OffsetDateTime = creationDate
        override val processedData: String? = processedData
        override val game: String? = "co"
        override val toothbrushMac: String? = null
        override val kolibreeId: Long? = null
        override val profileId: Long = DEFAULT_PROFILE_ID
    }
}
