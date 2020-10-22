/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import androidx.lifecycle.Lifecycle
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot.LAST_BRUSHING_SESSION
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxConfiguration
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.offlinebrushings.ExtractionProgress
import com.kolibree.android.offlinebrushings.OfflineBrushingSyncedResult
import com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher
import com.kolibree.android.offlinebrushings.retriever.TimestampedExtractionProgress
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.BrushingBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LastBrushingCardViewModelTest : BaseUnitTest() {

    private val brushingCardDataUseCase: BrushingCardDataUseCase = mock()
    private val offlineExtractionProgressPublisher: OfflineExtractionProgressPublisher = mock()
    private val pulsingDotUseCase: PulsingDotUseCase = mock()

    private val navigator: HumHomeNavigator = mock()

    private val brushingFacade: BrushingFacade = mock()

    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val toolboxViewModel: ToolboxViewModel = mock()

    private lateinit var viewModel: LastBrushingCardViewModel

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel = spy(
            LastBrushingCardViewModel(
                initialViewState = LastBrushingCardViewState.initial(
                    DynamicCardPosition.ONE
                ),
                brushingCardDataUseCase = brushingCardDataUseCase,
                navigator = navigator,
                brushingFacade = brushingFacade,
                currentProfileProvider = currentProfileProvider,
                offlineExtractionProgressPublisher = offlineExtractionProgressPublisher,
                pulsingDotUseCase = pulsingDotUseCase,
                toolboxViewModel = toolboxViewModel
            )
        )
    }

    override fun tearDown() {
        FailEarly.overrideDelegateWith(TestDelegate)
        super.tearDown()
    }

    @Test
    fun `retrieve offline brushing data only when the app is in the foreground`() {
        val offlineBrushingMock = PublishSubject.create<TimestampedExtractionProgress>()

        whenever(brushingCardDataUseCase.load()).thenReturn(Flowable.just(listOf(BrushingCardData.empty())))
        whenever(offlineExtractionProgressPublisher.stream())
            .thenReturn(offlineBrushingMock)

        whenever(pulsingDotUseCase.shouldShowPulsingDot(any())).thenReturn(Flowable.just(false))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        assertFalse(offlineBrushingMock.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        assertTrue(offlineBrushingMock.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        assertFalse(offlineBrushingMock.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        assertTrue(offlineBrushingMock.hasObservers())
    }

    @Test
    fun `display offline brushing sync progress`() {
        val offlineBrushingMock = PublishSubject.create<TimestampedExtractionProgress>()

        whenever(brushingCardDataUseCase.load()).thenReturn(Flowable.just(listOf(BrushingCardData.empty())))
        whenever(offlineExtractionProgressPublisher.stream())
            .thenReturn(offlineBrushingMock)
        whenever(pulsingDotUseCase.shouldShowPulsingDot(any())).thenReturn(Flowable.just(false))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        var extractionProgress = ExtractionProgress.withBrushingProgress(
            brushingsSynced = emptyList(),
            totalBrushings = 0
        )
        offlineBrushingMock.onNext(
            TimestampedExtractionProgress.fromExtractionProgress(extractionProgress)
        )

        var viewState = viewModel.getViewState()!!
        assertEquals(extractionProgress.progress, viewState.offlineBrushingSyncProgress)

        extractionProgress = ExtractionProgress.withBrushingProgress(
            brushingsSynced = listOf(
                OfflineBrushingSyncedResult(1, "mac1", TrustedClock.getNowOffsetDateTime()),
                OfflineBrushingSyncedResult(2, "mac2", TrustedClock.getNowOffsetDateTime()),
                OfflineBrushingSyncedResult(3, "mac3", TrustedClock.getNowOffsetDateTime())
            ),
            totalBrushings = 3
        )
        offlineBrushingMock.onNext(
            TimestampedExtractionProgress.fromExtractionProgress(extractionProgress)
        )

        viewState = viewModel.getViewState()!!
        assertEquals(extractionProgress.progress, viewState.offlineBrushingSyncProgress)
    }

    @Test
    fun `update view state after pulsing dot state changed`() {
        val shouldShowStream = PublishProcessor.create<Boolean>()
        whenever(pulsingDotUseCase.shouldShowPulsingDot(LAST_BRUSHING_SESSION))
            .thenReturn(shouldShowStream)
        whenever(toolboxViewModel.factory()).thenReturn(mock())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        shouldShowStream.offer(true)
        assertTrue(viewModel.getViewState()!!.pulsingDotVisible)

        shouldShowStream.offer(false)
        assertFalse(viewModel.getViewState()!!.pulsingDotVisible)
    }

    @Test
    fun `show proper toolbox and call use case after click on pulsing dot`() {
        val mockConfiguration: ToolboxConfiguration = mock()
        val mockFactory: ToolboxConfiguration.Factory = mock()

        whenever(pulsingDotUseCase.shouldShowPulsingDot(LAST_BRUSHING_SESSION))
            .thenReturn(Flowable.just(true))
        whenever(toolboxViewModel.factory()).thenReturn(mockFactory)
        whenever(mockFactory.testBrushing()).thenReturn(mockConfiguration)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onPulsingDotClick()

        verify(pulsingDotUseCase).onPulsingDotClicked(eq(LAST_BRUSHING_SESSION))
        verify(toolboxViewModel).show(eq(mockConfiguration))
    }

    /*
    onResume
     */

    @Test
    fun `onResume emits a state with shouldRender true`() {
        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onResume(mock())

        testObserver.assertLastValueWithPredicate { it.shouldRender }
    }

    /*
    onPause
     */

    @Test
    fun `onPause emits a state with shouldRender false`() {
        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onPause(mock())

        testObserver.assertLastValueWithPredicate { it.shouldRender.not() }
    }

    /*
    deleteBrushingSessionCompletable
     */

    @Test
    fun `deleteBrushingSessionCompletable invokes deleteBrushing on getBrushingSessions result`() {
        val expectedProfileId = 1986L
        val expectedZonedDatetime = TrustedClock.getNowOffsetDateTime()
        val expectedBrushingSession = BrushingBuilder.create()
            .withDateTime(expectedZonedDatetime)
            .build()
        val currentProfile = ProfileBuilder.create().withId(expectedProfileId).build()

        whenever(currentProfileProvider.currentProfile()).thenReturn(currentProfile)
        whenever(
            brushingFacade.getBrushingSessions(
                expectedZonedDatetime,
                expectedZonedDatetime,
                expectedProfileId
            )
        ).thenReturn(Observable.just(listOf(expectedBrushingSession)))
        whenever(brushingFacade.deleteBrushing(expectedBrushingSession))
            .thenReturn(Completable.complete())

        viewModel.deleteBrushingSessionCompletable(expectedZonedDatetime)
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(currentProfileProvider).currentProfile()
        verify(brushingFacade).getBrushingSessions(
            expectedZonedDatetime,
            expectedZonedDatetime,
            expectedProfileId
        )
        verify(brushingFacade).deleteBrushing(expectedBrushingSession)
    }

    /*
    deleteSelectedBrushingSessionCompletable
     */

    @Test
    fun `deleteSelectedBrushingSessionCompletable does nothing when there is no brushing day`() {
        doReturn(null).whenever(viewModel).selectedItemDatetime()

        viewModel.deleteSelectedBrushingSessionCompletable()
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(viewModel, never()).deleteBrushingSessionCompletable(any())
    }

    @Test
    fun `deleteSelectedBrushingSessionCompletable invokes deleteSelectedBrushingSessionCompletable when there is a brushing day`() {
        val expectedZonedDatetime = TrustedClock.getNowOffsetDateTime()
        doReturn(expectedZonedDatetime).whenever(viewModel).selectedItemDatetime()
        doReturn(Completable.complete()).whenever(viewModel)
            .deleteBrushingSessionCompletable(expectedZonedDatetime)

        viewModel.deleteSelectedBrushingSessionCompletable()
            .test()
            .assertNoErrors()
            .assertComplete()

        verify(viewModel).deleteBrushingSessionCompletable(expectedZonedDatetime)
    }

    /*
    onDeleteBrushingSessionClick
     */

    @Test
    fun `onDeleteBrushingSessionClick invokes showDeleteBrushingSessionConfirmationDialog`() {
        viewModel.onDeleteBrushingSessionClick()

        verify(navigator).showDeleteBrushingSessionConfirmationDialog(any(), any())
    }

    @Test
    fun `onDeleteBrushingSessionClick should send event`() {
        viewModel.onDeleteBrushingSessionClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("LastBrushing_DeleteBrushingSession"))
    }

    /*
    onDeleteBrushingSessionConfirmed
     */

    @Test
    fun `onDeleteBrushingSessionConfirmed sends Analytics event`() {
        viewModel.onDeleteBrushingSessionConfirmed()

        verify(eventTracker).sendEvent(AnalyticsEvent("LastBrushing_DeleteBrushingSession_Ok"))
    }

    /*
    onDeleteBrushingSessionCanceled
     */

    @Test
    fun `onDeleteBrushingSessionCanceled sends Analytics event`() {
        viewModel.onDeleteBrushingSessionCanceled()

        verify(eventTracker).sendEvent(AnalyticsEvent("LastBrushing_DeleteBrushingSession_No"))
    }

    /*
    onExtractionProgress
     */

    @Test
    fun `onExtractionProgress updates viewState`() {
        val extractionProgress =
            ExtractionProgress.withBrushingProgress(
                listOf(
                    OfflineBrushingSyncedResult(
                        0L,
                        "",
                        TrustedClock.getNowOffsetDateTime()
                    )
                ), 2
            )
        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onExtractionProgress(
            TimestampedExtractionProgress.fromExtractionProgress(extractionProgress)
        )

        testObserver.assertLastValueWithPredicate { it.offlineBrushingSyncProgress == extractionProgress.progress }
    }
}
