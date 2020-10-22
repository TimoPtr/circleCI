/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.mvi

import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.lifecycle.GameLifecycle
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTING
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_LOST
import com.kolibree.android.test.LifecycleObserverTester
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.android.parcel.Parcelize
import org.junit.Test

class BaseGameViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: StubViewModel
    private lateinit var connection: KLTBConnection
    private lateinit var viewModelLifecycleTester: LifecycleObserverTester
    private val lostConnectionTestProcessor = PublishSubject.create<LostConnectionHandler.State>()

    private val gameInteractor: GameInteractor = mock()

    private val gameToothbrushInteractorFacade: GameToothbrushInteractorFacade = mock()

    private val lostConnectionHandler: LostConnectionHandler = mock()

    private val keepScreenOnController: KeepScreenOnController = mock()

    companion object {

        internal const val MAC_ADDRESS = "00:00:00:00:00:00"
    }

    override fun setup() {
        super.setup()

        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withVibration(true)
            .build()
        whenever(gameInteractor.connection).then { connection }

        whenever(lostConnectionHandler.connectionObservable(MAC_ADDRESS))
            .thenReturn(lostConnectionTestProcessor)

        viewModel = spy(
            StubViewModel(
                StubViewState(),
                Optional.of(MAC_ADDRESS),
                gameInteractor,
                gameToothbrushInteractorFacade,
                lostConnectionHandler,
                keepScreenOnController
            )
        )

        viewModelLifecycleTester = viewModel.lifecycleTester()
    }
    /*
    onCreate
     */

    @Test
    fun `onCreate adds hooks to gameInteractor`() {
        pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(gameInteractor).setLifecycleOwner(viewModelLifecycleTester.lifecycleOwner)
        verify(gameInteractor).addListener(viewModel)
    }

    /*
    onResume
     */

    @Test
    fun `onResume subscribes to lostConnectionHandler connectionObservable`() {
        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(lostConnectionHandler).connectionObservable(eq(MAC_ADDRESS))

        assertTrue(lostConnectionTestProcessor.hasObservers())
    }

    /*
    onPause
     */

    @Test
    fun `onPause disposes lostConnectionHandler connectionObservable`() {
        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(lostConnectionTestProcessor.hasObservers())

        pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertFalse(lostConnectionTestProcessor.hasObservers())
    }

    /*
    onDestroy
     */

    @Test
    fun `onDestroy removeListener from gameInteractor`() {
        pushLifecycleTo(Lifecycle.Event.ON_STOP)
        verify(gameInteractor, never()).removeListener(any())

        pushLifecycleTo(Lifecycle.Event.ON_DESTROY)
        verify(gameInteractor).removeListener(viewModel)
    }

    @Test
    fun `onCleared removes listener from gameInteractor`() {
        pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        verify(gameInteractor, never()).removeListener(any())

        viewModel.invokeOnCleared()
        verify(gameInteractor).removeListener(viewModel)
    }

    @Test
    fun `onConnectionEstablished invokes facade onConnectionEstablished when we care about the connection, even if it's not active`() {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()

        assertWeCareAboutConnection()

        viewModel.onConnectionEstablished()

        verify(gameToothbrushInteractorFacade).onConnectionEstablished(connection)
    }

    @Test
    fun `onConnectionEstablished invokes onConnectionEstablised(connection) when we care about the connection, even if it's not active`() {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()

        assertWeCareAboutConnection()

        viewModel.onConnectionEstablished()

        verify(viewModel).onConnectionEstablished(connection)
    }

    @Test
    fun `onConnectionEstablished skips when connection is not the one we care `() {
        val localConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("SO:ME:OT:TH:ER:TB")
            .withVibration(true)
            .build()

        assertWeDontCareAboutConnection(localConnection)

        viewModel.onConnectionEstablished()

        verify(viewModel, never()).onConnectionEstablished(localConnection)
        verify(gameToothbrushInteractorFacade, never()).onConnectionEstablished(localConnection)
    }

    /*
    onLostConnectionHandleStateChanged
     */

    @Test
    fun `onLostConnectionHandleStateChanged pushes ConnectionHandlerStateChange with connection we care and invokes onLostConnectionHandle running maybeTurnVibratorOffCompletable after each state`() {
        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        doNothing().whenever(viewModel).notifyLostConnectionStateChanged(any(), any())

        LostConnectionHandler.State.values()
            .forEach { state ->
                val testObserver = viewModel.actionsObservable.test()

                val maybeForceVibratorOffSubject = CompletableSubject.create()
                doReturn(maybeForceVibratorOffSubject)
                    .whenever(viewModel)
                    .maybeForceVibratorOffOnConnectionActive(state)

                lostConnectionTestProcessor.onNext(state)

                assertTrue(maybeForceVibratorOffSubject.hasObservers())
                maybeForceVibratorOffSubject.onComplete()

                testObserver.assertValue(ConnectionHandlerStateChanged(state))
                verify(viewModel).notifyLostConnectionStateChanged(connection, state)
            }
    }

    @Test
    fun `onLostConnectionHandleStateChanged skip when current connection is not the one we care`() {
        val localConnection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("SO:ME:OT:TH:ER:TB")
            .withVibration(true)
            .build()
        whenever(gameInteractor.connection).then { localConnection }
        assertWeDontCareAboutConnection(localConnection)

        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        doReturn(Completable.complete())
            .whenever(viewModel)
            .maybeForceVibratorOffOnConnectionActive(any())

        val testObserver = viewModel.actionsObservable.test().assertNoValues()
        lostConnectionTestProcessor.onNext(CONNECTION_ACTIVE)

        testObserver.assertNoValues()
        verify(viewModel, never()).notifyLostConnectionStateChanged(any(), any())
    }

    /*
  maybeForceVibratorOffOnConnectionActive
   */

    @Test
    fun `maybeForceVibratorOffOnConnectionActive doesn't turn off vibrator if lost connection state is not CONNECTION_ACTIVE`() {
        LostConnectionHandler.State.values()
            .filterNot { it == CONNECTION_ACTIVE }
            .forEach { newLostState ->
                viewModel.maybeForceVibratorOffOnConnectionActive(newLostState).test()

                verifyNoMoreInteractions(connection.vibrator())
            }
    }

    @Test
    fun `maybeForceVibratorOffOnConnectionActive doesn't turn off vibrator if connection supports polling, even if it's CONNECTION_ACTIVE and we care about it`() {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withEmitsVibrationAfterConectionActive(true)
            .build()

        assertWeCareAboutConnection()

        viewModel.maybeForceVibratorOffOnConnectionActive(CONNECTION_ACTIVE).test()

        verifyNoMoreInteractions(connection.vibrator())
    }

    @Test
    fun `maybeForceVibratorOffOnConnectionActive turns off vibrator and invokes onVibratorOff if connection does not support polling, it's CONNECTION_ACTIVE and we care about it`() {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withEmitsVibrationAfterConectionActive(false)
            .withSupportVibrationCommands()
            .build()

        assertWeCareAboutConnection()

        doNothing().whenever(viewModel).onVibratorOff(connection)

        viewModel.maybeForceVibratorOffOnConnectionActive(CONNECTION_ACTIVE).test()

        verify(connection.vibrator()).off()

        verify(viewModel).onVibratorOff(connection)
    }

    @Test
    fun `maybeForceVibratorOffOnConnectionActive never invokes onVibratorOff or onVibratorOff if connection does not support polling, it's CONNECTION_ACTIVE but we don't care about it`() {
        val localConnection = KLTBConnectionBuilder.createAndroidLess()
            .withEmitsVibrationAfterConectionActive(false)
            .withSupportVibrationCommands()
            .build()

        assertWeDontCareAboutConnection(localConnection)

        viewModel.maybeForceVibratorOffOnConnectionActive(CONNECTION_ACTIVE).test()

        verifyNoMoreInteractions(connection.vibrator())

        verify(viewModel, never()).onVibratorOff(any())
    }

    /*
    onVibratorOn
     */

    @Test
    fun `onVibratorOn push VibratorStateChanged true action`() {
        val testObserver = viewModel.actionsObservable.test()
        viewModel.onVibratorOn(connection)
        testObserver.assertValue(VibratorStateChanged(true))
    }

    @Test
    fun `onVibratorOff push VibratorStateChanged false action if lostConnectionStae eq ACTIVE`() {
        viewModel.updateViewState { StubViewState(CONNECTION_ACTIVE) }
        val testObserver = viewModel.actionsObservable.test()
        viewModel.onVibratorOff(connection)
        testObserver.assertValue(VibratorStateChanged(false))
    }

    @Test
    fun `onVibratorOff push ConnectionHandlerStateChanged action if lostConnectionStae not eq ACTIVE`() {
        viewModel.updateViewState { StubViewState(CONNECTING) }
        val testObserver = viewModel.actionsObservable.test()
        viewModel.onVibratorOff(connection)
        testObserver.assertValue(ConnectionHandlerStateChanged(CONNECTING))
    }

    @Test
    fun `onVibratorOff doesn't emit any action if lostConnectionState is null`() {
        viewModel.updateViewState { StubViewState(null) }

        val actionListener = viewModel.actionsObservable.test()

        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onVibratorOff(connection)

        actionListener.assertNoValues()
    }

    @Test
    fun `onVibratorOff emits VibratorStateChanged action if lostConnectionState is active`() {
        val actionListener = viewModel.actionsObservable.test()

        pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        viewModel.updateViewState { StubViewState(CONNECTION_ACTIVE) }

        viewModel.onVibratorOff(connection)

        actionListener.assertValues(
            VibratorStateChanged(false)
        )
    }

    @Test
    fun `onVibratorOff emits VibratorStateChanged action if lostConnectionState is lost`() {
        viewModel.updateViewState { StubViewState(CONNECTION_LOST) }

        val actionListener = viewModel.actionsObservable.test()

        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onVibratorOff(connection)

        actionListener.assertValues(
            ConnectionHandlerStateChanged(CONNECTION_LOST)
        )
    }

    @Test
    fun `resumeGame turns the vibration on`() {
        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.resumeGame()

        verify(connection.vibrator()).on()
    }

    @Test
    fun `subscribeGameLifeCycle subscribe gameLifecycle and accept lifecycleState`() {
        val relay = BehaviorRelay.createDefault(GameLifecycle.Idle)
        pushLifecycleTo(Lifecycle.Event.ON_START, gameLifeCycleObservable = relay)

        verify(viewModel).subscribeGameLifeCycle()

        relay.accept(GameLifecycle.Started)
        verify(viewModel).onGameLifecycleEvent(GameLifecycle.Started)
    }

    @Test
    fun `GameLifecycleListener on lifecycleState Paused`() {
        viewModel.onGameLifecycleEvent(GameLifecycle.Paused)
        verify(keepScreenOnController).allowScreenOff()
    }

    @Test
    fun `GameLifecycleListener on lifecycleState Started`() {
        viewModel.onGameLifecycleEvent(GameLifecycle.Started)
        verify(keepScreenOnController).keepScreenOn()
    }

    @Test
    fun `GameLifecycleListener on lifecycleState Resumed`() {
        viewModel.onGameLifecycleEvent(GameLifecycle.Resumed)
        verify(keepScreenOnController).keepScreenOn()
    }

    @Test
    fun `GameLifecycleListener on lifecycleState Restarted`() {
        viewModel.onGameLifecycleEvent(GameLifecycle.Restarted)
        verify(keepScreenOnController).keepScreenOn()
    }

    /*
    Utils
     */

    fun create(
        viewState: BaseGameViewState,
        macAddress: Optional<String> = Optional.absent(),
        gameInteractor: GameInteractor = this.gameInteractor,
        gameToothbrushInteractorFacade: GameToothbrushInteractorFacade = this.gameToothbrushInteractorFacade,
        lostConnectionHandler: LostConnectionHandler = this.lostConnectionHandler,
        keepScreenOnController: KeepScreenOnController = this.keepScreenOnController
    ): StubViewModel =
        StubViewModel(
            viewState,
            macAddress,
            gameInteractor,
            gameToothbrushInteractorFacade,
            lostConnectionHandler,
            keepScreenOnController
        )

    private fun pushLifecycleTo(
        newState: Lifecycle.Event,
        gameLifeCycleObservable: Observable<GameLifecycle> = BehaviorRelay.createDefault(
            GameLifecycle.Idle
        )
    ) {
        mockGameLifeCycleObservable(gameLifeCycleObservable)

        viewModelLifecycleTester.pushLifecycleTo(newState)
    }

    private fun mockGameLifeCycleObservable(
        gameLifeCycleObservable: Observable<GameLifecycle> = BehaviorRelay.createDefault(
            GameLifecycle.Idle
        )
    ) {
        whenever(gameToothbrushInteractorFacade.gameLifeCycleObservable())
            .thenReturn(gameLifeCycleObservable)
    }

    @Parcelize
    class StubViewState(override val lostConnectionState: LostConnectionHandler.State? = mock()) :
        BaseGameViewState

    class StubViewModel(
        viewState: BaseGameViewState,
        macAddress: Optional<String>,
        gameInteractor: GameInteractor,
        gameToothbrushInteractorFacade: GameToothbrushInteractorFacade,
        lostConnectionHandler: LostConnectionHandler,
        keepScreenOnController: KeepScreenOnController
    ) : BaseGameViewModel<BaseGameViewState>(
        viewState, macAddress,
        gameInteractor, gameToothbrushInteractorFacade, lostConnectionHandler,
        keepScreenOnController
    ) {
        override fun onConnectionEstablished(connection: KLTBConnection) {
        }

        override fun onLostConnectionHandleStateChanged(
            connection: KLTBConnection,
            state: LostConnectionHandler.State
        ) {
        }

        fun invokeOnCleared() = onCleared()

        fun testConnectionWeCareAbout(connection: KLTBConnection) =
            connectionWeCareAbout(connection)
    }

    private fun assertWeCareAboutConnection() {
        assertNotNull(viewModel.testConnectionWeCareAbout(connection))
    }

    private fun assertWeDontCareAboutConnection(connection: InternalKLTBConnection) {
        assertNull(viewModel.testConnectionWeCareAbout(connection))
    }
}
