/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.brush_found.mockBlinkingConnection
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListAnalytics.noBrushFoundClose
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListAnalytics.noBrushFoundGetIt
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.FINISH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.NO_BLINKING_CONNECTION
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.SIGN_UP
import com.kolibree.android.app.ui.pairing.usecases.NextNavigationActionUseCase
import com.kolibree.android.app.ui.pairing.wake_your_brush.mockPrerequisitesStream
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.BluetoothDisabled
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.ConnectionAllowed
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.fakeScanResult
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ScanToothbrushListViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ScanToothbrushListViewModel

    private val sharedFacade: PairingFlowSharedFacade = mock()

    private val navigator: PairingNavigator = mock()

    private val autoExpireScanner: AutoExpireScanner = mock()

    private val scanListBlinkConnectionUseCase: ScanListBlinkConnectionUseCase = mock()

    private val nextNavigationActionUseCase: NextNavigationActionUseCase = mock()

    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock()

    private val scanToothbrushListPrerequisitesUseCase: ScanToothbrushListPrerequisitesUseCase =
        mock()

    private val scanListConfirmResultUseCase: ScanListConfirmResultUseCase = mock()

    private val timeoutScheduler = TestScheduler()

    private val prerequisiteStateSubject = PublishSubject.create<ConnectionPrerequisitesState>()

    override fun setup() {
        super.setup()

        viewModel = ScanToothbrushListViewModel(
            initialViewState = null,
            pairingFlowSharedFacade = sharedFacade,
            navigator = navigator,
            timeoutScheduler = timeoutScheduler,
            scanListBlinkConnectionUseCase = scanListBlinkConnectionUseCase,
            autoExpireScanner = autoExpireScanner,
            nextNavigationActionUseCase = nextNavigationActionUseCase,
            checkConnectionPrerequisitesUseCase = checkConnectionPrerequisitesUseCase,
            scanListConfirmResultUseCase = scanListConfirmResultUseCase,
            scanToothbrushListPrerequisitesUseCase = scanToothbrushListPrerequisitesUseCase
        )

        checkConnectionPrerequisitesUseCase.mockPrerequisitesStream(prerequisiteStateSubject)
    }

    /*
    onCreate
    */
    @Test
    fun `onCreate invokes pairingFlowPrerequisites validateOrNavigate`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(scanToothbrushListPrerequisitesUseCase).validateOrNavigate()
    }

    /*
    items
     */
    @Test
    fun `items returns viewState items`() {
        val expectedItems = listOf<ScanToothbrushItemBindingModel>(mock(), mock())

        val testObserver = viewModel.items.test()

        viewModel.updateViewState {
            copy(items = expectedItems)
        }

        testObserver.assertValue(expectedItems)
    }

    /*
    showNoBrushFound
     */
    @Test
    fun `showNoBrushFound returns viewState showNoBrushFound`() {
        val testObserver = viewModel.showNoBrushFound.test()

        viewModel.updateViewState {
            copy(showNoBrushFound = true)
        }

        testObserver.assertValue(true)
    }

    /*
    getItClick
     */
    @Test
    fun `getItClick sends noBrushFoundGetIt analytics and viewState showNoBrushFound to false`() {
        viewModel.updateViewState {
            copy(showNoBrushFound = true)
        }

        viewModel.getItClick()

        verify(eventTracker).sendEvent(noBrushFoundGetIt())
        assertFalse(viewModel.getViewState()!!.showNoBrushFound)
    }

    /*
    closeClick
     */
    @Test
    fun `closeClick sends noBrushFoundClose analytics and viewState showNoBrushFound to false`() {
        viewModel.updateViewState {
            copy(showNoBrushFound = true)
        }

        viewModel.closeClick()

        verify(eventTracker).sendEvent(noBrushFoundClose())
        assertFalse(viewModel.getViewState()!!.showNoBrushFound)
    }

    /*
    onStop
    */
    @Test
    fun `onStop invokes hideError`() {
        pushToOnResume()

        verify(sharedFacade, never()).hideError()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_STOP)

        verify(sharedFacade).hideError()
    }

    /*
    onBlinkClick
    */
    @Test
    fun `onBlinkClick sends Blink event`() {
        val item = scanToothbrushItem()
        mockBlinkScanResult(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onBlinkClick(item)

        verify(eventTracker).sendEvent(ScanToothbrushListAnalytics.blink())
    }

    @Test
    fun `onBlinkClick hides blink progress of every row as soon as we subscribe to blink`() {
        val scanResultToBeClicked = scanToothbrushItem(isBlinkProgressVisible = false)
        val otherItem = scanToothbrushItem(macAndName = "other", isBlinkProgressVisible = true)
        val itemBlinkSubject = mockBlinkScanResult(scanResultToBeClicked.toothbrushScanResult)

        val viewState =
            ScanToothbrushListViewState(items = listOf(scanResultToBeClicked, otherItem))

        viewModel.updateViewState { viewState }
        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(viewState)

        pushToOnResume()

        viewModel.onBlinkClick(scanResultToBeClicked)

        assertTrue(itemBlinkSubject.hasObservers())

        val expectedViewState =
            viewState.copy(items = viewState.items.map {
                it.copy(isBlinkProgressVisible = false)
            }
            )

        viewStateObserver.assertValues(viewState, expectedViewState)
    }

    @Test
    fun `onBlinkClick sets isBlinkInProgress=true and rowClickable=false as soon as blink emits InProgress`() {
        val scanResultToBeClicked = scanToothbrushItem(isBlinkProgressVisible = false)
        val otherItem = scanToothbrushItem(macAndName = "other", isBlinkProgressVisible = true)
        val itemBlinkSubject = mockBlinkScanResult(scanResultToBeClicked.toothbrushScanResult)

        val viewState =
            ScanToothbrushListViewState(items = listOf(scanResultToBeClicked, otherItem))

        viewModel.updateViewState { viewState }
        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(viewState)

        pushToOnResume()

        viewModel.onBlinkClick(scanResultToBeClicked)

        assertTrue(itemBlinkSubject.hasObservers())

        val expectedViewState =
            viewState.copy(
                items = listOf(
                    scanResultToBeClicked.copy(
                        isRowClickable = false,
                        isBlinkProgressVisible = true
                    ),
                    otherItem.copy(isRowClickable = false, isBlinkProgressVisible = false)
                )
            )

        itemBlinkSubject.onNext(BlinkEvent.InProgress)

        viewStateObserver.assertValueCount(3)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `onBlinkClick sets isBlinkInProgress=false and rowClickable=true when blink emits Success`() {
        val scanResultToBeClicked = scanToothbrushItem(isBlinkProgressVisible = false)
        val otherItem = scanToothbrushItem(macAndName = "other", isBlinkProgressVisible = true)
        val itemBlinkSubject = mockBlinkScanResult(scanResultToBeClicked.toothbrushScanResult)

        val viewState =
            ScanToothbrushListViewState(items = listOf(scanResultToBeClicked, otherItem))

        viewModel.updateViewState { viewState }
        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(viewState)

        pushToOnResume()

        viewModel.onBlinkClick(scanResultToBeClicked)

        val expectedViewState =
            viewState.copy(
                items = listOf(
                    scanResultToBeClicked.copy(
                        isRowClickable = true,
                        isBlinkProgressVisible = false
                    ),
                    otherItem.copy(isRowClickable = true, isBlinkProgressVisible = false)
                )
            )

        itemBlinkSubject.onNext(BlinkEvent.InProgress)
        itemBlinkSubject.onNext(BlinkEvent.Success(mock()))

        viewStateObserver.assertValueCount(4)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `onBlinkClick sets isBlinkInProgress=false and rowClickable=true when blink emits Timeout`() {
        val scanResultToBeClicked = scanToothbrushItem(isBlinkProgressVisible = false)
        val otherItem = scanToothbrushItem(macAndName = "other", isBlinkProgressVisible = true)
        val itemBlinkSubject = mockBlinkScanResult(scanResultToBeClicked.toothbrushScanResult)

        val viewState =
            ScanToothbrushListViewState(items = listOf(scanResultToBeClicked, otherItem))

        viewModel.updateViewState { viewState }
        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(viewState)

        pushToOnResume()

        viewModel.onBlinkClick(scanResultToBeClicked)

        val expectedViewState =
            viewState.copy(
                items = listOf(
                    scanResultToBeClicked.copy(
                        isRowClickable = true,
                        isBlinkProgressVisible = false
                    ),
                    otherItem.copy(isRowClickable = true, isBlinkProgressVisible = false)
                )
            )

        itemBlinkSubject.onNext(BlinkEvent.InProgress)
        itemBlinkSubject.onNext(BlinkEvent.Timeout)

        viewStateObserver.assertValueCount(4)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `onBlinkClick sets isBlinkInProgress=false and rowClickable=true when blink emits Error`() {
        val scanResultToBeClicked = scanToothbrushItem(isBlinkProgressVisible = false)
        val otherItem = scanToothbrushItem(macAndName = "other", isBlinkProgressVisible = true)
        val itemBlinkSubject = mockBlinkScanResult(scanResultToBeClicked.toothbrushScanResult)

        val viewState =
            ScanToothbrushListViewState(items = listOf(scanResultToBeClicked, otherItem))

        viewModel.updateViewState { viewState }
        val viewStateObserver = viewModel.viewStateFlowable.test()
            .assertValue(viewState)

        pushToOnResume()

        viewModel.onBlinkClick(scanResultToBeClicked)

        val expectedViewState =
            viewState.copy(
                items = listOf(
                    scanResultToBeClicked.copy(
                        isRowClickable = true,
                        isBlinkProgressVisible = false
                    ),
                    otherItem.copy(isRowClickable = true, isBlinkProgressVisible = false)
                )
            )

        itemBlinkSubject.onNext(BlinkEvent.InProgress)
        itemBlinkSubject.onNext(BlinkEvent.Error(TestForcedException()))

        viewStateObserver.assertValueCount(4)

        viewStateObserver.assertLastValue(expectedViewState)
    }

    @Test
    fun `onBlinkClick invokes resumeScanningFromRxSubscription after Blink observable completes`() {
        viewModel = spy(viewModel)

        doNothing().whenever(viewModel).resumeScanningFromRxSubscription()

        val item = scanToothbrushItem()
        val subject = mockBlinkScanResult(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onBlinkClick(item)

        subject.onComplete()

        verify(viewModel).resumeScanningFromRxSubscription()
    }

    @Test
    fun `onBlinkClick resumes scanning if blink throws error`() {
        viewModel = spy(viewModel)

        doNothing().whenever(viewModel).resumeScanningFromRxSubscription()

        val item = scanToothbrushItem()
        val blinkSubject = mockBlinkScanResult(item.toothbrushScanResult)

        mockScanner()

        pushToOnResume()

        viewModel.onBlinkClick(item)

        assertTrue(viewModel.scanDisposable!!.isDisposed)

        blinkSubject.onError(TestForcedException())

        verify(viewModel).resumeScanningFromRxSubscription()
    }

    @Test
    fun `onBlinkClick stops scanning on subscription`() {
        val item = scanToothbrushItem()
        mockBlinkScanResult(item.toothbrushScanResult)

        val scanDisposable = mock<Disposable>()
        viewModel.scanDisposable = scanDisposable

        pushToOnResume()

        viewModel.onBlinkClick(item)

        verify(scanDisposable).dispose()
        assertTrue(viewModel.scanDisposable!!.isDisposed)
    }

    /*
    onCloseClick
     */
    @Test
    fun `onCloseClick sends GoBack event`() {
        viewModel.onCloseClick()

        verify(eventTracker).sendEvent(ScanToothbrushListAnalytics.goBack())
    }

    @Test
    fun `onCloseClick invokes navigateFromScanListToWakeYourBrush`() {
        viewModel.onCloseClick()

        verify(navigator).navigateFromScanListToWakeYourBrush()
    }

    /*
    onResume
     */
    @Test
    fun `onResume invokes checkForEmptyScanResult`() {
        viewModel = spy(viewModel)

        pushToOnResume()

        verify(viewModel).checkForEmptyScanResult()
    }

    @Test
    fun `onResume subscribes to scan`() {
        val scanSubject = pushToOnResume()

        assertTrue(scanSubject.hasObservers())
        assertNotNull(viewModel.scanDisposable)
    }

    @Test
    fun `onResume subscribes to checkConnectionPrerequisitesUseCase`() {
        pushToOnResume()

        assertTrue(prerequisiteStateSubject.hasObservers())
    }

    @Test
    fun `whenever checkConnectionPrerequisitesUseCase emits BluetoothDisabled, LocationServiceDisabled or LocationPermissionNotGranted, invoke validateOrNavigate`() {
        pushToOnResume()

        ConnectionPrerequisitesState.values()
            .filterNot { it == ConnectionAllowed }
            .forEachIndexed { index, newState ->
                prerequisiteStateSubject.onNext(newState)

                verify(
                    scanToothbrushListPrerequisitesUseCase,
                    times(index + 2) // it was already invoked once in onCreate
                ).validateOrNavigate()
            }
    }

    @Test
    fun `whenever checkConnectionPrerequisitesUseCase emits ConnectionAllowed, hide error and never invoke showError`() {
        pushToOnResume()

        prerequisiteStateSubject.onNext(ConnectionAllowed)

        verify(sharedFacade).hideError()
    }

    /*
    Scan
     */
    @Test
    fun `when scan emits BluetoothOff, do nothing, since it will be handled by prerequisites state`() {
        val scanSubject = pushToOnResume()

        viewModel.viewStateFlowable.test().assertValue(ScanToothbrushListViewState.initial())

        scanSubject.onNext(AutoExpireScanResult.BluetoothOff)

        verify(sharedFacade, never()).showError(any())
    }

    @Test
    fun `when scan emits BluetoothOff, clear ViewState results`() {
        val scanSubject = pushToOnResume()

        val viewStateWithResults = ScanToothbrushListViewState.initial()
            .copy(items = listOf(scanToothbrushItem()))

        viewModel.updateViewState { viewStateWithResults }

        val observer =
            viewModel.viewStateFlowable.test().assertValue(viewStateWithResults)

        scanSubject.onNext(AutoExpireScanResult.BluetoothOff)

        observer.assertLastValue(viewStateWithResults.copy(items = listOf()))
    }

    @Test
    fun `when scan emits new results, they are pushed to the ViewState`() {
        val scanSubject = pushToOnResume()

        val observer =
            viewModel.viewStateFlowable.test().assertValue(ScanToothbrushListViewState.initial())

        val mac = "maccc"
        val expectedResult = fakeScanResult(mac = mac, name = mac)
        scanSubject.onNext(AutoExpireScanResult.Batch(listOf(expectedResult)))

        val expectedViewState =
            ScanToothbrushListViewState(items = listOf(scanToothbrushItem(macAndName = expectedResult.mac)))

        observer.assertValueCount(2)
        observer.assertLastValue(expectedViewState)
    }

    @Test
    fun `when scan emits new results and there's a blinking connection, the emitted ViewState includes the blinkingConnection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("randooom")
            .build()
        mockBlinkingConnection(connection)

        val scanSubject = pushToOnResume()

        val observer =
            viewModel.viewStateFlowable.test().assertValue(ScanToothbrushListViewState.initial())

        val mac = "maccc"
        val expectedResult = fakeScanResult(mac = mac, name = mac)
        scanSubject.onNext(AutoExpireScanResult.Batch(listOf(expectedResult)))

        val expectedViewState = ScanToothbrushListViewState.initial()
            .copy(
                items = listOf(
                    scanToothbrushItem(macAndName = expectedResult.mac)
                )
            )

        observer.assertValueCount(2)
        observer.assertLastValue(expectedViewState)
    }

    @Test
    fun `when scan emits new results it should hide the error`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("randooom")
            .build()
        mockBlinkingConnection(connection)

        val scanSubject = pushToOnResume()

        viewModel.viewStateFlowable.test().assertValue(ScanToothbrushListViewState.initial())

        val mac = "maccc"
        val expectedResult = fakeScanResult(mac = mac, name = mac)
        scanSubject.onNext(AutoExpireScanResult.Batch(listOf(expectedResult)))

        verify(sharedFacade).hideError()
    }

    /*
    onItemClick
     */
    @Test
    fun `onItemClick subscribes to blink observable for scan result`() {
        val item = scanToothbrushItem()
        val subject = mockScanResultConfirm(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onItemClick(item)

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `onItemClick invokes navigateFromScanListToModelMismatch if blink is Success and onConnectionConfirmed returns MODEL_MISTACH`() {
        val item = scanToothbrushItem()
        val subject = mockScanResultConfirm(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onItemClick(item)

        mockOnConnectionConfirmed(MODEL_MISMATCH)

        subject.onNext(BlinkEvent.Success(mock()))

        verify(navigator).navigateFromScanListToModelMismatch()
    }

    @Test
    fun `onItemClick invokes navigateFromScanListToSignUp if blink is Success and onConnectionConfirmed returns SIGN_UP`() {
        val item = scanToothbrushItem()
        val subject = mockScanResultConfirm(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onItemClick(item)

        mockOnConnectionConfirmed(SIGN_UP)

        subject.onNext(BlinkEvent.Success(mock()))

        verify(navigator).navigateFromScanListToSignUp()
    }

    @Test
    fun `onItemClick invokes finishFlow if blink is Success and onConnectionConfirmed returns FINISH`() {
        val item = scanToothbrushItem()
        val subject = mockScanResultConfirm(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onItemClick(item)

        mockOnConnectionConfirmed(FINISH)

        subject.onNext(BlinkEvent.Success(mock()))

        verify(navigator).finishFlow()
    }

    @Test
    fun `onItemClick invokes resumeScanningFromRxSubscription if blink is Success and onConnectionConfirmed returns NO_BLINKING_CONNECTION`() {
        viewModel = spy(viewModel)

        doNothing().whenever(viewModel).resumeScanningFromRxSubscription()

        val item = scanToothbrushItem()
        val subject = mockScanResultConfirm(item.toothbrushScanResult)

        pushToOnResume()

        viewModel.onItemClick(item)

        mockOnConnectionConfirmed(NO_BLINKING_CONNECTION)

        subject.onNext(BlinkEvent.Success(mock()))

        verify(viewModel).resumeScanningFromRxSubscription()
    }

    @Test
    fun `onItemClick resumes scanning if blink throws error`() {
        viewModel = spy(viewModel)

        doNothing().whenever(viewModel).resumeScanningFromRxSubscription()

        val item = scanToothbrushItem()
        val blinkSubject = mockScanResultConfirm(item.toothbrushScanResult)

        mockScanner()

        pushToOnResume()

        viewModel.onItemClick(item)

        assertTrue(viewModel.scanDisposable!!.isDisposed)

        blinkSubject.onError(TestForcedException())

        verify(viewModel).resumeScanningFromRxSubscription()
    }

    @Test
    fun `onItemClick stops scanning`() {
        val item = scanToothbrushItem()
        mockScanResultConfirm(item.toothbrushScanResult)

        val scanDisposable = mock<Disposable>()
        viewModel.scanDisposable = scanDisposable

        pushToOnResume()

        viewModel.onItemClick(item)

        verify(scanDisposable).dispose()
        assertTrue(viewModel.scanDisposable!!.isDisposed)
    }

    /*
    onPause
     */

    @Test
    fun `onPause unsubscribes from scan`() {
        val scanSubject = pushToOnResume()

        assertTrue(scanSubject.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertFalse(scanSubject.hasObservers())
    }

    /*
    checkForEmptyScanResult
     */
    @Test
    fun `checkForEmptyScanResult sets showNoBrushFound to true when viewState items are empty for too many advertising windows`() {
        viewModel.updateViewState {
            copy(items = emptyList(), showNoBrushFound = false)
        }

        viewModel.checkForEmptyScanResult()

        prerequisiteStateSubject.onNext(BluetoothDisabled)

        assertFalse(viewModel.getViewState()!!.showNoBrushFound)

        advanceTimeShowNoToothbrush()

        assertTrue(viewModel.getViewState()!!.showNoBrushFound)
    }

    @Test
    fun `checkForEmptyScanResult sets showNoBrushFound to false when viewState items is not empty`() {
        viewModel.updateViewState {
            copy(items = listOf(mock()), showNoBrushFound = true)
        }

        viewModel.checkForEmptyScanResult()

        assertFalse(viewModel.getViewState()!!.showNoBrushFound)
    }

    /*
    Utils
     */
    private fun mockScanner(): PublishSubject<AutoExpireScanResult> {
        val subject = PublishSubject.create<AutoExpireScanResult>()

        whenever(autoExpireScanner.scan()).thenReturn(subject)

        return subject
    }

    private fun mockBlinkingConnection(connection: KLTBConnection = mock()) {
        sharedFacade.mockBlinkingConnection(connection)
    }

    private fun mockBlinkScanResult(scanResult: ToothbrushScanResult): PublishSubject<BlinkEvent> {
        return scanListBlinkConnectionUseCase.mockBlinkScanResult(scanResult)
    }

    private fun mockScanResultConfirm(scanResult: ToothbrushScanResult): PublishSubject<BlinkEvent> {
        val blinkEventSubject = PublishSubject.create<BlinkEvent>()
        whenever(scanListConfirmResultUseCase.confirm(eq(scanResult), any()))
            .thenAnswer { invocation ->
                val doOnSubscribeBlock = invocation.getArgument(1) as (() -> Unit)
                blinkEventSubject
                    .doOnSubscribe { doOnSubscribeBlock() }
            }

        return blinkEventSubject
    }

    private fun pushToOnResume(): PublishSubject<AutoExpireScanResult> {
        val subject = mockScanner()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        return subject
    }

    private fun mockOnConnectionConfirmed(action: ConnectionConfirmedNavigationAction) {
        whenever(nextNavigationActionUseCase.nextNavitationStep()).thenReturn(action)
    }

    private fun advanceTimeShowNoToothbrush(seconds: Long = FOUR_BLUETOOTH_ADVERTISING_WINDOWS) {
        timeoutScheduler.advanceTimeBy(
            seconds,
            TimeUnit.SECONDS
        )
    }
}

internal fun ScanListBlinkConnectionUseCase.mockBlinkScanResult(scanResult: ToothbrushScanResult): PublishSubject<BlinkEvent> {
    val blinkEventSubject = PublishSubject.create<BlinkEvent>()
    whenever(blink(scanResult)).thenReturn(blinkEventSubject)

    return blinkEventSubject
}

private const val FOUR_BLUETOOTH_ADVERTISING_WINDOWS = 20L
