package com.kolibree.android.sdk.core.driver.ble

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.VibratorMode
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.math.Axis
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler

/**
 * Connect M1 driver implementation
 */
// the version one)
// open for testing :-( See https://discuss.kotlinlang.org/t/how-to-test-protected-function/6133/17
internal open class CM1Driver : KolibreeBleDriver {
    constructor(
        context: Context,
        mac: String,
        listener: KLTBDriverListener
    ) : super(context, mac, listener)

    @VisibleForTesting
    internal constructor(
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

    override fun setVibratorMode(vibratorMode: VibratorMode): Completable = Completable.complete()

    override fun calibrationDataSize(): Int = CALIBRATION_DATA_SIZE

    @Throws(Exception::class)
    override fun loadSensorCalibration() {
        super.loadSensorCalibration()
        val accelerometerOffsetsVector = bleManager
            .getDeviceParameter(byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_ACCELEROMETER_OFFSETS))
            .skip(1)
            .readVector()
        sensorCalibration[12] = accelerometerOffsetsVector[Axis.X]
        sensorCalibration[13] = accelerometerOffsetsVector[Axis.Y]
        sensorCalibration[14] = accelerometerOffsetsVector[Axis.Z]
        rawDataFactory().setAccelerometerOffset(accelerometerOffsetsVector)
        val gyroscopeOffsetVector = bleManager
            .getDeviceParameter(byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_GYROMETER_OFFSETS))
            .skip(1)
            .readVector()
        sensorCalibration[15] = gyroscopeOffsetVector[Axis.X]
        sensorCalibration[16] = gyroscopeOffsetVector[Axis.Y]
        sensorCalibration[17] = gyroscopeOffsetVector[Axis.Z]
        rawDataFactory().setGyroscopeOffset(gyroscopeOffsetVector)
    }

    public override fun toothbrushModel(): ToothbrushModel = ToothbrushModel.CONNECT_M1

    override fun supportsReadingBootloader(): Boolean =
        super.supportsReadingBootloader() &&
            getFirmwareVersion().isNewerOrSame(READ_BOOTLOADER_SUPPORTED_FW)

    override fun supportsGRUData(): Boolean =
        !getFirmwareVersion().isNewerOrSame(RNN_NOT_SUPPORTED_FW)

    override fun supportsBrushingEventsPolling(): Boolean =
        getFirmwareVersion().isNewerOrSame(POLL_BRUSHING_EVENTS_FW)

    override fun overpressureStateFlowable(): Flowable<OverpressureState> =
        Flowable.error(CommandNotSupportedException())
}

private const val CALIBRATION_DATA_SIZE = 18
private val RNN_NOT_SUPPORTED_FW = SoftwareVersion(2, 0, 0)
private val READ_BOOTLOADER_SUPPORTED_FW = SoftwareVersion(1, 4, 1)
private val POLL_BRUSHING_EVENTS_FW = SoftwareVersion(1, 6, 3)
