package com.kolibree.charts.persistence.repo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.charts.models.Stat
import com.kolibree.charts.persistence.models.StatInternal
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZonedDateTime

/**
 * Created by guillaumeagis on 23/05/2018.
 */

@RunWith(AndroidJUnit4::class)
class StatRepositoryTest : StatRepositoryTestUtils() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun lastWeek() = TrustedClock.getNowZonedDateTime().minusDays(7)

    @Test
    fun verifyIfStatIsCorrectlyStoredWithoutBrushingData() {
        initializeStatRepository()
        testSubscriberForStatSince(lastWeek(), profileIdUser1, emptyList())
    }

    @Test
    fun verifyIfStatIsCorrectlyStoredWithBrushingData() {
        initializeStatRepository(brushingsProfile1)
        testSubscriberForStatSince(lastWeek(), profileIdUser1, convertIntoStats(brushingsProfile1))

        initializeStatRepository(brushingsProfile2)
        testSubscriberForStatSince(lastWeek(), profileIdUser2, convertIntoStats(brushingsProfile2))

        val newList = brushingsProfile1 + brushingsToAddProfile1
        val expected = convertIntoStats(newList)
        initializeStatRepository(newList)
        testSubscriberForStatSince(lastWeek(), profileIdUser1, expected)

        initializeStatRepository()
        testSubscriberForStatSince(lastWeek(), profileIdUser1, expected)
    }

    @Test
    fun verifyCanDeleteAllDataForUsersWithNewData() {

        initializeStatRepository(brushingsProfile1)
        testSubscriberForStatSince(lastWeek(), profileIdUser1, convertIntoStats(brushingsProfile1))
        initializeStatRepository(listOf(brushingsToAddProfile1))
        testSubscriberForStatSince(
            lastWeek(),
            profileIdUser1,
            convertIntoStats(listOf(brushingsToAddProfile1))
        )
        initializeStatRepository(listOf(brushingsToAddProfile1))
        testSubscriberForStatSince(lastWeek(), profileIdUser2, emptyList())
    }

    @Test
    fun verifyTruncateRemovesAllStats() {
        initializeStatRepository()

        statDao.insertAll(convertIntoStatsInternal(brushingsProfile1))
        testSubscriberAllStatInternalsForProfile(lastWeek(), profileIdUser1, convertIntoStatsInternal(brushingsProfile1))
        testSubscriberForStatSince(lastWeek(), profileIdUser1, convertIntoStats(brushingsProfile1))

        val truncateObserver = statRepository.truncate().test()
        truncateObserver.assertNoErrors()
        truncateObserver.assertComplete()

        testSubscriberAllStatInternalsForProfile(lastWeek(), profileIdUser1, emptyList())
        testSubscriberForStatSince(lastWeek(), profileIdUser1, emptyList())
    }

    @Test
    fun verifyDataForUsersWithDataGivenADateInTheFuture() {
        initializeStatRepository()
        testSubscriberForStatSince(TrustedClock.getNowZonedDateTime().plusDays(7), profileIdUser1, emptyList())
    }

    /**
     * Required to wait some ms in order to let the data to be stored , if needed
     */
    private fun testSubscriberForStatSince(
        startTime: ZonedDateTime,
        profileId: Long,
        expected: List<Stat>
    ) {
        whenever(checkupCalculator.calculateCheckup(any(), any(), any())).thenReturn(mock())

        val subscriber = statRepository.getStatsSince(startTime, profileId).test()

        subscriber.assertValue(expected)
    }

    private fun testSubscriberAllStatInternalsForProfile(
        startTime: ZonedDateTime,
        profileId: Long,
        expected: List<StatInternal>
    ) {
        val observer = statDao.readStatsSince(profileId, startTime.toInstant().toEpochMilli()).test()
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValue(expected)
    }

    private fun convertIntoStats(list: List<Brushing>): List<Stat> {
        return list.map {
            Stat.fromStatInternal(
                StatInternal.fromBrushing(it, TrustedClock.utcClock),
                mock()
            )
        }
    }

    private fun convertIntoStatsInternal(list: List<Brushing>): List<StatInternal> {
        var id = 1L
        return list.map {
            StatInternal.fromBrushing(it, TrustedClock.utcClock)
                .also { stat -> stat.id = id++ }
        }
    }

    private fun initializeStatRepository(associatedBrushingList: List<Brushing> = emptyList()) {
        whenever(brushingsRepository.getNonDeletedBrushings()).then {
            Flowable.just(
                associatedBrushingList
            )
        }
        statRepository =
            StatRepositoryImpl(statDao, TrustedClock.utcClock, brushingsRepository, checkupCalculator)
    }
}
