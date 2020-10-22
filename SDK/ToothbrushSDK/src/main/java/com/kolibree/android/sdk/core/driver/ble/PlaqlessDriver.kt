/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.toPlaqlessRawSensorData
import com.kolibree.android.sdk.connection.detectors.data.toPlaqlessSensorData
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_DETECTOR_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_IMU_CHAR
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.error.callSafely
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import java.util.concurrent.atomic.AtomicReference

/*
open for Testing :(
 */
internal open class PlaqlessDriver : KolibreeBleDriver {

    constructor(context: Context, mac: String, listener: KLTBDriverListener) :
        super(context, mac, listener)

    @VisibleForTesting
    constructor(
        bleManager: KLNordicBleManager,
        listener: KLTBDriverListener,
        bluetoothScheduler: Scheduler,
        mac: String,
        notificationCaster: CharacteristicNotificationStreamer,
        notifyListenerScheduler: Scheduler
    ) : super(
        bleManager,
        listener,
        bluetoothScheduler,
        mac,
        notificationCaster,
        notifyListenerScheduler
    )

    override fun calibrationDataSize(): Int = AraDriver.CALIBRATION_DATA_SIZE

    private val plaqlessRawDataFlowable = AtomicReference<Flowable<PlaqlessRawSensorState>>()
    private val plaqlessSensorFlowable = AtomicReference<Flowable<PlaqlessSensorState>>()

    @VisibleForTesting
    val plaqlessRingLedStateRelay: Relay<PlaqlessRingLedState> = PublishRelay.create()

    override fun onDeviceParameterNotification(commandId: Byte, payloadReader: PayloadReader) {
        super.onDeviceParameterNotification(commandId, payloadReader)
        when (commandId) {
            GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE -> {
                plaqlessRingLedStateRelay.accept(PlaqlessRingLedState.create(payloadReader))
            }
        }
    }

    override fun supportsReadingBootloader(): Boolean =
        super.supportsReadingBootloader() &&
            getFirmwareVersion().isNewerOrSame(READ_BOOTLOADER_SUPPORTED_FW)

    override fun plaqlessRawDataNotifications(): Flowable<PlaqlessRawSensorState> {
        val notificationFlowable = characteristicNotificationFlowable(
            PLAQLESS_IMU_CHAR,
            onSubscribeBlock = { bleManager.enablePlaqlessRawDataNotifications() },
            onCancelBlock = {
                if (plaqlessRawDataFlowable.get() != null) {
                    callSafely { bleManager.disablePlaqlessRawDataNotifications() }

                    plaqlessRawDataFlowable.set(null)
                }
            })
            .map { notificationData -> notificationData.toPlaqlessRawSensorData() }

        plaqlessRawDataFlowable.compareAndSet(null, notificationFlowable)

        return plaqlessRawDataFlowable.get()
    }

    override fun plaqlessNotifications(): Flowable<PlaqlessSensorState> {
        val notificationFlowable = characteristicNotificationFlowable(
            PLAQLESS_DETECTOR_CHAR,
            onSubscribeBlock = { bleManager.enablePlaqlessNotifications() },
            onCancelBlock = {
                if (plaqlessSensorFlowable.get() != null) {
                    callSafely { bleManager.disablePlaqlessNotifications() }

                    plaqlessSensorFlowable.set(null)
                }
            })
            .map { notificationData -> notificationData.toPlaqlessSensorData() }

        plaqlessSensorFlowable.compareAndSet(null, notificationFlowable)

        return plaqlessSensorFlowable.get()
    }

    override fun supportsGRUData(): Boolean = false

    override fun plaqlessRingLedState(): Flowable<PlaqlessRingLedState> =
        Flowable.merge(
            Flowable.fromCallable { getPlaqlessRingState() },
            plaqlessRingLedStateRelay.toFlowable(BackpressureStrategy.LATEST)
        )

    @VisibleForTesting
    fun getPlaqlessRingState(): PlaqlessRingLedState {
        val result =
            setAndGetDeviceParameter(byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE))
        result.skip(1)
        return PlaqlessRingLedState.create(result)
    }

    private inline fun characteristicNotificationFlowable(
        characteristic: GattCharacteristic,
        crossinline onSubscribeBlock: () -> Unit,
        crossinline onCancelBlock: () -> Unit
    ): Flowable<ByteArray> = bleNotificationMulticaster().characteristicStream(
        characteristic,
        onSubscribeBlock = {
            synchronized(bleManager) {
                onSubscribeBlock.invoke()
            }
        },
        onCancelBlock = {
            synchronized(bleManager) {
                onCancelBlock.invoke()
            }
        }
    )
        .share()

    override fun fileType(): FileType = FileType.PLAQLESS

    override fun toothbrushModel(): ToothbrushModel = ToothbrushModel.PLAQLESS

    override fun supportsBrushingEventsPolling(): Boolean =
        getFirmwareVersion().isNewerOrSame(POLL_BRUSHING_EVENTS_FW)

    override fun overpressureStateFlowable(): Flowable<OverpressureState> =
        Flowable.error(CommandNotSupportedException())
}

private val READ_BOOTLOADER_SUPPORTED_FW = SoftwareVersion(2, 0, 0)
private val POLL_BRUSHING_EVENTS_FW = SoftwareVersion(2, 0, 10)
