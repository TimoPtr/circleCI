/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import com.jraska.livedata.test
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.checkup.base.CheckupActions
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.threeten.bp.OffsetDateTime

/** [DayCheckupViewModel] unit tests */
class DayCheckupViewModelTest : BaseUnitTest() {

    private val currentProfileProvider = mock<CurrentProfileProvider>()

    private val brushingFacade = mock<BrushingFacade>()

    private val checkupCalculator = mock<CheckupCalculator>()

    private lateinit var viewModel: DayCheckupViewModel

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel =
            DayCheckupViewModel(
                initialViewState = DayCheckupViewState.initial(),
                currentProfileProvider = currentProfileProvider,
                brushingFacade = brushingFacade,
                checkupCalculator = checkupCalculator,
                forDate = TrustedClock.getNowOffsetDateTime()
            )
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    onStart
     */

    @Test
    fun `onStart subscribes to brushing observable`() {
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(ProfileBuilder.create().build()))
        whenever(brushingFacade.getBrushingSessions(any(), any(), any()))
            .thenReturn(Observable.never())

        viewModel.onStart(mock())

        verify(currentProfileProvider).currentProfileSingle()
        verify(brushingFacade).getBrushingSessions(any(), any(), any())
    }

    /*
    onPageSelected
     */

    @Test
    fun `onPageSelected sets currentPosition`() {
        viewModel.sessionCache.get().apply {
            add(createDayCheckupData())
            add(createDayCheckupData())
        }

        val expectedCurrentPosition = 1
        assertNotEquals(expectedCurrentPosition, viewModel.currentPosition)

        viewModel.onPageSelected(expectedCurrentPosition)

        assertEquals(expectedCurrentPosition, viewModel.currentPosition)
    }

    @Test
    fun `onPageSelected emits current position's data`() {
        val expectedCoverage = 34f
        viewModel.sessionCache.get().apply {
            add(createDayCheckupData(1f))
            add(createDayCheckupData(expectedCoverage))
        }

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onPageSelected(1)

        testObserver.assertLastValueWithPredicate { it.coverage == expectedCoverage }
    }

    /*
    onBrushingDeleted
     */

    @Test
    fun `onBrushingDeleted deletes corresponding entry in cache`() {
        val brushingDate1 = TrustedClock.getNowOffsetDateTime().minusNanos(1)
        val brushingDate2 = brushingDate1.minusNanos(1)
        val brushing1 = mockIBrushing(brushingDate1)
        val brushing2 = mockIBrushing(brushingDate2)

        viewModel.sessionCache.get().apply {
            add(createDayCheckupData(brushing = brushing1))
            add(createDayCheckupData(brushing = brushing2))
        }

        viewModel.onBrushingDeleted(brushing1)

        val cache = viewModel.sessionCache.get()
        assertEquals(1, cache.size)
        assertEquals(brushingDate2, cache[0].iBrushing.dateTime)
    }

    @Test
    fun `onBrushingDeleted emits FinishOk action when all the sessions have been deleted`() {
        val brushingDate = TrustedClock.getNowOffsetDateTime()
        val brushing = mockIBrushing(brushingDate)

        viewModel.sessionCache.get().add(createDayCheckupData(brushing = brushing))

        val testObserver = viewModel.actionsObservable.test()
        viewModel.onBrushingDeleted(brushing)

        testObserver.assertLastValue(CheckupActions.FinishOk)
    }

    @Test
    fun `onBrushingDeleted emits data at current position`() {
        val brushingDate1 = TrustedClock.getNowOffsetDateTime().minusNanos(1)
        val brushingDate2 = brushingDate1.minusNanos(1)
        val brushing1 = mockIBrushing(brushingDate1)
        val brushing2 = mockIBrushing(brushingDate2)

        viewModel.sessionCache.get().apply {
            add(createDayCheckupData(brushing = brushing1))
            add(createDayCheckupData(brushing = brushing2))
        }

        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onBrushingDeleted(brushing1)

        testObserver.assertLastValueWithPredicate { it.date == brushingDate2 }
    }

    /*
    currentBrushingSession
     */

    @Test
    fun `currentBrushingSession finds and returns expected object at expected position`() {
        val brushing1 = mockIBrushing()
        val brushing2 = mockIBrushing()

        viewModel.sessionCache.get().apply {
            add(createDayCheckupData(brushing = brushing1))
            add(createDayCheckupData(brushing = brushing2))
        }

        viewModel.currentPosition = 1
        val item = viewModel.currentBrushingSession()

        assertEquals(brushing2, item)
    }

    /*
    checkupDataListLiveData
     */

    @Test
    fun `checkupDataListLiveData emits state's checkup data list`() {
        val expectedList = listOf(mapOf(MouthZone16.LoIncExt to 19f))

        val testObserver = viewModel.checkupDataListLiveData.test()
        viewModel.updateViewState { copy(checkupData = expectedList) }

        testObserver.assertValue(expectedList)
    }

    @Test
    fun `checkupDataListLiveData never emits the same list`() {
        val expectedList = listOf(mapOf(MouthZone16.LoIncExt to 19f))

        val testObserver = viewModel.checkupDataListLiveData.test()
        viewModel.updateViewState { copy(checkupData = expectedList) }
        viewModel.updateViewState { copy(checkupData = expectedList) }
        viewModel.updateViewState { copy(checkupData = expectedList) }

        testObserver.assertValueHistory(listOf(), expectedList)
    }

    /*
    pagerIndicatorVisibleLiveData
     */

    @Test
    fun `pagerIndicatorVisibleLiveData emits true when checkup data list has more than 1 item`() {
        val testObserver = viewModel.pagerIndicatorVisibleLiveData.test()

        viewModel.updateViewState { copy(checkupData = listOf(mapOf(), mapOf())) }

        testObserver.assertValue(true)
    }

    @Test
    fun `pagerIndicatorVisibleLiveData emits false when checkup data list contains 1 item`() {
        val testObserver = viewModel.pagerIndicatorVisibleLiveData.test()

        viewModel.updateViewState { copy(checkupData = listOf(mapOf())) }

        testObserver.assertValue(false)
    }

    /*
    Utils
     */

    private fun createDayCheckupData(
        coverage: Float = 0f,
        brushing: IBrushing = mockIBrushing()
    ) = DayCheckupData(
        coverage = coverage,
        checkupData = mapOf(),
        duration = 0L,
        iBrushing = brushing,
        durationPercentage = 0f
    )

    private fun mockIBrushing(
        date: OffsetDateTime = TrustedClock.getNowOffsetDateTime()
    ) = mock<IBrushing>().apply {
        whenever(dateTime).thenReturn(date)
    }
}
