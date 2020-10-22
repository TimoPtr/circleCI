package com.kolibree.charts.persistence.repo

import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.charts.MockDatabaseProvider
import com.kolibree.charts.persistence.dao.StatDao
import com.kolibree.charts.persistence.models.StatInternal
import com.kolibree.charts.persistence.room.StatsRoomAppDatabase
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.mock
import java.util.ArrayList
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.threeten.bp.OffsetDateTime

/**
 * Created by guillaumeagis on 23/05/2018.
 */

abstract class StatRepositoryTestUtils : BaseInstrumentationTest() {

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    internal lateinit var statDao: StatDao
    private lateinit var statRoomDatabase: StatsRoomAppDatabase
    private val stats = ArrayList<StatInternal>()
    private val mockDataProvider = MockDatabaseProvider()

    protected lateinit var statRepository: StatRepository

    protected val brushingsRepository = mock<BrushingsRepository>()
    protected val checkupCalculator = mock<CheckupCalculator>()

    protected lateinit var brushingsProfile1: List<Brushing>
    protected lateinit var brushingsProfile2: List<Brushing>
    protected lateinit var brushingsToAddProfile1: Brushing

    @Before
    override fun setUp() {
        super.setUp()
        statRoomDatabase = mockDataProvider.providesAppDatabase(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        statDao = statRoomDatabase.statDao()

        val currentTime = TrustedClock.getNowOffsetDateTime()

        brushingsProfile1 = listOf(
            createBrushing(profileIdUser1, 30, currentTime, "processed_data1"),
            createBrushing(profileIdUser1, 40, currentTime.minusDays(1), "processed_data2"),
            createBrushing(profileIdUser1, 50, currentTime.minusDays(2), "processed_data3")
        )

        brushingsProfile2 = listOf(
            createBrushing(profileIdUser2, 60, currentTime.minusDays(3), "processed_data4"),
            createBrushing(profileIdUser2, 70, currentTime.minusDays(4), "processed_data5"),
            createBrushing(profileIdUser2, 80, currentTime.minusDays(5), "processed_data6")
        )

        brushingsToAddProfile1 =
            createBrushing(profileIdUser1, 90, currentTime.minusDays(6), "processed_data7")
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearDB()
        stats.clear()
    }

    /**
     * Clean the content of the DB
     */
    protected fun clearDB() {
        statRoomDatabase.clearAllTables()
    }

    private fun createBrushing(
        profileId: Long,
        duration: Long,
        time: OffsetDateTime,
        processedData: String
    ) = Brushing(
        duration,
        120,
        time,
        100,
        30, processedData,
        profileId,
        1,
        "game1"
    )

    companion object {
        internal const val profileIdWithoutData = 10L
        internal const val profileIdUser1 = 53L
        internal const val profileIdUser2 = 101L
    }
}
