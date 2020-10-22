/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.testbrushing

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.HomeSessionFlag
import com.kolibree.android.app.ui.brushing.BrushingsForCurrentProfileUseCase
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmileCounterChangedUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Invisible
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.NoInternet
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayLanding
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.TestBrushing
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.synchronization.SynchronizationState.Failure
import com.kolibree.android.synchronization.SynchronizationState.Ongoing
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import com.kolibree.android.test.lifecycleTester
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestBrushingPriorityDisplayViewModelTest : BaseUnitTest() {

    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()

    private val startNonUnityGameUseCase: StartNonUnityGameUseCase = mock()
    private val brushingsForCurrentProfileUseCase: BrushingsForCurrentProfileUseCase = mock()
    private val sessionFlags: SessionFlags = mock()
    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority> = mock()
    private val smileCounterChangedUseCase: SmileCounterChangedUseCase = mock()
    private val synchronizationStateUseCase: SynchronizationStateUseCase = mock()

    private lateinit var testBrushingPriorityDisplayViewModel: TestBrushingPriorityDisplayViewModel

    private val testScheduler = TestScheduler()

    override fun setup() {
        super.setup()

        mockDefaultStreams()

        testBrushingPriorityDisplayViewModel = TestBrushingPriorityDisplayViewModel(
            toothbrushConnectionStateViewModel,
            brushingsForCurrentProfileUseCase,
            startNonUnityGameUseCase,
            sessionFlags,
            priorityItemUseCase,
            smileCounterChangedUseCase,
            synchronizationStateUseCase,
            testScheduler
        )
    }

    private fun mockDefaultStreams() {
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.never())
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(Flowable.never())
        whenever(smileCounterChangedUseCase.counterStateObservable).thenReturn(Observable.never())
        whenever(synchronizationStateUseCase.onceAndStream).thenReturn(Observable.never())
    }

    @Test
    fun `Test Brushing should be launch when the queue emits TestBrushing item`() {
        val subject = CompletableSubject.create()
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(subject)

        testBrushingDisplayableScenario()

        verify(sessionFlags).setSessionFlag(
            HomeSessionFlag.SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN,
            true
        )
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `Test Brushing should be finished and consume the item when PlayIncrease occurs`() {
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(Completable.complete())
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(smileCounterChangedUseCase.counterStateObservable).thenReturn(
            Observable.just(PlayIncrease(1, 2))
        )

        testBrushingDisplayableScenario()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(priorityItemUseCase).markAsDisplayed(TestBrushing)
    }

    @Test
    fun `Test Brushing should be finished and consume the item when a Smiles Failure occurs`() {
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(Completable.complete())
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(synchronizationStateUseCase.onceAndStream).thenReturn(Observable.just(Failure()))

        testBrushingDisplayableScenario()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(priorityItemUseCase).markAsDisplayed(TestBrushing)
    }

    @Test
    fun `Test Brushing should be finished and consume the item when Error occurs`() {
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(Completable.complete())
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(smileCounterChangedUseCase.counterStateObservable).thenReturn(
            Observable.just(Error)
        )

        testBrushingDisplayableScenario()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(priorityItemUseCase).markAsDisplayed(TestBrushing)
    }

    @Test
    fun `Test Brushing should be finished and consume the item when NoInternet occurs`() {
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(Completable.complete())
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(smileCounterChangedUseCase.counterStateObservable).thenReturn(
            Observable.just(NoInternet)
        )

        testBrushingDisplayableScenario()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(priorityItemUseCase).markAsDisplayed(TestBrushing)
    }

    @Test
    fun `Test Brushing should not consume the item if smile completion is not wanted`() {
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(smileCounterChangedUseCase.counterStateObservable).thenReturn(
            Observable.just(PlayLanding(2), Pending, Invisible)
        )

        testBrushingDisplayableScenario()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(priorityItemUseCase, never()).markAsDisplayed(TestBrushing)
    }

    @Test
    fun `Test Brushing should not consume the item if the synchronization is not a failure`() {
        whenever(priorityItemUseCase.submitAndWaitFor(TestBrushing)).thenReturn(Completable.complete())
        whenever(synchronizationStateUseCase.onceAndStream).thenReturn(Observable.just(Ongoing()))

        testBrushingDisplayableScenario()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(priorityItemUseCase, never()).markAsDisplayed(TestBrushing)
    }

    @Test
    fun `onCreate should submit and wait for TestBrushing if the connection is active and user hasn't brushed yet`() {
        val subject = CompletableSubject.create()
        whenever(startNonUnityGameUseCase.start(ActivityGame.TestBrushing)).thenReturn(subject)

        testBrushingDisplayableScenario()

        assertFalse(subject.hasObservers())

        verify(priorityItemUseCase).submitAndWaitFor(TestBrushing)
    }

    @Test
    fun `onCreate should NOT submit and wait for TestBrushing if user suppressed the notification`() {
        whenever(sessionFlags.readSessionFlag(HomeSessionFlag.SUPPRESS_TEST_BRUSHING_REMINDER))
            .thenReturn(true)

        testBrushingDisplayableScenario()

        verify(priorityItemUseCase, never()).submitAndWaitFor(TestBrushing)
    }

    @Test
    fun `onCreate should NOT submit and wait for TestBrushing if the connection is inactive even if user hasn't brushed yet`() {
        val notConnectedTB = SingleToothbrushConnecting("wdc")
        val brushingNumber = 0L

        whenever(brushingsForCurrentProfileUseCase.getBrushingCount(ActivityGame.TestBrushing))
            .thenReturn(Flowable.just(brushingNumber))
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(notConnectedTB, 123))
        )

        testBrushingPriorityDisplayViewModel.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(priorityItemUseCase, never()).submitAndWaitFor(TestBrushing)
    }

    @Test
    fun `onCreate should NOT submit and wait for TestBrushing if the connection is active and user has brushed`() {
        whenever(brushingsForCurrentProfileUseCase.getBrushingCount(ActivityGame.TestBrushing))
            .thenReturn(Flowable.just(10L))

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(connectedToothBrushState)
        )

        testBrushingPriorityDisplayViewModel.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(priorityItemUseCase, never()).submitAndWaitFor(TestBrushing)
    }

    @Test
    fun `onCreate should submit and wait for TestBrushing only one if two connection state are sent`() {
        val brushingNumber = 0L

        whenever(brushingsForCurrentProfileUseCase.getBrushingCount(ActivityGame.TestBrushing))
            .thenReturn(Flowable.just(brushingNumber))
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable)
            .thenReturn(
                Flowable.just(
                    connectedToothBrushState,
                    disconnectedToothBrushState,
                    connectedToothBrushState
                )
            )

        testBrushingPriorityDisplayViewModel.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(priorityItemUseCase).submitAndWaitFor(TestBrushing)
    }

    private fun testBrushingDisplayableScenario() {
        whenever(brushingsForCurrentProfileUseCase.getBrushingCount(ActivityGame.TestBrushing))
            .thenReturn(Flowable.just(0L))

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(connectedToothBrushState)
        )

        testBrushingPriorityDisplayViewModel.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)
    }

    companion object {
        private val connectedToothBrushState =
            ToothbrushConnectionStateViewState(SingleToothbrushConnected("wdc"), 123)

        private val disconnectedToothBrushState =
            ToothbrushConnectionStateViewState(SingleToothbrushDisconnected("wdc"), 123)
    }
}
