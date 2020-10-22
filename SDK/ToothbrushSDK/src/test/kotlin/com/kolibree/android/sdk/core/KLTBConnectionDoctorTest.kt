package com.kolibree.android.sdk.core

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.scan.ScanBeforeConnectFilter
import com.kolibree.android.sdk.scan.ToothbrushApp
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.usecases.OnConnectionActiveUseCase
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class KLTBConnectionDoctorTest : BaseUnitTest() {
    private val toothbrushScanner: ToothbrushScanner = mock()

    private val connectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock()

    private val bluetoothUtils: IBluetoothUtils = mock()

    private val bluetoothAdapter: BluetoothAdapter = mock()

    private val onConnectionActiveUseCase: OnConnectionActiveUseCase = mock()

    private val scanBeforeConnectFilter: ScanBeforeConnectFilter = mock()

    private lateinit var doctor: KLTBConnectionDoctor

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    /*
    init
     */
    @Test
    fun `init with initialized false invokes establishConnection`() {
        createDoctorSpy()

        doNothing().whenever(doctor).establishConnection()

        assertFalse(doctor.initialized.get())

        doctor.init()

        verify(doctor).establishConnection()
    }

    @Test
    fun `init with initialized false sets initialized to true`() {
        createDoctorSpy()

        doNothing().whenever(doctor).establishConnection()

        assertFalse(doctor.initialized.get())

        doctor.init()

        assertTrue(doctor.initialized.get())
    }

    @Test
    fun `init with initialized true never invokes establishConnection`() {
        createDoctorSpy()

        doctor.initialized.set(true)

        doctor.init()

        verify(doctor, never()).establishConnection()
    }

    @Test
    fun `init with initialized true leaves initialized as true`() {
        createDoctorSpy()

        doctor.initialized.set(true)

        doctor.init()

        assertTrue(doctor.initialized.get())
    }

    /*
    CLOSE
     */
    @Test
    fun `close does nothing if instance is not initialized`() {
        val connection = defaultConnection()
        createDoctorInstance(connection)

        doctor.initialized.set(false)

        doctor.listenToBluetoothStateDisposable = mock()
        doctor.disposables.add(doctor.listenToBluetoothStateDisposable!!)

        doctor.connectionAttemptDisposable = mock()
        doctor.disposables.add(doctor.connectionAttemptDisposable!!)

        doctor.close()

        verify(connection, never()).disconnect()
        verify(connection.state(), never()).unregister(doctor)
        verify(doctor.listenToBluetoothStateDisposable!!, never()).dispose()
        verify(doctor.connectionAttemptDisposable!!, never()).dispose()
        verify(toothbrushScanner, never()).stopScan(doctor)

        assertFalse(doctor.initialized.get())
    }

    @Test
    fun close_invokesDisconnectOnConnection() {
        val connection = defaultConnection()
        createDoctorInstance(connection)

        doctor.initialized.set(true)

        doctor.close()

        verify(connection).disconnect()
    }

    @Test
    fun close_unregistersAsStateListener() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)

        doctor.initialized.set(true)

        doctor.close()

        verify(connection.state()).unregister(doctor)
    }

    @Test
    fun close_disposesListeningToBluetoothStateDisposable() {
        createDoctorInstance()

        doctor.listenToBluetoothStateDisposable = mock()

        doctor.disposables.add(doctor.listenToBluetoothStateDisposable!!)

        doctor.initialized.set(true)

        doctor.close()

        verify(doctor.listenToBluetoothStateDisposable!!).dispose()
    }

    @Test
    fun close_disposesConnectionAttemptDisposable() {
        createDoctorInstance()

        doctor.connectionAttemptDisposable = mock()

        doctor.disposables.add(doctor.connectionAttemptDisposable!!)

        doctor.initialized.set(true)

        doctor.close()

        verify(doctor.connectionAttemptDisposable!!).dispose()
    }

    @Test
    fun close_invokesStopScanner() {
        val connection = defaultConnection()
        createDoctorInstance(connection)

        doctor.initialized.set(true)

        doctor.close()

        verify(toothbrushScanner).stopScan(doctor)
    }

    /*
    runOnConnectionActiveHooks
     */
    @Test
    fun `runOnConnectionActiveHooks subscribes to onConnectionActiveUseCase`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)

        val subject = CompletableSubject.create()
        whenever(onConnectionActiveUseCase.apply(connection)).thenReturn(subject)

        doctor.runOnConnectionActiveHooks(connection)

        assertTrue(subject.hasObservers())
    }

    /*
    ON CONNECTION STATE CHANGED
     */
    @Test
    fun onConnectionStateChanged_newStateAny_isClosedTrue_doesNotInvokeEstablishConnection() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)
        doctorAsSpy()

        doReturn(true).whenever(doctor).isClosed()
        doNothing().whenever(doctor).runOnConnectionActiveHooks(any())

        KLTBConnectionState.values().forEach {
            doctor.onConnectionStateChanged(connection, it)
        }

        verify(doctor, never()).establishConnection()
    }

    @Test
    fun onConnectionStateChanged_newStateAnyExceptTerminated_isClosedFalse_doesNotInvokeEstablishConnection() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)
        doctorAsSpy()

        doReturn(false).whenever(doctor).isClosed()
        doNothing().whenever(doctor).runOnConnectionActiveHooks(any())

        KLTBConnectionState.values().filter { it != TERMINATED }.forEach {
            doctor.onConnectionStateChanged(connection, it)
        }

        verify(doctor, never()).establishConnection()
    }

    @Test
    fun onConnectionStateChanged_newStateTerminated_isClosedFalse_invokesEstablishConnection() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)
        doctorAsSpy()

        doReturn(false).whenever(doctor).isClosed()
        doNothing().whenever(doctor).establishConnection()

        doctor.onConnectionStateChanged(connection, TERMINATED)

        verify(doctor).establishConnection()
    }

    @Test
    fun `onConnectionStateChanged never invokes runOnConnectionActiveHooks when state is not ACTIVE`() {
        KLTBConnectionState.values()
            .filterNot { it == ACTIVE }
            .forEach { state ->
                val connection = KLTBConnectionBuilder
                    .createAndroidLess()
                    .withState(state)
                    .build()

                createDoctorInstance(connection)

                doctor.onConnectionStateChanged(connection, state)

                verify(onConnectionActiveUseCase, never()).apply(connection)
            }
    }

    @Test
    fun `onConnectionStateChanged invokes runOnConnectionActiveHooks when state is ACTIVE`() {
        KLTBConnectionState.values()
            .filter { it == ACTIVE }
            .forEach { state ->
                val connection = KLTBConnectionBuilder
                    .createAndroidLess()
                    .withState(state)
                    .build()

                val localOnConnectionActiveUseCase: OnConnectionActiveUseCase = mock()
                whenever(localOnConnectionActiveUseCase.apply(any()))
                    .thenReturn(Completable.complete())

                doctor = createDoctor(
                    connection = connection,
                    onConnectionActiveUseCase = localOnConnectionActiveUseCase
                )

                doctor.onConnectionStateChanged(connection, state)

                verify(localOnConnectionActiveUseCase).apply(connection)
            }
    }

    /*
    ESTABLISH CONNECTION
     */
    @Test
    fun establishConnection_shouldAttemptConnectionFalse_doesNothing() {
        createDoctorSpy()

        doReturn(false).whenever(doctor).shouldAttemptConnection()

        doctor.establishConnection()

        verify(doctor, never()).listenToBluetoothState()
    }

    @Test
    fun establishConnection_shouldAttemptConnectionTrue_bluetoothNotEnabled_neverInvokesScanFor() {
        createDoctorSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        mockBluetoothStateObservable()

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        doctor.establishConnection()

        verify(toothbrushScanner, never()).scanFor(any())
    }

    @Test
    fun establishConnection_shouldAttemptConnectionTrue_bluetoothNotEnabled_registersAsBluetoothStateListener() {
        createDoctorSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(mock())

        assertNull(doctor.listenToBluetoothStateDisposable)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        mockBluetoothStateObservable()

        doctor.establishConnection()

        verify(bluetoothUtils).bluetoothStateObservable()
        assertNotNull(doctor.listenToBluetoothStateDisposable)
    }

    @Test
    fun establishConnection_shouldAttemptConnectionTrue_bluetoothEnabled_setsConnectionStateToNEW() {
        createDoctorSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(mock())

        assertNull(doctor.listenToBluetoothStateDisposable)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        mockBluetoothStateObservable()

        doNothing().whenever(doctor).scanForToothbrush()

        enableScanBeforeConnect()

        doctor.establishConnection()

        verify(doctor.connection).setState(NEW)
    }

    @Test
    fun establishConnection_shouldAttemptConnectionTrue_bluetoothEnabled_registersAsBluetoothStateListener() {
        createDoctorSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(mock())

        assertNull(doctor.listenToBluetoothStateDisposable)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        mockBluetoothStateObservable()

        doNothing().whenever(doctor).scanForToothbrush()

        enableScanBeforeConnect()

        doctor.establishConnection()

        verify(bluetoothUtils).bluetoothStateObservable()
        assertNotNull(doctor.listenToBluetoothStateDisposable)
    }

    @Test
    fun establishConnection_shouldAttemptConnectionTrue_bluetoothEnabled_startsBluetoothScan() {
        createDoctorSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        val bluetoothDevice: BluetoothDevice = mock()
        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(bluetoothDevice)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        mockBluetoothStateObservable()

        doNothing().whenever(doctor).scanForToothbrush()

        enableScanBeforeConnect()

        doctor.establishConnection()

        verify(doctor).scanForToothbrush()
    }

    @Test
    fun `establish connection invokes scan when device location is ready and scanBeforeConnect returns true`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withModel(CONNECT_E1)
            .withSupportEstablish().build()
        createDoctorInstance(connection)
        doctorAsSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        val bluetoothDevice: BluetoothDevice = mock()
        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(bluetoothDevice)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        mockBluetoothStateObservable()

        enableScanBeforeConnect()

        doNothing().whenever(doctor).scanForToothbrush()

        doctor.establishConnection()

        verify(doctor).scanForToothbrush()
    }

    @Test
    fun `establish connection invokes attemptConnection with establishDFU when device location is ready and scanBeforeConnect returns false and is running bootloader = true`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(CONNECT_E1)
            .withState(NEW)
            .withBootloader(true)
            .withSupportEstablish()
            .build()

        createDoctorInstance(connection)
        doctorAsSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        val bluetoothDevice: BluetoothDevice = mock()
        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(bluetoothDevice)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        mockBluetoothStateObservable()

        whenever(scanBeforeConnectFilter.scanBeforeConnect(connection))
            .thenReturn(false)

        doNothing().whenever(doctor).scanForToothbrush()

        doctor.establishConnection()

        verify(doctor).listenToStateAndAttemptConnection(connection.establishDfuBootloaderCompletable())
    }

    @Test
    fun `establish connection invokes attemptConnection with establishCompletable when device location is ready and scanBeforeConnect returns false and is running bootloader = false`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(CONNECT_E1)
            .withState(NEW)
            .withBootloader(false)
            .withSupportEstablish()
            .build()

        createDoctorInstance(connection)
        doctorAsSpy()
        doReturn(true).whenever(doctor).shouldAttemptConnection()

        val bluetoothDevice: BluetoothDevice = mock()
        whenever(bluetoothAdapter.getRemoteDevice(DEFAULT_MAC)).thenReturn(bluetoothDevice)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        mockBluetoothStateObservable()

        whenever(scanBeforeConnectFilter.scanBeforeConnect(connection))
            .thenReturn(false)

        doNothing().whenever(doctor).scanForToothbrush()

        doctor.establishConnection()

        verify(doctor).listenToStateAndAttemptConnection(connection.establishCompletable())
    }

    /*
    listenToStateAndAttemptConnection
     */
    @Test
    fun `listenToStateAndAttemptConnection subscribes to completable parameter`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withState(NEW)
            .build()

        createDoctorInstance(connection)

        val subject = CompletableSubject.create()
        doctor.listenToStateAndAttemptConnection(subject)

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `listenToStateAndAttemptConnection registers as state listener`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withState(NEW)
            .build()

        createDoctorInstance(connection)

        doctor.listenToStateAndAttemptConnection(Completable.complete())

        verify(connection.state()).register(doctor)
    }

    /*
    LISTEN TO BLUETOOTH STATE
     */
    @Test
    fun `scanForToothbrush invokes toothbushScanner scanFor if deviceLocation is ready`() {
        createDoctorSpy()

        doReturn(true).whenever(doctor).isDeviceLocationReady()

        doctor.scanForToothbrush()

        verify(toothbrushScanner).scanFor(doctor)
    }

    @Test
    fun `scanForToothbrush never invokes toothbushScanner scanFor if deviceLocation is not ready`() {
        createDoctorSpy()

        doNothing().whenever(doctor).listenToLocationStatus()

        doReturn(false).whenever(doctor).isDeviceLocationReady()

        doctor.scanForToothbrush()

        verify(toothbrushScanner, never()).scanFor(doctor)
    }

    @Test
    fun `scanForToothbrush invokes listenToLocationStatus if deviceLocation is not ready`() {
        createDoctorSpy()

        doNothing().whenever(doctor).listenToLocationStatus()

        doReturn(false).whenever(doctor).isDeviceLocationReady()

        doctor.scanForToothbrush()

        verify(doctor).listenToLocationStatus()
    }

    /*
    LISTEN TO BLUETOOTH STATE
     */
    @Test
    fun listenToBluetoothState_newBTState_invokesOnNewBluetoothState() {
        createDoctorInstance()
        doctorAsSpy()

        val btSubject = mockBluetoothStateObservable()

        doNothing().whenever(doctor).onNewBluetoothState(any())

        doctor.listenToBluetoothState()

        verify(doctor, never()).onNewBluetoothState(any())

        btSubject.onNext(true)
        verify(doctor).onNewBluetoothState(true)

        btSubject.onNext(false)
        verify(doctor).onNewBluetoothState(false)
    }

    @Test
    fun listenToBluetoothState_duplicatedBTState_ignoresSecondValue() {
        createDoctorInstance()
        doctorAsSpy()

        val btSubject = mockBluetoothStateObservable()

        doNothing().whenever(doctor).onNewBluetoothState(any())

        doctor.listenToBluetoothState()

        verify(doctor, never()).onNewBluetoothState(any())

        btSubject.onNext(true)
        verify(doctor).onNewBluetoothState(true)

        btSubject.onNext(true)
        verify(doctor, times(1)).onNewBluetoothState(true)
    }

    @Test
    fun listenToBluetoothState_listenToBluetoothStateDisposableNotNull_doesNotSubscribe() {
        createDoctorInstance()
        doctor.listenToBluetoothStateDisposable = mock()

        doctor.listenToBluetoothState()

        verify(bluetoothUtils, never()).bluetoothStateObservable()
    }

    @Test
    fun listenToBluetoothState_listenToBluetoothStateDisposableNotDisposed_doesNotSubscribe() {
        createDoctorInstance()
        doctor.listenToBluetoothStateDisposable = mock()

        doctor.listenToBluetoothState()

        verify(bluetoothUtils, never()).bluetoothStateObservable()
    }

    @Test
    fun listenToBluetoothState_listenToBluetoothStateDisposableDisposed_subscribes() {
        createDoctorInstance()
        doctor.listenToBluetoothStateDisposable = mock()
        whenever(doctor.listenToBluetoothStateDisposable?.isDisposed).thenReturn(true)

        mockBluetoothStateObservable()

        doctor.listenToBluetoothState()

        verify(bluetoothUtils).bluetoothStateObservable()
    }

    /*
    ON NEW BLUETOOTH STATE
     */
    @Test
    fun onNewBluetoothState_true_attemptsToEstablishConnection() {
        createDoctorInstance()
        doctorAsSpy()

        doNothing().whenever(doctor).establishConnection()

        doctor.onNewBluetoothState(true)

        verify(doctor).establishConnection()
    }

    @Test
    fun onNewBluetoothState_false_invokesAbortConnection() {
        createDoctorInstance()
        doctorAsSpy()

        doctor.connectionAttemptDisposable = mock()

        doctor.onNewBluetoothState(false)

        verify(doctor.connectionAttemptDisposable)?.dispose()
    }

    @Test
    fun onNewBluetoothState_false_stopsScan() {
        createDoctorInstance()
        doctorAsSpy()

        doctor.onNewBluetoothState(false)

        verify(toothbrushScanner).stopScan(doctor)
    }

    /*
    ON TOOTHBRUSH FOUND
     */
    @Test
    fun onErrorEstablishingConnection_unregistersAsStateListener() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)
        doctorAsSpy()

        doReturn(true).whenever(doctor).isClosed()

        doctor.onErrorEstablishingConnection(mock())

        verify(connection.state()).unregister(doctor)
    }

    @Test
    fun onErrorEstablishingConnection_doctorClosed_doesNotInvokeEstablishConnection() {
        createDoctorInstance()
        doctorAsSpy()

        doReturn(true).whenever(doctor).isClosed()

        doctor.onErrorEstablishingConnection(mock())

        verify(doctor, never()).establishConnection()
    }

    @Test
    fun onErrorEstablishingConnection_doctorNotClosed_invokeEstablishConnection() {
        createDoctorInstance()
        doctorAsSpy()

        doReturn(false).whenever(doctor).isClosed()
        doNothing().whenever(doctor).establishConnection()

        doctor.onErrorEstablishingConnection(mock())

        verify(doctor).establishConnection()
    }

    /*
    ON TOOTHBRUSH FOUND
     */

    @Test
    fun onToothbrushFound_shouldAttemptConnectionFalse_doesNothing() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().build()
        createDoctorInstance(connection)
        doctorAsSpy()

        doReturn(false).whenever(doctor).shouldAttemptConnection()

        doctor.onToothbrushFound(mock())

        verify(connection, never()).establishCompletable()
    }

    @Test
    fun onToothbrushFound_shouldAttemptConnectionTrue_subscribesToEstablishCompletable() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().build()
        createDoctorInstance(connection)

        doctorAsSpy()

        doReturn(true).whenever(doctor).shouldAttemptConnection()

        val result = mock<ToothbrushScanResult>()
        val completableSubject = CompletableSubject.create()
        doReturn(completableSubject).whenever(doctor).establishCompletable(result)

        assertEquals(0, doctor.disposables.size())

        doNothing().whenever(doctor).listenToConnectionState()

        doctor.onToothbrushFound(result)

        verify(doctor).establishCompletable(result)

        assertTrue(completableSubject.hasObservers())
        assertEquals(1, doctor.disposables.size())
    }

    @Test
    fun onToothbrushFound_shouldAttemptConnectionTrue_establishCompletableError_invokesOnErrorEstablishingConnection() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().build()
        createDoctorInstance(connection)

        doctorAsSpy()

        doReturn(true).whenever(doctor).shouldAttemptConnection()

        val completableSubject = CompletableSubject.create()
        doReturn(completableSubject).whenever(connection).establishCompletable()

        doNothing().whenever(doctor).listenToConnectionState()

        doctor.onToothbrushFound(mock())

        doNothing().whenever(doctor).onErrorEstablishingConnection(any())

        completableSubject.onError(Exception("Test forced error"))

        verify(doctor).onErrorEstablishingConnection(any())
    }

    @Test
    fun onToothbrushFound_shouldAttemptConnectionTrue_registersAsConnectionStateListener() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withSupportEstablish().build()
        createDoctorInstance(connection)

        doctorAsSpy()

        doReturn(true).whenever(doctor).shouldAttemptConnection()

        doNothing().whenever(doctor).listenToConnectionState()

        doctor.onToothbrushFound(mock())

        verify(doctor).listenToConnectionState()
    }

    @Test
    fun onToothbrushFound_noAttemptInProgress_storesDisposableWhileConnecting_nullifiesOnSuccess() {
        val completableSubject = CompletableSubject.create()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withSupportEstablish(completableSubject)
                .build()
        createDoctorInstance(connection)

        doctorAsSpy()

        doReturn(true).whenever(doctor).shouldAttemptConnection()

        doNothing().whenever(doctor).listenToConnectionState()

        doctor.onToothbrushFound(mock())

        assertNotNull(doctor.connectionAttemptDisposable)

        completableSubject.onComplete()

        assertNull(doctor.connectionAttemptDisposable)
    }

    @Test
    fun onToothbrushFound_noAttemptInProgress_storesDisposableWhileConnecting_nullifiesOnError() {
        val completableSubject = CompletableSubject.create()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withSupportEstablish(completableSubject)
                .build()
        createDoctorInstance(connection)

        doctorAsSpy()

        doReturn(true).whenever(doctor).shouldAttemptConnection()

        doNothing().whenever(doctor).listenToConnectionState()

        doctor.onToothbrushFound(mock())

        assertNotNull(doctor.connectionAttemptDisposable)

        completableSubject.onError(Exception("test forced exception"))

        assertNull(doctor.connectionAttemptDisposable)
    }

    /*
    listenToConnectionState
     */

    @Test
    fun `listenToConnectionState registers as state listener`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)

        doctorAsSpy()

        doNothing().whenever(doctor).onConnectionStateChanged(eq(connection), any())

        doctor.listenToConnectionState()

        verify(connection.state()).register(doctor)
    }

    /*
    SHOULD SCAN FOR TOOTHBRUSH
     */

    @Test
    fun `shouldScanForToothbrush returns true if scanBeforeConnectFilter returns true`() {
        createDoctorInstance()

        enableScanBeforeConnect()

        assertTrue(doctor.shouldScanForToothbrush())
    }

    @Test
    fun `shouldScanForToothbrush returns false if scanBeforeConnectFilter returns false`() {
        createDoctorInstance()

        disableScanBeforeConnect()

        assertFalse(doctor.shouldScanForToothbrush())
    }

    /*
    ESTABLISH COMPLETABLE
     */

    @Test
    fun establishCompletable_toothbrushAppDFU_BOOTLOADER_invokesEstablishDfuBootloaderCompletable() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)

        val result: ToothbrushScanResult = mock()
        whenever(result.toothbrushApp).thenReturn(ToothbrushApp.DFU_BOOTLOADER)
        doctor.establishCompletable(result)

        verify(connection).establishDfuBootloaderCompletable()
    }

    @Test
    fun establishCompletable_toothbrushAppMain_invokesEstablishCompletable() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)

        val result: ToothbrushScanResult = mock()
        whenever(result.toothbrushApp).thenReturn(ToothbrushApp.MAIN)
        doctor.establishCompletable(result)

        verify(connection).establishCompletable()
    }

    @Test
    fun establishCompletable_toothbrushAppUnknown_invokesEstablishCompletable() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorInstance(connection)

        val result: ToothbrushScanResult = mock()
        whenever(result.toothbrushApp).thenReturn(ToothbrushApp.UNKNOWN)
        doctor.establishCompletable(result)

        verify(connection).establishCompletable()
    }

    /*
    LISTEN TO LOCATION STATUS
     */

    @Test
    fun `listenToLocationStatus does not subscribe if it's already listening to status updates`() {
        createDoctorInstance()

        doctor.listenToLocationStatusDisposable = mock()
        whenever(doctor.listenToLocationStatusDisposable!!.isDisposed).thenReturn(false)

        doctor.listenToLocationStatus()

        verify(connectionPrerequisitesUseCase, never()).checkOnceAndStream()
    }

    @Test
    fun `listenToLocationStatus unsubscribes after ConnectionAllowed`() {
        createDoctorInstance()

        val subject = PublishSubject.create<ConnectionPrerequisitesState>()
        whenever(connectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(subject)

        doctor.listenToLocationStatus()

        assertTrue(subject.hasObservers())

        subject.onNext(ConnectionPrerequisitesState.LocationPermissionNotGranted)

        assertTrue(subject.hasObservers())

        subject.onNext(ConnectionPrerequisitesState.LocationServiceDisabled)

        assertTrue(subject.hasObservers())

        subject.onNext(ConnectionPrerequisitesState.ConnectionAllowed)

        assertFalse(subject.hasObservers())
    }

    @Test
    fun `listenToLocationStatus unsubscribes after disposing`() {
        createDoctorInstance()

        val subject = PublishSubject.create<ConnectionPrerequisitesState>()
        whenever(connectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(subject)

        doctor.listenToLocationStatus()

        assertTrue(subject.hasObservers())

        doctor.disposables.dispose()

        assertFalse(subject.hasObservers())
    }

    @Test
    fun `listenToLocationStatus invokes onDeviceLocationReady after ConnectionAllowed`() {
        createDoctorSpy()

        val subject = PublishSubject.create<ConnectionPrerequisitesState>()
        whenever(connectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(subject)

        doNothing().whenever(doctor).onDeviceLocationReady()

        doctor.listenToLocationStatus()

        verify(doctor, never()).onDeviceLocationReady()

        subject.onNext(ConnectionPrerequisitesState.LocationPermissionNotGranted)

        verify(doctor, never()).onDeviceLocationReady()

        subject.onNext(ConnectionPrerequisitesState.LocationServiceDisabled)

        verify(doctor, never()).onDeviceLocationReady()

        subject.onNext(ConnectionPrerequisitesState.ConnectionAllowed)

        verify(doctor).onDeviceLocationReady()
    }

    /*
    IS DEVICE LOCATION READY
     */

    @Test
    fun `onDeviceLocationReady invokes establish connection`() {
        createDoctorSpy()

        doNothing().whenever(doctor).establishConnection()

        doctor.onDeviceLocationReady()

        verify(doctor).establishConnection()
    }

    /*
    IS DEVICE LOCATION READY
     */

    @Test
    fun `isDeviceLocationReady returns true if connectionPrerequisitesUseCase returns ConnectionAllowed`() {
        createDoctorInstance()

        whenever(connectionPrerequisitesUseCase.checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.ConnectionAllowed)

        assertTrue(doctor.isDeviceLocationReady())
    }

    @Test
    fun `isDeviceLocationReady returns false if connectionPrerequisitesUseCase does not returns ConnectionAllowed`() {
        createDoctorInstance()

        whenever(connectionPrerequisitesUseCase.checkConnectionPrerequisites()).thenReturn(
            ConnectionPrerequisitesState.BluetoothDisabled
        )

        assertFalse(doctor.isDeviceLocationReady())

        whenever(connectionPrerequisitesUseCase.checkConnectionPrerequisites()).thenReturn(
            ConnectionPrerequisitesState.LocationServiceDisabled
        )

        assertFalse(doctor.isDeviceLocationReady())

        whenever(connectionPrerequisitesUseCase.checkConnectionPrerequisites()).thenReturn(
            ConnectionPrerequisitesState.LocationPermissionNotGranted
        )

        assertFalse(doctor.isDeviceLocationReady())
    }

    /*
    is closed
     */

    @Test
    fun `isClosed returns false if instance is initialized`() {
        createDoctorInstance()

        doctor.initialized.set(true)

        assertFalse(doctor.isClosed())
    }

    @Test
    fun `isClosed returns true if instance is not initialized`() {
        createDoctorInstance()

        doctor.initialized.set(false)

        assertTrue(doctor.isClosed())
    }

    /*
    SHOULD ATTEMPT CONNECTION
     */

    @Test
    fun `shouldAttemptConnection returns false if doctor is closed`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorSpy(connection)

        doReturn(true).whenever(doctor).isClosed()

        assertFalse(doctor.shouldAttemptConnection())

        verifyNoMoreInteractions(connection.state())
    }

    @Test
    fun shouldAttemptConnection_disposableIsNull_stateIsNEW_returnsTrue() {
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withState(NEW).build()
        createDoctorSpy(connection)

        doReturn(false).whenever(doctor).isClosed()
        doReturn(true).whenever(connection).isConnectionAllowed()

        assertTrue(doctor.shouldAttemptConnection())
    }

    @Test
    fun shouldAttemptConnection_disposableIsNull_stateIsTERMINATED_returnsTrue() {
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withState(TERMINATED)
                .build()
        createDoctorSpy(connection)

        doReturn(false).whenever(doctor).isClosed()
        doReturn(true).whenever(connection).isConnectionAllowed()

        assertTrue(doctor.shouldAttemptConnection())
    }

    @Test
    fun shouldAttemptConnection_disposableIsNull_stateIsOtherThanNewOrTerminated_returnsFalse() {
        KLTBConnectionState.values().forEach { state ->
            if (state == NEW || state == TERMINATED) return

            val connection = KLTBConnectionBuilder.createAndroidLess().withState(state).build()
            createDoctorSpy(connection)

            doReturn(false).whenever(doctor).isClosed()

            assertFalse(doctor.shouldAttemptConnection())
        }
    }

    @Test
    fun shouldAttemptConnection_disposableIsNotNullAndNotDisposed_returnsFalse() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorSpy(connection)

        doReturn(false).whenever(doctor).isClosed()

        val disposable = mock<Disposable>()
        whenever(disposable.isDisposed).thenReturn(false)
        doctor.connectionAttemptDisposable = disposable

        assertFalse(doctor.shouldAttemptConnection())

        verify(connection, never()).state()
    }

    @Test
    fun shouldAttemptConnection_disposableIsNotNullButItIsDisposed_proceedsToCheckState() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        createDoctorSpy(connection)

        doReturn(false).whenever(doctor).isClosed()
        doReturn(true).whenever(connection).isConnectionAllowed()

        val disposable = mock<Disposable>()
        whenever(disposable.isDisposed).thenReturn(true)
        doctor.connectionAttemptDisposable = disposable

        doctor.shouldAttemptConnection()

        verify(connection).state()
    }

    @Test
    fun shouldAttemptConnection_connectionNotAllowed_returnsFalse() {
        val connection = KLTBConnectionBuilder
            .createAndroidLess()
            .withState(TERMINATED)
            .build()
        createDoctorSpy(connection)

        doReturn(false).whenever(doctor).isClosed()
        doReturn(false).whenever(connection).isConnectionAllowed()

        assertFalse(doctor.shouldAttemptConnection())
    }

    /*
    UTILS
     */
    internal fun createDoctorInstance(
        connection: InternalKLTBConnection = defaultConnection()
    ) {
        doctor = createDoctor(
            connection = connection,
            toothbrushScanner = toothbrushScanner,
            connectionPrerequisitesUseCase = connectionPrerequisitesUseCase,
            bluetoothUtils = bluetoothUtils,
            bluetoothAdapter = bluetoothAdapter,
            onConnectionActiveUseCase = onConnectionActiveUseCase,
            scanBeforeConnectFilter = scanBeforeConnectFilter
        )
    }

    private fun createDoctorSpy(connection: InternalKLTBConnection = defaultConnection()) {
        createDoctorInstance(connection)
        doctorAsSpy()
    }

    private fun doctorAsSpy() {
        doctor = spy(doctor)
    }

    private fun defaultConnection() = KLTBConnectionBuilder.createAndroidLess().build()

    private fun mockBluetoothStateObservable(): PublishSubject<Boolean> {
        val btStateSubject = PublishSubject.create<Boolean>()
        whenever(bluetoothUtils.bluetoothStateObservable()).thenReturn(btStateSubject)

        return btStateSubject
    }

    private fun enableScanBeforeConnect() {
        whenever(scanBeforeConnectFilter.scanBeforeConnect(doctor.connection))
            .thenReturn(true)
    }

    private fun disableScanBeforeConnect() {
        whenever(scanBeforeConnectFilter.scanBeforeConnect(doctor.connection))
            .thenReturn(false)
    }
}

internal fun createDoctor(
    connection: InternalKLTBConnection,
    toothbrushScanner: ToothbrushScanner = mock(),
    connectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock(),
    bluetoothUtils: IBluetoothUtils = mock(),
    bluetoothAdapter: BluetoothAdapter = mock(),
    scanBeforeConnectFilter: ScanBeforeConnectFilter = mock(),
    onConnectionActiveUseCase: OnConnectionActiveUseCase = mock()
): KLTBConnectionDoctor {
    return KLTBConnectionDoctor(
        connection = connection,
        toothbrushScanner = toothbrushScanner,
        connectionPrerequisitesUseCase = connectionPrerequisitesUseCase,
        bluetoothUtils = bluetoothUtils,
        bluetoothAdapter = bluetoothAdapter,
        scanBeforeConnectFilter = scanBeforeConnectFilter,
        onConnectionActiveUseCase = onConnectionActiveUseCase
    )
}
