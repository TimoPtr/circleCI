/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.test.invokeOnCleared
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class WakeYourBrushViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: WakeYourBrushViewModel

    private val pairingFlowSharedFacade: PairingFlowSharedFacade = mock()
    private val navigator: PairingNavigator = mock()
    private val wakeYourBrushPrerequisitesUseCase: WakeYourBrushPrerequisitesUseCase = mock()
    private val bluetoothUtils: IBluetoothUtils = mock()
    private val blinkFirstScanResultUseCase: BlinkFirstScanResultUseCase = mock()
    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock()
    private val timeoutScheduler = TestScheduler()

    private lateinit var prerequisiteStateSubject: BehaviorSubject<ConnectionPrerequisitesState>

    override fun setup() {
        super.setup()

        viewModel =
            WakeYourBrushViewModel(
                null,
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                navigator = navigator,
                wakeYourBrushPrerequisitesUseCase = wakeYourBrushPrerequisitesUseCase,
                bluetoothUtils = bluetoothUtils,
                blinkFirstScanResultUseCase = blinkFirstScanResultUseCase,
                timeoutScheduler = timeoutScheduler,
                checkConnectionPrerequisitesUseCase = checkConnectionPrerequisitesUseCase
            )

        prerequisiteStateSubject = checkConnectionPrerequisitesUseCase.mockPrerequisitesStream()
    }

    @Test
    fun `SHOW_NOTHING_HAPPENING_AFTER_SECONDS equals to 10L`() {
        assertEquals(10L, SHOW_NOTHING_HAPPENING_AFTER_SECONDS)
    }

    /*
    onCreate
    */
    @Test
    fun `onCreate invokes pairingFlowPrerequisites validateOrNavigate`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(wakeYourBrushPrerequisitesUseCase).validateOrNavigate()
    }

    /*
    onResume
     */

    @Test
    fun `onResume sets isForeground to true`() {
        mockBluetoothObservable(initialValue = false)
        mockImmediateUnpairblinking()

        assertFalse(viewModel.isForeground)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(viewModel.isForeground)
    }

    @Test
    fun `onResume subscribes to checkConnectionPrerequisitesUseCase`() {
        mockBluetoothObservable(initialValue = false)
        mockImmediateUnpairblinking()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(prerequisiteStateSubject.hasObservers())
    }

    @Test
    fun `whenever checkConnectionPrerequisitesUseCase emits a new item different than ConnectionAllowed, invoke validateAndNavigate`() {
        mockBluetoothObservable(initialValue = false)
        mockImmediateUnpairblinking()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        // invoked in onCreate
        verify(wakeYourBrushPrerequisitesUseCase, times(1)).validateOrNavigate()

        ConnectionPrerequisitesState.values()
            .filterNot { it == ConnectionPrerequisitesState.ConnectionAllowed }
            .forEachIndexed { index, newState ->
                prerequisiteStateSubject.onNext(newState)

                verify(wakeYourBrushPrerequisitesUseCase, times(index + 2)).validateOrNavigate()
            }
    }

    @Test
    fun `when bluetooth is on, onResume subscribes to blink use case after unpairing blinking connection`() {
        mockBluetoothObservable(initialValue = true)

        val blinkSubject = mockBlinkUseCase()
        val unpairSubject = mockUnpairblinking()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(unpairSubject.hasObservers())
        assertFalse(blinkSubject.hasObservers())

        unpairSubject.onComplete()

        assertTrue(blinkSubject.hasObservers())
    }

    @Test
    fun `onResume sets showNothingHappening to false`() {
        mockBluetoothObservable(initialValue = false)
        mockImmediateUnpairblinking()

        viewModel = spy(viewModel)

        viewModel.updateViewState { copy(showNothingHappening = true) }

        doReturn(CompletableSubject.create()).whenever(viewModel).showNothingHappeningCompletable()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertFalse(viewModel.getViewState()!!.showNothingHappening)
    }

    @Test
    fun `onResume subscribe to showNothingHappeningCompletable and showNothingHappening to true`() {
        viewModel = spy(viewModel)
        val subject = CompletableSubject.create()

        doReturn(subject).whenever(viewModel).showNothingHappeningCompletable()
        doReturn(Completable.complete().subscribe({}, {})).whenever(viewModel).startScan()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(subject.hasObservers())

        subject.onComplete()

        assertTrue(viewModel.getViewState()!!.showNothingHappening)
    }

    /*
    onPause
     */

    @Test
    fun `onPause stops listening to bluetooth state updates`() {
        val bluetoothSubject = mockBluetoothObservable(initialValue = false)

        mockBlinkUseCase()

        viewModel.actionsObservable.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertFalse(bluetoothSubject.hasObservers())
    }

    @Test
    fun `onPause unsubscribes from blinking use case`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(blinkSubject.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertFalse(blinkSubject.hasObservers())
    }

    @Test
    fun `when bluetooth is on after lifecycle has transitioned to onPause, we never subscribe to blink use case`() {
        mockBluetoothObservable(initialValue = false)

        val blinkSubject = mockBlinkUseCase()

        viewModel.actionsObservable.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertFalse(blinkSubject.hasObservers())
    }

    /*
    onCleared
     */
    @Test
    fun `onCleared unpairs from blinkingConnection in a blocking fashion if it's not null`() {
        /*
        While this subject has observers, it means we didn't invoke parent.onCleared
         */
        val testSubject = PublishSubject.create<Boolean>()
        viewModel.disposeOnCleared {
            testSubject.subscribe()
        }

        val unpairSubject = CompletableSubject.create()
        doAnswer {
            unpairSubject
                .doOnSubscribe {
                    assertTrue(testSubject.hasObservers())

                    unpairSubject.onComplete()
                }
        }.whenever(pairingFlowSharedFacade).unpairBlinkingConnectionCompletable()

        assertTrue(testSubject.hasObservers())

        viewModel.invokeOnCleared()

        verify(pairingFlowSharedFacade).unpairBlinkingConnectionCompletable()

        assertFalse(testSubject.hasObservers())
    }

    @Test
    fun `onCleared invokes super oncleared even if unpair throws exception`() {
        /*
        While this subject has observers, it means we didn't invoke parent.onCleared
         */
        val testSubject = PublishSubject.create<Boolean>()
        viewModel.disposeOnCleared {
            testSubject.subscribe()
        }

        val subject = CompletableSubject.create()
        doAnswer {
            subject
                .doOnSubscribe {
                    assertTrue(testSubject.hasObservers())

                    subject.onError(TestForcedException())
                }
        }.whenever(pairingFlowSharedFacade).unpairBlinkingConnectionCompletable()

        assertTrue(testSubject.hasObservers())

        viewModel.invokeOnCleared()

        assertFalse(testSubject.hasObservers())
    }

    /*
    onNothingHappeningClick
     */
    @Test
    fun `onNothingHappeningClick invokes navigator navigateToIsBrushReady`() {
        viewModel.onNothingHappeningClick()

        verify(navigator).navigateToIsBrushReady()
    }

    /*
    showNothingHappeningCompletable
     */

    @Test
    fun `showNothingHappeningCompletable emits after SHOW_NOTHING_HAPPENING_AFTER_SECONDS when nothing is push`() {
        val testObserver = viewModel.showNothingHappeningCompletable().test()

        timeoutScheduler.advanceTimeBy(SHOW_NOTHING_HAPPENING_AFTER_SECONDS, TimeUnit.SECONDS)

        testObserver.assertComplete()
    }

    @Test
    fun `showNothingHappeningCompletable emits after SHOW_NOTHING_HAPPENING_AFTER_SECONDS after last emitted item`() {
        val testObserver = viewModel.showNothingHappeningCompletable().test()

        timeoutScheduler.advanceTimeBy(SHOW_NOTHING_HAPPENING_AFTER_SECONDS / 2, TimeUnit.SECONDS)

        viewModel.postponeNothingHappeningRelay.accept(true)

        timeoutScheduler.advanceTimeBy(SHOW_NOTHING_HAPPENING_AFTER_SECONDS / 2, TimeUnit.SECONDS)

        testObserver.assertNotComplete()

        timeoutScheduler.advanceTimeBy(SHOW_NOTHING_HAPPENING_AFTER_SECONDS / 2, TimeUnit.SECONDS)

        testObserver.assertComplete()
    }

    /*
    BlinkEven InProgress
     */

    @Test
    fun `when bluetooth is on and blink use case emits InProgress, invoke postponeNothingHappeningRelay accept`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        val testObserver = viewModel.postponeNothingHappeningRelay.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        blinkSubject.onNext(BlinkEvent.InProgress)

        testObserver.assertValue(true)
    }

    /*
    BlinkEven Success
     */

    @Test
    fun `when bluetooth is on and blink use case emits Success, invoke postponeNothingHappeningRelay accept`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        val testObserver = viewModel.postponeNothingHappeningRelay.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        blinkSubject.onNext(BlinkEvent.Success(mock()))

        testObserver.assertValue(true)
    }

    @Test
    fun `when bluetooth is on and blink use case emits Success, invoke setBlinkingConnection before invoking navigateToBrushFound`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val connection = mock<KLTBConnection>()
        blinkSubject.onNext(BlinkEvent.Success(connection))

        inOrder(pairingFlowSharedFacade, navigator) {
            verify(pairingFlowSharedFacade).setBlinkingConnection(connection)
            verify(navigator).navigateToBrushFound()
        }
    }

    /*
    BlinkEven Error
     */

    @Test
    fun `when bluetooth is on and blink use case emits Error, never invoke showError`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        viewModel.actionsObservable.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        blinkSubject.onNext(BlinkEvent.Error(TestForcedException()))

        verify(pairingFlowSharedFacade, never()).showError(any())
    }

    @Test
    fun `when bluetooth is on and blink use case emits Error, invoke postponeNothingHappeningRelay accept`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        viewModel.actionsObservable.test()

        val testObserver = viewModel.postponeNothingHappeningRelay.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        blinkSubject.onNext(BlinkEvent.Error(TestForcedException()))

        testObserver.assertValue(true)
    }

    @Test
    fun `when blink event is TimeoutException, restart scanning after unpairing and sets showNothingHappeningto true`() {
        mockBluetoothObservable(initialValue = true)
        val unpairSubject = mockUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(bluetoothUtils, times(1)).bluetoothStateObservable()

        assertFalse(blinkSubject.hasObservers())
        assertTrue(unpairSubject.hasObservers())

        unpairSubject.onComplete()

        assertTrue(blinkSubject.hasObservers())

        blinkSubject.onNext(BlinkEvent.Timeout)

        verify(bluetoothUtils, times(2)).bluetoothStateObservable()

        assertTrue(viewModel.getViewState()!!.showNothingHappening)
    }

    @Test
    fun `when blink error, never restart scanning and and sets showNothingHappeningto  true`() {
        mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val blinkSubject = mockBlinkUseCase()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(bluetoothUtils, times(1)).bluetoothStateObservable()

        blinkSubject.onNext(BlinkEvent.Error(TestForcedException()))

        verify(bluetoothUtils, times(1)).bluetoothStateObservable()

        assertTrue(viewModel.getViewState()!!.showNothingHappening)
    }

    /*
    Bluetooth switching state in the middle
     */

    @Test
    fun `when bluetooth is switched off in the middle of a connection attempt, we unsubscribe from blink use case`() {
        val bluetoothSubject = mockBluetoothObservable(initialValue = true)
        mockImmediateUnpairblinking()

        val subject = mockBlinkUseCase()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertTrue(subject.hasObservers())

        bluetoothSubject.onNext(false)

        assertFalse(subject.hasObservers())
    }

    @Test
    fun `when bluetooth is off, onResume subscribes to blink use case only after it's on`() {
        val bluetoothSubject = mockBluetoothObservable(initialValue = false)
        mockImmediateUnpairblinking()

        val subject = mockBlinkUseCase()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertFalse(subject.hasObservers())

        bluetoothSubject.onNext(true)

        assertTrue(subject.hasObservers())
    }

    /*
    Utils
    */

    private fun mockBlinkUseCase(): PublishSubject<BlinkEvent> {
        val subject = PublishSubject.create<BlinkEvent>()
        whenever(blinkFirstScanResultUseCase.blinkFirstScanResult())
            .thenReturn(subject)
        return subject
    }

    private fun mockBluetoothObservable(initialValue: Boolean): BehaviorSubject<Boolean> {
        return bluetoothUtils.mockBluetoothObservable(initialValue)
    }

    private fun mockUnpairblinking(): CompletableSubject {
        return pairingFlowSharedFacade.mockUnpairblinking()
    }

    private fun mockImmediateUnpairblinking() =
        pairingFlowSharedFacade.mockImmediateUnpairblinking()
}

internal fun CheckConnectionPrerequisitesUseCase.mockPrerequisitesStream(
    initialValue: ConnectionPrerequisitesState = ConnectionPrerequisitesState.ConnectionAllowed
): BehaviorSubject<ConnectionPrerequisitesState> {
    val subject = BehaviorSubject.createDefault(initialValue)
    whenever(checkOnceAndStream()).thenReturn(subject)

    return subject
}

internal fun CheckConnectionPrerequisitesUseCase.mockPrerequisitesStream(
    subject: Subject<ConnectionPrerequisitesState>
) {
    whenever(checkOnceAndStream()).thenReturn(subject)
}

internal fun PairingFlowSharedFacade.mockUnpairblinking(): CompletableSubject {
    val subject = CompletableSubject.create()

    whenever(unpairBlinkingConnectionCompletable())
        .thenReturn(subject)

    return subject
}

internal fun PairingFlowSharedFacade.mockImmediateUnpairblinking() {
    whenever(unpairBlinkingConnectionCompletable())
        .thenReturn(Completable.complete())
}

internal fun IBluetoothUtils.mockBluetoothObservable(initialValue: Boolean): BehaviorSubject<Boolean> {
    val subject = BehaviorSubject.createDefault(initialValue)

    whenever(isBluetoothEnabled).thenReturn(initialValue)

    whenever(bluetoothStateObservable())
        .thenReturn(
            subject.doOnNext { whenever(isBluetoothEnabled).thenReturn(it) }
        )

    return subject
}
