/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.DetectorsManager
import com.kolibree.android.sdk.connection.detectors.RawDetector
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.math.Vector
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.android.unity.BaseGameMiddlewareInstrumentationTest
import com.kolibree.game.middleware.BrushingState
import com.kolibree.game.middleware.ConnectionState
import com.kolibree.game.middleware.DataCallback
import com.kolibree.game.middleware.ReconnectionCallback
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToothbrushInteractorImplTest : BaseGameMiddlewareInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val overrideSchedulersRule = UnitTestImmediateRxSchedulersOverrideRule()

    private val lifecycle: Lifecycle = mock()

    private val lifecycleDisposableScopeOwner = LifecycleDisposableScopeOwner(lifecycle)

    private val connection: KLTBConnection = mock()

    private val state: com.kolibree.android.sdk.connection.state.ConnectionState = mock()

    private val vibrator: Vibrator = mock()

    private val detectors = mock<DetectorsManager>()

    private val rawDataDetector: RawDetector = mock()

    private lateinit var toothbrushInteractor: ToothbrushInteractorImplSpy

    override fun setUp() {
        super.setUp()
        setupMocks()
        toothbrushInteractor = ToothbrushInteractorImplSpy(
            lifecycleDisposableScopeOwner,
            connection,
            lifecycle,
            overrideSchedulersRule.scheduler()
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forE1() {
        setupMocks(toothbrushModel = ToothbrushModel.CONNECT_E1)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.CONNECT_E1,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forE2() {
        setupMocks(toothbrushModel = ToothbrushModel.CONNECT_E2)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.CONNECT_E2,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forB1() {
        setupMocks(toothbrushModel = ToothbrushModel.CONNECT_B1)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.CONNECT_B1,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forM1() {
        setupMocks(toothbrushModel = ToothbrushModel.CONNECT_M1)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.CONNECT_M1,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forPlaqless() {
        setupMocks(toothbrushModel = ToothbrushModel.PLAQLESS)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.PLAQLESS,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forAra() {
        setupMocks(toothbrushModel = ToothbrushModel.ARA)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.ARA,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forHiLink() {
        setupMocks(toothbrushModel = ToothbrushModel.HILINK)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.HILINK,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forHumElectric() {
        setupMocks(toothbrushModel = ToothbrushModel.HUM_ELECTRIC)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.HUM_ELECTRIC,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forHumBattery() {
        setupMocks(toothbrushModel = ToothbrushModel.HUM_BATTERY)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.HUM_BATTERY,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun getToothbrushModel_returnsCorrectMiddlewareModel_forGlint() {
        setupMocks(toothbrushModel = ToothbrushModel.GLINT)
        assertEquals(
            com.kolibree.game.middleware.ToothbrushModel.GLINT,
            toothbrushInteractor.toothbrushModel
        )
    }

    @Test
    fun toothbrushInteractor_properlyHandlesLifecycleDisposableScopes() {
        testInteractorLifecycle(toothbrushInteractor, lifecycleDisposableScopeOwner)
    }

    /*
    KLTBConnectionState.toMiddlewareType
     */

    @Test
    fun toMiddlewareType_ACTIVE_returnsConnected() {
        assertEquals(ConnectionState.CONNECTED, KLTBConnectionState.ACTIVE.toMiddlewareType())
    }

    @Test
    fun toMiddlewareType_OTA_returnsConnected() {
        assertEquals(ConnectionState.CONNECTED, KLTBConnectionState.OTA.toMiddlewareType())
    }

    @Test
    fun toMiddlewareType_NEW_returnsConnecting() {
        assertEquals(ConnectionState.CONNECTING, KLTBConnectionState.NEW.toMiddlewareType())
    }

    @Test
    fun toMiddlewareType_ESTABLISHING_returnsConnecting() {
        assertEquals(
            ConnectionState.CONNECTING,
            KLTBConnectionState.ESTABLISHING.toMiddlewareType()
        )
    }

    @Test
    fun toMiddlewareType_TERMINATING_returnsDisconnected() {
        assertEquals(
            ConnectionState.DISCONNECTED,
            KLTBConnectionState.TERMINATING.toMiddlewareType()
        )
    }

    @Test
    fun toMiddlewareType_TERMINATED_returnsDisconnected() {
        assertEquals(
            ConnectionState.DISCONNECTED,
            KLTBConnectionState.TERMINATED.toMiddlewareType()
        )
    }

    /*
    KLTBConnection.getBrushingState
     */

    @Test
    fun getBrushingState_returns_STARTED_when_vibrator_isOn_returns_true() {
        setupMocks()
        whenever(vibrator.isOn).thenReturn(true)
        assertEquals(BrushingState.STARTED, connection.getBrushingState())
    }

    @Test
    fun getBrushingState_returns_STOPPED_when_vibrator_isOn_returns_true() {
        setupMocks()
        whenever(vibrator.isOn).thenReturn(false)
        assertEquals(BrushingState.STOPPED, connection.getBrushingState())
    }

    /*
    start
     */

    @Test
    fun start_subscribe_to_vibrator_on() {
        val expectedCompletable = CompletableSubject.create()
        whenever(vibrator.on()).thenReturn(expectedCompletable)
        setupMocks()

        toothbrushInteractor.lifecycleDisposableScopeOwner.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        toothbrushInteractor.start()

        verify(vibrator).on()
        assertTrue(expectedCompletable.hasObservers())
    }

    @Test
    fun start_notifyBrushingStateChange_with_brushingState_STARTING() {
        val expectedCompletable = Completable
            .complete()
        whenever(vibrator.on()).thenReturn(expectedCompletable)
        setupMocks()

        toothbrushInteractor.lifecycleDisposableScopeOwner.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        toothbrushInteractor.start()

        assertEquals(1, toothbrushInteractor.stateChangedNotifications.size)
        assertEquals(BrushingState.STARTING, toothbrushInteractor.stateChangedNotifications[0])
    }

    /*
    stop
     */

    @Test
    fun stop_subscribe_to_vibrator_off() {
        val expectedCompletable = CompletableSubject.create()
        whenever(vibrator.off()).thenReturn(expectedCompletable)
        setupMocks()

        toothbrushInteractor.lifecycleDisposableScopeOwner.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        toothbrushInteractor.stop()

        verify(vibrator).off()
        assertTrue(expectedCompletable.hasObservers())
    }

    @Test
    fun stop_notifyBrushingStateChange_with_brushingState_STOPPING() {
        val expectedCompletable = Completable
            .complete()
        whenever(vibrator.off()).thenReturn(expectedCompletable)
        setupMocks()

        toothbrushInteractor.lifecycleDisposableScopeOwner.lifecycleTester()
            .pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        toothbrushInteractor.stop()

        assertEquals(1, toothbrushInteractor.stateChangedNotifications.size)
        assertEquals(BrushingState.STOPPING, toothbrushInteractor.stateChangedNotifications[0])
    }

    /*
    onVibratorStateChanged
     */

    @Test
    fun onVibratorStateChanged_notifyBrushingStateChange_with_STARTED_when_on_true() {
        setupMocks()
        toothbrushInteractor.onVibratorStateChanged(connection, true)

        assertEquals(1, toothbrushInteractor.stateChangedNotifications.size)
        assertEquals(BrushingState.STARTED, toothbrushInteractor.stateChangedNotifications[0])
    }

    @Test
    fun onVibratorStateChanged_notifyBrushingStateChange_with_STOPPED_when_on_false() {
        setupMocks()
        toothbrushInteractor.onVibratorStateChanged(connection, false)

        assertEquals(1, toothbrushInteractor.stateChangedNotifications.size)
        assertEquals(BrushingState.STOPPED, toothbrushInteractor.stateChangedNotifications[0])
    }

    /*
    enableRawData
     */

    @Test
    fun enableRawData_setsRawDataCallback() {
        val expectedCallback = mock<DataCallback>()
        assertNull(toothbrushInteractor.rawDataCallback.get())

        toothbrushInteractor.enableRawData(expectedCallback)

        assertEquals(expectedCallback, toothbrushInteractor.rawDataCallback.get())
    }

    @Test
    fun enableRawData_registersListener() {
        val expectedCallback = mock<DataCallback>()

        toothbrushInteractor.enableRawData(expectedCallback)

        verify(rawDataDetector).register(toothbrushInteractor)
    }

    @Test
    fun enableRawData_invokesEnableRawDataNotifications() {
        toothbrushInteractor.enableRawData(mock())

        verify(detectors).enableRawDataNotifications()
    }

    /*
    disableRawData
     */

    @Test
    fun disableRawData_unregistersListener() {
        toothbrushInteractor.disableRawData()

        verify(rawDataDetector).unregister(toothbrushInteractor)
    }

    @Test
    fun disableRawData_nullifiesRawDataCallback() {
        toothbrushInteractor.rawDataCallback.set(mock())

        toothbrushInteractor.disableRawData()

        assertNull(toothbrushInteractor.rawDataCallback.get())
    }

    @Test
    fun disableRawData_invokesDisableRawDataNotifications() {
        toothbrushInteractor.disableRawData()

        verify(detectors).disableRawDataNotifications()
    }

    /*
    RawSensorState.toMiddlewareType
     */

    @Test
    fun toMiddlewareType_properlyMapsRawSensorState() {
        val timestamp = 1986.1983f
        val expectedTimestamp = RawSensorState.convertRawTimestamp(timestamp)
        val expectedGyrX = 1.1f
        val expectedGyrY = 2.2f
        val expectedGyrZ = 3.3f
        val expectedAccelX = 21.1f
        val expectedAccelY = 23.2f
        val expectedAccelZ = 34.3f

        val rawSensorState = RawSensorState(
            timestamp,
            Vector(expectedAccelX, expectedAccelY, expectedAccelZ),
            Vector(expectedGyrX, expectedGyrY, expectedGyrZ),
            Vector(0f, 0f, 0f)
        )

        val mappedData = rawSensorState.toMiddlewareType()

        assertEquals(expectedTimestamp, mappedData.timestamp)
        assertEquals(expectedGyrX, mappedData.gyroX)
        assertEquals(expectedGyrY, mappedData.gyroY)
        assertEquals(expectedGyrZ, mappedData.gyroZ)
        assertEquals(expectedAccelX, mappedData.accelX)
        assertEquals(expectedAccelY, mappedData.accelY)
        assertEquals(expectedAccelZ, mappedData.accelZ)
    }

    /*
    setOnReconnectionCallback
     */

    @Test
    fun setOnReconnectionCallback_setsReconnectionCallback() {
        val expectedCallback = mock<ReconnectionCallback>()

        assertNull(toothbrushInteractor.reconnectionCallback.get())

        toothbrushInteractor.setOnReconnectionCallback(expectedCallback)

        assertEquals(expectedCallback, toothbrushInteractor.reconnectionCallback.get())
    }

    /*
    clearReconnectionCallback
     */

    @Test
    fun clearReconnectionCallback_sets_reconnectionCallback_to_null() {
        val expectedCallback = mock<ReconnectionCallback>()

        toothbrushInteractor.setOnReconnectionCallback(expectedCallback)
        toothbrushInteractor.clearReconnectionCallback()

        assertNull(toothbrushInteractor.reconnectionCallback.get())
    }

    /*
    onConnectionStateChanged
     */

    @Test
    fun onConnectionStateChanged_terminated_setsIsReconnectingToTrue() {
        val callback = mock<ReconnectionCallback>()

        toothbrushInteractor.reconnectionCallback.set(callback)
        assertFalse(toothbrushInteractor.isReconnecting.get())
        toothbrushInteractor.onConnectionStateChanged(connection, KLTBConnectionState.TERMINATED)

        assertTrue(toothbrushInteractor.isReconnecting.get())
        verify(callback, never()).onReconnection()
    }

    @Test
    fun onConnectionStateChanged_activeAndIsReconnectingTrue_setsIsReconnectingToFalse() {
        toothbrushInteractor.isReconnecting.set(true)
        toothbrushInteractor.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        assertFalse(toothbrushInteractor.isReconnecting.get())
    }

    @Test
    fun onConnectionStateChanged_activeAndIsReconnectingTrue_invokesCallback() {
        val callback = mock<ReconnectionCallback>()

        toothbrushInteractor.reconnectionCallback.set(callback)
        toothbrushInteractor.isReconnecting.set(true)
        toothbrushInteractor.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        verify(callback).onReconnection()
    }

    @Test
    fun onConnectionStateChanged_reconnection_with_raw_data_enable_register_raw_data() {
        val callback = mock<ReconnectionCallback>()

        toothbrushInteractor.reconnectionCallback.set(callback)
        toothbrushInteractor.isReconnecting.set(true)
        toothbrushInteractor.rawDataEnabled.set(true)
        toothbrushInteractor.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        verify(callback).onReconnection()
        verify(detectors).enableRawDataNotifications()
        verify(detectors.rawData()).register(toothbrushInteractor)
    }

    @Test
    fun onConnectionStateChanged_reconnection_with_raw_data_disable_does_not_register_raw_data() {
        val callback = mock<ReconnectionCallback>()

        toothbrushInteractor.reconnectionCallback.set(callback)
        toothbrushInteractor.isReconnecting.set(true)
        toothbrushInteractor.rawDataEnabled.set(false)
        toothbrushInteractor.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        verify(callback).onReconnection()
        verify(detectors, never()).enableRawDataNotifications()
        verify(detectors.rawData(), never()).register(toothbrushInteractor)
    }

    @Test
    fun onConnectionStateChanged_notifyConnectionStateChange_with_middleware_type() {
        toothbrushInteractor.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        assertEquals(1, toothbrushInteractor.connectionStateChangedNotifications.size)
        assertEquals(
            ConnectionState.CONNECTED,
            toothbrushInteractor.connectionStateChangedNotifications[0]
        )
    }

    /*
    getSerial
     */

    @Test
    fun getSerial_returns_name_of_toothbrush() {
        val expectedName = "TB"

        whenever(connection.toothbrush().getName()).thenReturn(expectedName)
        assertEquals(expectedName, toothbrushInteractor.serial)
    }

    /*
    getMacAddress
     */

    @Test
    fun getMacAddress_returns_mac_of_toothbrush() {
        val expectedMac = "MAC"

        whenever(connection.toothbrush().mac).thenReturn(expectedMac)
        assertEquals(expectedMac, toothbrushInteractor.macAddress)
    }

    @Test
    fun getToothbrushVersion_returns_versions() {
        whenever(connection.toothbrush().hardwareVersion).thenReturn(HardwareVersion(1, 1))
        whenever(connection.toothbrush().firmwareVersion).thenReturn(SoftwareVersion(1, 2, 3))

        assertEquals("1.1", toothbrushInteractor.toothbrushVersion.hwVersion)
        assertEquals("1.2.3", toothbrushInteractor.toothbrushVersion.fwVersion)
    }

    @Test
    fun getCalibration_returns_calibration() {
        val expectedCalibration = floatArrayOf(1.0f, 2.0f)
        whenever(connection.detectors().calibrationData).thenReturn(expectedCalibration)

        assertEquals(expectedCalibration.map { it.toFloat() }, toothbrushInteractor.calibration.toTypedArray().map { it.toFloat() })
    }

    /*
    onCreate
     */

    @Test
    fun onCreate_registersItselfAsStateListener() {
        toothbrushInteractor.onCreate()

        verify(state).register(toothbrushInteractor)
    }

    @Test
    fun onCreate_registersItselfAsVibratorListener() {
        toothbrushInteractor.onCreate()

        verify(vibrator).register(toothbrushInteractor)
    }

    /*
    onDestroy
     */

    @Test
    fun onDestroy_unregistersItselfFromVibratorListeners() {
        toothbrushInteractor.onDestroy()

        verify(vibrator).unregister(toothbrushInteractor)
    }

    @Test
    fun onDestroy_unregistersItselfFromStateListeners() {
        toothbrushInteractor.onDestroy()

        verify(state).unregister(toothbrushInteractor)
    }

    @Test
    fun onDestroy_unregistersItselfFromRawDataListeners() {
        toothbrushInteractor.onDestroy()

        verify(rawDataDetector).unregister(toothbrushInteractor)
    }

    /*
    Utils
     */

    private fun setupMocks(toothbrushModel: ToothbrushModel = ToothbrushModel.CONNECT_E2) {
        whenever(state.current).thenReturn(KLTBConnectionState.ACTIVE)

        whenever(detectors.rawData()).thenReturn(rawDataDetector)

        val toothbrush: Toothbrush = mock()
        whenever(toothbrush.model).thenReturn(toothbrushModel)
        whenever(connection.toothbrush()).thenReturn(toothbrush)
        whenever(connection.state()).thenReturn(state)
        whenever(connection.vibrator()).thenReturn(vibrator)
        whenever(connection.detectors()).thenReturn(detectors)
    }

    internal class ToothbrushInteractorImplSpy(
        lifecycleDisposableScopeOwner: LifecycleDisposableScopeOwner,
        connection: KLTBConnection,
        lifecycle: Lifecycle,
        callbackScheduler: Scheduler
    ) : ToothbrushInteractorImpl(
        lifecycleDisposableScopeOwner,
        connection,
        lifecycle,
        callbackScheduler
    ) {

        val stateChangedNotifications = arrayListOf<BrushingState>()
        val connectionStateChangedNotifications = arrayListOf<ConnectionState>()

        override fun notifyBrushingStateChange(state: BrushingState) {
            super.notifyBrushingStateChange(state)
            stateChangedNotifications.add(state)
        }

        override fun notifyConnectionStateChange(state: ConnectionState) {
            super.notifyConnectionStateChange(state)
            connectionStateChangedNotifications.add(state)
        }
    }
}
