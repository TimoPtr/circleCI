/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.kolibree.android.app.lifecycle.LifecycleDisposableScope
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.game.sensors.interactors.MonitorCurrentBrushingInteractor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.math.Axis
import com.kolibree.game.middleware.BrushingState
import com.kolibree.game.middleware.ConnectionState
import com.kolibree.game.middleware.DataCallback
import com.kolibree.game.middleware.DoubleVector
import com.kolibree.game.middleware.RawData
import com.kolibree.game.middleware.ReconnectionCallback
import com.kolibree.game.middleware.ToothbrushInteractor
import com.kolibree.game.middleware.ToothbrushVersion
import io.reactivex.Scheduler
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import timber.log.Timber

typealias MiddlewareToothbrushModel = com.kolibree.game.middleware.ToothbrushModel

/**
 * WARNING : This class is open for test purpose please don't extend it in the actual code
 */
@SuppressLint("ExperimentalClassUse")
@Suppress("TooManyFunctions")
internal open class ToothbrushInteractorImpl @Inject constructor(
    @VisibleForTesting val lifecycleDisposableScopeOwner: LifecycleDisposableScopeOwner,
    private val connection: KLTBConnection,
    lifecycle: Lifecycle,
    private val callbackScheduler: Scheduler
) : ToothbrushInteractor(
    connection.state().current.toMiddlewareType(),
    connection.getBrushingState()
),
    LifecycleDisposableScope by lifecycleDisposableScopeOwner,
    ConnectionStateListener, VibratorListener, RawDetectorListener, LifecycleObserver {

    @Inject
    lateinit var monitorCurrentBrushingInteractor: MonitorCurrentBrushingInteractor

    @VisibleForTesting
    val isReconnecting = AtomicBoolean(false)

    @VisibleForTesting
    val rawDataEnabled = AtomicBoolean(false)

    @VisibleForTesting
    val rawDataCallback = AtomicReference<DataCallback>()

    @VisibleForTesting
    val reconnectionCallback = AtomicReference<ReconnectionCallback>()

    init {
        lifecycleDisposableScopeOwner.monitoredClassName = javaClass.simpleName
        lifecycle.addObserver(this)
    }

    override fun setOnReconnectionCallback(callback: ReconnectionCallback?) =
        checkNotNull(callback)
            .let { reconnectionCallback.set(it) }

    override fun clearReconnectionCallback() {
        reconnectionCallback.set(null)
    }

    override fun start() {
        disposeOnDestroy {
            connection.vibrator()
                .on()
                .observeOn(callbackScheduler)
                .doOnSubscribe { notifyBrushingStateChange(BrushingState.STARTING) }
                // TODO maybe send old state when error ? https://kolibree.atlassian.net/browse/KLTB002-9810
                .subscribe({}, Timber::e)
        }
    }

    override fun stop() {
        disposeOnDestroy {
            connection.vibrator().off().observeOn(callbackScheduler)
                .doOnSubscribe { notifyBrushingStateChange(BrushingState.STOPPING) }
                .subscribe({}, Timber::e)
        }
    }

    override fun enableRawData(callBacks: DataCallback?) {
        rawDataEnabled.set(true)
        rawDataCallback.set(checkNotNull(callBacks))
        registerRawData()
    }

    override fun disableRawData() {
        rawDataEnabled.set(false)
        connection.detectors().disableRawDataNotifications()
        connection.detectors().rawData().unregister(this)
        rawDataCallback.set(null)
    }

    override fun getToothbrushModel(): MiddlewareToothbrushModel =
        connection
            .toothbrush()
            .model
            .toMiddlewareType()

    override fun getSerial(): String = connection.toothbrush().getName()

    override fun getMacAddress(): String = connection.toothbrush().mac

    override fun getToothbrushVersion(): ToothbrushVersion = ToothbrushVersion(
        connection.toothbrush().hardwareVersion.toString(),
        connection.toothbrush().firmwareVersion.toString()
    )

    override fun getCalibration(): DoubleVector =
        DoubleVector(connection.detectors().calibrationData.map(Float::toDouble))

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) = newState.let {
        notifyConnectionStateChange(it.toMiddlewareType())

        if (it == KLTBConnectionState.TERMINATED) {
            isReconnecting.set(true)
        } else if (it == KLTBConnectionState.ACTIVE &&
            isReconnecting.compareAndSet(true, false)
        ) {
            if (rawDataEnabled.get()) {
                // when a disconnection occur we need enable back the raw data notification
                registerRawData()
            }
            reconnectionCallback.get()?.onReconnection()
        }
    }

    override fun onVibratorStateChanged(
        connection: KLTBConnection,
        on: Boolean
    ) {
        if (on) {
            notifyBrushingStateChange(BrushingState.STARTED)
        } else {
            notifyBrushingStateChange(BrushingState.STOPPED)
        }
    }

    override fun onRawData(
        source: KLTBConnection,
        sensorState: RawSensorState
    ) = rawDataCallback
        .get()
        .onRawData(sensorState.toMiddlewareType())

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        connection.state().register(this)
        connection.vibrator().register(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        connection.detectors().rawData().unregister(this)
        connection.vibrator().unregister(this)
        connection.state().unregister(this)
    }

    private fun registerRawData() {
        connection.detectors().rawData().register(this)
        connection.detectors().enableRawDataNotifications()
    }
}

private fun ToothbrushModel.toMiddlewareType(): MiddlewareToothbrushModel = when (this) {
    ARA -> MiddlewareToothbrushModel.ARA
    CONNECT_M1 -> MiddlewareToothbrushModel.CONNECT_M1
    CONNECT_E1 -> MiddlewareToothbrushModel.CONNECT_E1
    CONNECT_E2 -> MiddlewareToothbrushModel.CONNECT_E2
    CONNECT_B1 -> MiddlewareToothbrushModel.CONNECT_B1
    PLAQLESS -> MiddlewareToothbrushModel.PLAQLESS
    HILINK -> MiddlewareToothbrushModel.HILINK
    HUM_ELECTRIC -> MiddlewareToothbrushModel.HUM_ELECTRIC
    HUM_BATTERY -> MiddlewareToothbrushModel.HUM_BATTERY
    GLINT -> MiddlewareToothbrushModel.GLINT
}

@VisibleForTesting
internal fun KLTBConnectionState.toMiddlewareType(): ConnectionState =
    when (this) {
        KLTBConnectionState.ACTIVE,
        KLTBConnectionState.OTA -> ConnectionState.CONNECTED
        KLTBConnectionState.NEW,
        KLTBConnectionState.ESTABLISHING -> ConnectionState.CONNECTING
        KLTBConnectionState.TERMINATING,
        KLTBConnectionState.TERMINATED -> ConnectionState.DISCONNECTED
    }

@VisibleForTesting
internal fun RawSensorState.toMiddlewareType(): RawData =
    RawData(
        RawSensorState.convertRawTimestamp(timestamp),
        gyroscope.get(Axis.X),
        gyroscope.get(Axis.Y),
        gyroscope.get(Axis.Z),
        acceleration.get(Axis.X),
        acceleration.get(Axis.Y),
        acceleration.get(Axis.Z)
    )

@VisibleForTesting
internal fun KLTBConnection.getBrushingState(): BrushingState =
    if (vibrator().isOn) BrushingState.STARTED else BrushingState.STOPPED
