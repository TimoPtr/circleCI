/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.ongoing

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.common.base.Optional
import com.jraska.livedata.test
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.testbrushing.R
import com.kolibree.android.testbrushing.TestBrushingNavigator
import com.kolibree.android.testbrushing.TestBrushingSharedViewModel
import com.kolibree.android.testbrushing.shared.TestBrushingUseCase
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class OngoingBrushingViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TestBrushingSharedViewModel = mock()
    private val navigator: TestBrushingNavigator = mock()
    private val testBrushingUseCase: TestBrushingUseCase = mock()
    private val brushingCreator: BrushingCreator = mock()
    private val macAddress: String = "00:00:00:00:00:00"
    private val gameInteractor: GameInteractor = mock()
    private val facade: GameToothbrushInteractorFacade = mock()
    private val lostConnectionHandler: LostConnectionHandler = mock()
    private val keepScreenOnController: KeepScreenOnController = mock()
    private val timeScheduler = TestScheduler()

    private lateinit var viewModel: OngoingBrushingViewModel

    override fun setup() {
        super.setup()
        viewModel =
            spy(
                OngoingBrushingViewModel(
                    OngoingBrushingViewState.initial(),
                    sharedViewModel,
                    navigator,
                    testBrushingUseCase,
                    brushingCreator,
                    Optional.of(macAddress),
                    gameInteractor,
                    facade,
                    lostConnectionHandler,
                    keepScreenOnController,
                    timeScheduler
                )
            )
    }

    @Test
    fun `initial StateView should have the animation configured properly`() {
        assertEquals(
            viewModel.getViewState()!!.brushingAnimation, LottieDelayedLoop(
                rawRes = R.raw.animation_brushing,
                isPlaying = true,
                loopStartFrameRes = R.integer.test_brushing_animation_start_frame,
                loopEndFrameRes = R.integer.test_brushing_animation_end_frame
            )
        )
    }

    /*
    * onCreate
    */

    @Test
    fun `onCreate sets lifecycle owner on brushingCreator`() {
        val lifecycleOwner: LifecycleOwner = mock()
        viewModel.lifecycleTester(lifecycleOwner = lifecycleOwner)
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        verify(brushingCreator).setLifecycleOwner(lifecycleOwner)
    }

    /*
    * onConnectionEstablished
    */

    @Test
    fun `onConnectionEstablished saves lost connection state in view state`() {
        assertNull(viewModel.getViewState()!!.lostConnectionState)

        val connection: KLTBConnection = mockConnection()
        viewModel.onConnectionEstablished(connection)

        assertEquals(
            LostConnectionHandler.State.CONNECTION_ACTIVE,
            viewModel.getViewState()!!.lostConnectionState
        )
        verify(viewModel, never()).resumeTestBrushing()
    }

    @Test
    fun `onConnectionEstablished calls resumeTestBrushing when the vibration was on`() {
        assertNull(viewModel.getViewState()!!.lostConnectionState)

        val connection: KLTBConnection = mockConnection()
        whenever(connection.vibrator().isOn).thenReturn(true)
        doNothing().whenever(viewModel).resumeTestBrushing()
        viewModel.onConnectionEstablished(connection)

        verify(viewModel).resumeTestBrushing()
    }

    /*
    * onLostConnectionHandleStateChanged
    */

    @Test
    fun `onLostConnectionHandleStateChanged saves lost connection state in view state`() {
        assertNull(viewModel.getViewState()!!.lostConnectionState)

        val connection: KLTBConnection = mockConnection()
        viewModel.onLostConnectionHandleStateChanged(
            connection,
            LostConnectionHandler.State.CONNECTION_LOST
        )

        assertEquals(
            LostConnectionHandler.State.CONNECTION_LOST,
            viewModel.getViewState()!!.lostConnectionState
        )
    }

    @Test
    fun `onLostConnectionHandleStateChanged calls notifyReconnection when connection is active`() {
        val connection: KLTBConnection = mockConnection()
        viewModel.onLostConnectionHandleStateChanged(
            connection,
            LostConnectionHandler.State.CONNECTION_ACTIVE
        )
        verify(testBrushingUseCase).notifyReconnection()
    }

    @Test
    fun `onLostConnectionHandleStateChanged pauses animation when connection is not active`() {
        val connection: KLTBConnection = mockConnection()
        viewModel.updateViewState {
            copy(brushingAnimation = brushingAnimation.copy(isPlaying = true))
        }

        viewModel.onLostConnectionHandleStateChanged(
            connection,
            LostConnectionHandler.State.CONNECTION_LOST
        )

        assertFalse(viewModel.getViewState()!!.brushingAnimation.isPlaying)

        viewModel.updateViewState {
            copy(brushingAnimation = brushingAnimation.copy(isPlaying = true))
        }

        viewModel.onLostConnectionHandleStateChanged(
            connection,
            LostConnectionHandler.State.CONNECTING
        )

        assertFalse(viewModel.getViewState()!!.brushingAnimation.isPlaying)
    }

    /*
     * resumeTestBrushing
     */

    @Test
    fun `resumeTestBrushing triggers when the pauseScreenVisible value was true`() {
        assertFalse(viewModel.getViewState()!!.pauseScreenVisible)
        viewModel.updateViewState { copy(pauseScreenVisible = true) }

        val actionListener = viewModel.actionsObservable.test()

        viewModel.resumeTestBrushing()

        assertFalse(viewModel.getViewState()!!.pauseScreenVisible)
    }

    @Test
    fun `resumeTestBrushing does not trigger when the pauseScreenVisible value didn't change`() {
        assertFalse(viewModel.getViewState()!!.pauseScreenVisible)

        val actionListener = viewModel.actionsObservable.test()

        viewModel.resumeTestBrushing()

        assertFalse(viewModel.getViewState()!!.pauseScreenVisible)
    }

    /*
     * pauseTestBrushing
     */

    @Test
    fun `pauseTestBrushing triggers ShowPauseView when the pauseScreenVisible value was false`() {
        assertFalse(viewModel.getViewState()!!.pauseScreenVisible)

        viewModel.pauseTestBrushing()

        assertTrue(viewModel.getViewState()!!.pauseScreenVisible)
    }

    @Test
    fun `pauseTestBrushing does not trigger ShowPauseView when the pauseScreenVisible value didn't change`() {
        assertFalse(viewModel.getViewState()!!.pauseScreenVisible)
        viewModel.updateViewState { copy(pauseScreenVisible = true) }

        val actionListener = viewModel.actionsObservable.test()

        viewModel.pauseTestBrushing()

        assertTrue(viewModel.getViewState()!!.pauseScreenVisible)
        actionListener.assertNoValues()
    }

    /*
     * continueTestBrushingSession
     */

    @Test
    fun `continueTestBrushingSession calls resumeGame`() {
        viewModel.continueTestBrushingSession()
        verify(viewModel).resumeGame()
    }

    @Test
    fun `continueTestBrushingSession sends analytics event`() {
        viewModel.continueTestBrushingSession()

        verify(eventTracker).sendEvent(AnalyticsEvent("TestBrushing_NotFinished"))
    }

    /*
     * tryToCreateTestBrushing
     */

    @Test
    fun `tryToCreateTestBrushing terminates the flow if there was no connection we are about`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)
        doReturn(null).whenever(gameInteractor).connection

        viewModel.tryToCreateTestBrushing()

        verify(navigator).terminate()
    }

    @Test
    fun `tryToCreateTestBrushing setups listeners and tries to upload brushing when there is a connection`() {
        val connection: KLTBConnection = mockConnection()
        doReturn(connection).whenever(gameInteractor).connection
        doReturn(Completable.complete()).whenever(viewModel).createAndUploadBrushing(connection)

        viewModel.tryToCreateTestBrushing()

        verify(viewModel).createAndUploadBrushing(connection)
        verify(brushingCreator).addListener(any())
        verify(navigator, never()).terminate()
    }

    /*
     * createAndUploadBrushing
     */

    @Test
    fun `createAndUploadBrushing completes gracefully if createBrushingData ends with success`() {
        val connection: KLTBConnection = mockConnection()
        val data: CreateBrushingData = mock()

        doReturn(Single.just(data)).whenever(testBrushingUseCase).createBrushingData(connection)
        doNothing().whenever(brushingCreator).onBrushingCompleted(false, connection, data)

        val observer = viewModel.createAndUploadBrushing(connection).test()

        observer.assertComplete()
        verify(testBrushingUseCase).createBrushingData(connection)
        verify(viewModel).showProgress(true)
        verify(brushingCreator).onBrushingCompleted(false, connection, data)
        verify(viewModel, never()).finishAndTerminate(connection)
    }

    @Test
    fun `createAndUploadBrushing invokes finishAndTerminate in case of error`() {
        val connection: KLTBConnection = mockConnection()
        val data: CreateBrushingData = mock()

        doReturn(Single.error<CreateBrushingData>(RuntimeException()))
            .whenever(testBrushingUseCase).createBrushingData(connection)
        doReturn(Completable.complete()).whenever(viewModel).finishAndTerminate(connection)

        val observer = viewModel.createAndUploadBrushing(connection).test()

        observer.assertComplete()
        verify(testBrushingUseCase).createBrushingData(connection)
        verify(viewModel).showProgress(true)
        verify(brushingCreator, never()).onBrushingCompleted(false, connection, data)
        verify(viewModel).finishAndTerminate(connection)
    }

    /*
     * finishWithSuccess
     */

    @Test
    fun `finishWithSuccess calls navigator finishWithSuccess if everything goes fine`() {
        val connection: KLTBConnection = mockConnection()
        doReturn(Completable.complete()).whenever(facade).onGameFinished()

        val observer = viewModel.finishWithSuccess(connection).test()

        observer.assertNoErrors()
        observer.assertComplete()
        verify(viewModel).showProgress(true)
        verify(connection.vibrator()).off()
        verify(viewModel).showProgress(false)
        verify(navigator).finishWithSuccess()
        verify(navigator, never()).terminate()
    }

    @Test
    fun `finishWithSuccess calls navigator terminate in case of error`() {
        val connection: KLTBConnection = mockConnection()
        doReturn(Completable.error(RuntimeException())).whenever(facade).onGameFinished()

        val observer = viewModel.finishWithSuccess(connection).test()

        observer.assertError(RuntimeException::class.java)
        verify(viewModel).showProgress(true)
        verify(connection.vibrator()).off()
        verify(viewModel).showProgress(false)
        verify(navigator, never()).finishWithSuccess()
        verify(navigator).terminate()
    }

    /*
     * finishAndTerminate
     */

    @Test
    fun `finishAndTerminate calls navigator terminate if everything goes fine`() {
        val connection: KLTBConnection = mockConnection()
        doReturn(Completable.complete()).whenever(facade).onGameFinished()

        val observer = viewModel.finishAndTerminate(connection).test()

        observer.assertNoErrors()
        observer.assertComplete()
        verify(viewModel).showProgress(true)
        verify(connection.vibrator()).off()
        verify(viewModel).showProgress(false)
        verify(navigator, never()).finishWithSuccess()
        verify(navigator).terminate()
    }

    @Test
    fun `finishAndTerminate calls navigator terminate in case of error`() {
        val connection: KLTBConnection = mockConnection()
        doReturn(Completable.error(RuntimeException())).whenever(facade).onGameFinished()

        val observer = viewModel.finishAndTerminate(connection).test()

        observer.assertError(RuntimeException::class.java)
        verify(viewModel).showProgress(true)
        verify(connection.vibrator()).off()
        verify(viewModel).showProgress(false)
        verify(navigator, never()).finishWithSuccess()
        verify(navigator).terminate()
    }

    /*
     * showTurnOffToothbrushMessage
     */

    @Test
    fun `showTurnOffToothbrushMessage should be launch after onCreate and a message should be shown after 20sec of ongoing brushing`() {
        whenever(lostConnectionHandler.connectionObservable(macAddress))
            .thenReturn(Observable.never())
        whenever(facade.gameLifeCycleObservable()).thenReturn(Observable.never())

        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE,
                pauseScreenVisible = false
            )
        }
        val showMessageTest = viewModel.showTurnOffToothbrushMessage.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        showMessageTest.assertValue(false)

        timeScheduler.advanceTimeBy(MESSAGE_INFO_WAITING_SECS, TimeUnit.SECONDS)

        showMessageTest.assertValue(true)
    }

    @Test
    fun `showTurnOffToothbrushMessage message should not be shown before 20sec`() {
        whenever(lostConnectionHandler.connectionObservable(macAddress))
            .thenReturn(Observable.never())
        whenever(facade.gameLifeCycleObservable()).thenReturn(Observable.never())
        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE,
                pauseScreenVisible = false
            )
        }

        val showMessageTest = viewModel.showTurnOffToothbrushMessage.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        showMessageTest.assertValue(false)

        timeScheduler.advanceTimeBy(MESSAGE_INFO_WAITING_SECS - 1, TimeUnit.SECONDS)

        showMessageTest.assertValue(false)
    }

    @Test
    fun `showTurnOffToothbrushMessage message should not be shown if 20s elapsed during pause`() {
        whenever(lostConnectionHandler.connectionObservable(macAddress))
            .thenReturn(Observable.never())
        whenever(facade.gameLifeCycleObservable()).thenReturn(Observable.never())
        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE,
                pauseScreenVisible = false
            )
        }

        val showMessageTest = viewModel.showTurnOffToothbrushMessage.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.updateViewState { copy(pauseScreenVisible = true) }

        showMessageTest.assertValue(false)

        timeScheduler.advanceTimeBy(MESSAGE_INFO_WAITING_SECS + 10, TimeUnit.SECONDS)

        showMessageTest.assertValue(false)
    }

    @Test
    fun `showTurnOffToothbrushMessage message should not be shown if 20s elapsed without connection`() {
        whenever(lostConnectionHandler.connectionObservable(macAddress))
            .thenReturn(Observable.never())
        whenever(facade.gameLifeCycleObservable()).thenReturn(Observable.never())
        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTION_LOST,
                pauseScreenVisible = false
            )
        }

        val showMessageTest = viewModel.showTurnOffToothbrushMessage.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)
        viewModel.updateViewState { copy(pauseScreenVisible = true) }

        showMessageTest.assertValue(false)

        timeScheduler.advanceTimeBy(MESSAGE_INFO_WAITING_SECS + 10, TimeUnit.SECONDS)

        showMessageTest.assertValue(false)

        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTING
            )
        }
        timeScheduler.advanceTimeBy(MESSAGE_INFO_WAITING_SECS + 10, TimeUnit.SECONDS)

        showMessageTest.assertValue(false)
    }

    private fun mockConnection(): KLTBConnection {
        val connection: KLTBConnection = mock()
        val toothbrush: Toothbrush = mock()
        val vibrator: Vibrator = mock()
        val state: ConnectionState = mock()
        doReturn(KLTBConnectionState.ACTIVE).whenever(state).current
        doReturn(Completable.complete()).whenever(vibrator).off()
        doReturn(macAddress).whenever(toothbrush).mac
        doReturn(toothbrush).whenever(connection).toothbrush()
        doReturn(vibrator).whenever(connection).vibrator()
        doReturn(state).whenever(connection).state()
        return connection
    }

    companion object {
        private const val MESSAGE_INFO_WAITING_SECS = 20L
    }
}
