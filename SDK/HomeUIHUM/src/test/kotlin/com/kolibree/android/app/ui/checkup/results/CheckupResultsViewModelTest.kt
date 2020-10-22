/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import androidx.lifecycle.Observer
import com.jraska.livedata.test
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.checkup.base.CheckupActions
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

/** [CheckupResultsViewModel] unit tests */
class CheckupResultsViewModelTest : BaseUnitTest() {

    private val currentProfileProvider = mock<CurrentProfileProvider>()

    private val brushingFacade = mock<BrushingFacade>()

    private val checkupCalculator = mock<CheckupCalculator>()

    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase = mock()

    private lateinit var viewModel: CheckupResultsViewModel

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel =
            CheckupResultsViewModel(
                initialViewState = CheckupResultsViewState.initial(CheckupOrigin.HOME),
                currentProfileProvider = currentProfileProvider,
                brushingFacade = brushingFacade,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase,
                checkupCalculator = checkupCalculator
            )
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    onFinishClick
     */

    @Test
    fun `onFinishClick pushes FinishOk`() {
        setCurrentBrushing()

        val testObserver = viewModel.actionsObservable.test()

        viewModel.onFinishClick()

        testObserver.assertLastValue(CheckupActions.FinishOk)
    }

    @Test
    fun `onFinishClick sends analytics`() {
        setCurrentBrushing()

        viewModel.actionsObservable.test()

        viewModel.onFinishClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Checkup_Collect"))
    }

    @Test
    fun `onFinishClick invokes onUserExpectsPoints with brushing's dateTime`() {
        val expectedTime = TrustedClock.getNowOffsetDateTime()
        setCurrentBrushing(expectedTime)
        viewModel.onFinishClick()

        verify(userExpectsSmilesUseCase).onUserExpectsPoints(expectedTime.toInstant())
    }

    /*
    onBackButtonClick
     */
    @Test
    fun `onBackButtonClick sends analytics`() {
        viewModel.onBackButtonClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Checkup_Quit"))
    }

    /*
    isManualBrushingLiveData
     */

    @Test
    fun `isManualBrushingLiveData emits state's isManualBrushing`() {
        val expectedIsManual = true
        val testObserver = viewModel.isManualBrushingLiveData.test()

        viewModel.updateViewState { copy(isManualBrushing = expectedIsManual) }

        testObserver.assertValue(expectedIsManual)
    }

    /*
    onDeleteConfirmed
     */

    @Test
    fun `onDeleteConfirmed deletes current brushing session`() {
        val expectedBrushing = mock<IBrushing>()
        whenever(brushingFacade.deleteBrushing(expectedBrushing))
            .thenReturn(Completable.complete())
        whenever(brushingFacade.getBrushings(any()))
            .thenReturn(Single.just(listOf(expectedBrushing)))
        whenever(currentProfileProvider.currentProfile())
            .thenReturn(ProfileBuilder.create().build())

        viewModel.currentBrushing.set(expectedBrushing)
        viewModel.onDeleteConfirmed()

        verify(brushingFacade).deleteBrushing(expectedBrushing)
    }

    /*
    onBrushingDeleted
    */

    @Test
    fun `onBrushingDeleted pushes FinishOk`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.onBrushingDeleted(mock())

        testObserver.assertLastValue(CheckupActions.FinishOk)
    }

    @Test
    fun `onBrushingDeleted sends analytics`() {
        viewModel.actionsObservable.test()

        viewModel.onBrushingDeleted(mock())

        verify(eventTracker).sendEvent(AnalyticsEvent("Checkup_Delete"))
    }

    /*
    onLastBrushingSession
     */

    @Test
    fun `onLastBrushingSession computes expected state from last brushing session and sets currentBrushing`() {
        val expectedCoverage = 0.30f
        val expectedDurationSeconds = 60L
        val goalDuration = 120L
        val expectedGame = "co+"
        val expectedDate = TrustedClock.getNowOffsetDateTime()

        val expectedBrushing = mock<IBrushing>()
        whenever(expectedBrushing.duration).thenReturn(expectedDurationSeconds)
        whenever(expectedBrushing.goalDuration).thenReturn(goalDuration.toInt())
        whenever(expectedBrushing.game).thenReturn(expectedGame)
        whenever(expectedBrushing.dateTime).thenReturn(expectedDate)

        val checkupData = mock<CheckupData>()
        val zoneSurfaceMap = mapOf<MouthZone16, Float>(mock())
        whenever(checkupData.coverage).thenReturn(expectedCoverage)
        whenever(checkupData.duration).thenReturn(Duration.ofSeconds(expectedDurationSeconds))
        whenever(checkupData.dateTime).thenReturn(expectedDate)
        whenever(checkupCalculator.calculateCheckup(expectedBrushing))
            .thenReturn(checkupData)
        whenever(checkupData.zoneSurfaceMap).thenReturn(zoneSurfaceMap)

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onLastBrushingSession(expectedBrushing)

        testObserver
            .assertNoErrors()
            .assertNotComplete()
            .assertLastValue(
                CheckupResultsViewState(
                    isManualBrushing = false,
                    coverage = expectedCoverage,
                    durationSeconds = expectedDurationSeconds,
                    game = expectedGame,
                    date = expectedDate,
                    durationPercentage = expectedDurationSeconds / goalDuration.toFloat(),
                    checkupData = zoneSurfaceMap,
                    checkupOrigin = CheckupOrigin.HOME
                )
            )

        assertEquals(expectedBrushing, viewModel.currentBrushing.get())
    }

    @Test
    fun `onLastBrushingSession sets isManual to true and coverage to null when no surface data`() {
        val expectedDurationSeconds = 60L
        val goalDuration = 120L
        val expectedGame = "co+"
        val expectedDate = TrustedClock.getNowOffsetDateTime()

        val expectedBrushing = mock<IBrushing>()
        whenever(expectedBrushing.duration).thenReturn(expectedDurationSeconds)
        whenever(expectedBrushing.goalDuration).thenReturn(goalDuration.toInt())
        whenever(expectedBrushing.game).thenReturn(expectedGame)
        whenever(expectedBrushing.dateTime).thenReturn(expectedDate)

        val checkupData = mock<CheckupData>()
        val zoneSurfaceMap = emptyMap<MouthZone16, Float>()
        whenever(checkupData.coverage).thenReturn(null)
        whenever(checkupData.duration).thenReturn(Duration.ofSeconds(expectedDurationSeconds))
        whenever(checkupData.dateTime).thenReturn(expectedDate)
        whenever(checkupCalculator.calculateCheckup(expectedBrushing))
            .thenReturn(checkupData)
        whenever(checkupData.zoneSurfaceMap).thenReturn(zoneSurfaceMap)
        whenever(checkupData.isManual).thenReturn(true)

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onLastBrushingSession(expectedBrushing)

        testObserver
            .assertNoErrors()
            .assertNotComplete()
            .assertLastValue(
                CheckupResultsViewState(
                    isManualBrushing = true,
                    coverage = null,
                    durationSeconds = expectedDurationSeconds,
                    game = expectedGame,
                    date = expectedDate,
                    durationPercentage = expectedDurationSeconds / goalDuration.toFloat(),
                    checkupData = zoneSurfaceMap,
                    checkupOrigin = CheckupOrigin.HOME
                )
            )

        assertEquals(expectedBrushing, viewModel.currentBrushing.get())
    }

    /*
    currentProfileLastBrushingSessionSingle
     */

    @Test
    fun `currentProfileLastBrushingSessionSingle emits current profile's last brushing session`() {
        val expectedProfileId = 1986L
        val expectedProfile = ProfileBuilder.create().withId(expectedProfileId).build()
        val expectedBrushing = mock<IBrushing>()

        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(expectedProfile))
        whenever(brushingFacade.getLastBrushingSession(expectedProfileId))
            .thenReturn(Single.just(expectedBrushing))

        viewModel.currentProfileLastBrushingSessionSingle()
            .test()
            .assertValue(expectedBrushing)
    }

    @Test
    fun `headline text should be accordingly set if origin is home`() {
        assertEquals(R.string.checkup_your_results, viewModel.title.value)
    }

    @Test
    fun `headline text should be accordingly set if origin is test brushing`() {
        val observer: Observer<Int> = mock()
        val viewModelFromTestBrushing =
            CheckupResultsViewModel(
                initialViewState = CheckupResultsViewState.initial(CheckupOrigin.TEST_BRUSHING),
                currentProfileProvider = currentProfileProvider,
                brushingFacade = brushingFacade,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase,
                checkupCalculator = checkupCalculator
            )

        with(viewModelFromTestBrushing) {
            title.observeForever(observer)
            updateViewState { copy(checkupOrigin = CheckupOrigin.TEST_BRUSHING) }
        }

        ArgumentCaptor.forClass(Int::class.java).run {
            verify(observer).onChanged(capture())
            assertEquals(R.string.checkup_see_how_you_did, value)
        }
    }

    private fun setCurrentBrushing(expectedTime: OffsetDateTime = TrustedClock.getNowOffsetDateTime()) {
        val brushing = mock<IBrushing>()
        whenever(brushing.dateTime).thenReturn(expectedTime)
        viewModel.currentBrushing.set(brushing)
    }
}
