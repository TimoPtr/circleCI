package com.kolibree.android.sdk.core.toothbrush

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.SwitchOffMode
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.battery.Battery
import com.kolibree.android.sdk.connection.toothbrush.battery.BatteryImplFactory
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.CommandSet
import com.kolibree.android.sdk.core.driver.ble.ParameterSet
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_MODE_LED_PATTERN
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_SPECIAL_LED_CONTROL
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.Duration

/**
 * BLE [Toothbrush] implementation
 *
 * @param mac non null toothbrush mac address
 * @param model non null device model
 * @param driver non null BleDriver
 * @param toothbrushName non null toothbrush bluetooth toothbrushName
 */
internal abstract class ToothbrushBaseBleImpl(
    mac: String,
    model: ToothbrushModel,
    protected val driver: BleDriver,
    toothbrushName: String
) : ToothbrushBaseImpl(mac, model, toothbrushName) {

    private val battery: Battery = BatteryImplFactory().createBatteryImplementation(model, driver)

    override val firmwareVersion: SoftwareVersion
        get() = driver.getFirmwareVersion()

    override val isRunningBootloader: Boolean
        get() = driver.isRunningBootloader

    override val hardwareVersion: HardwareVersion
        get() = driver.getHardwareVersion()

    override val bootloaderVersion: SoftwareVersion
        get() = driver.getBootloaderVersion()

    override val dspVersion: DspVersion
        get() = driver.getDspVersion()

    override fun playLedSignal(
        red: Byte,
        green: Byte,
        blue: Byte,
        pattern: LedPattern,
        period: Int,
        duration: Int
    ): Completable = if (!supportsLedPlaySignal()) {
        Completable.complete()
    } else Completable.create { emitter ->
        try {
            driver.sendCommand(
                CommandSet.playLedSignal(red, green, blue, pattern, period, duration)
            )

            emitter.onComplete()
        } catch (failureReason: FailureReason) {
            emitter.tryOnError(failureReason)
        }
    }

    @VisibleForTesting
    fun supportsLedPlaySignal(): Boolean =
        !isRunningBootloader && firmwareVersion.isNewerOrSame(minFwSupportingPlayLed())

    override fun calibrateAccelerometerAndGyrometer(): Single<Boolean> = Single.create { emitter ->
        try {
            emitter.onSuccess(
                driver.setDeviceParameter(
                    ParameterSet.calibrateAccelerometerAndGyrometerParameterPayload()
                )
            )
        } catch (failureReason: FailureReason) {
            emitter.tryOnError(failureReason)
        }
    }

    override fun battery(): Battery = battery

    @Throws(Exception::class)
    public override fun setToothbrushName(name: String) {
        driver.setDeviceParameter(ParameterSet.setToothbrushNameParameterPayload(name))
    }

    protected abstract fun minFwSupportingPlayLed(): SoftwareVersion

    override fun ping(): Completable {
        if (isRunningBootloader) {
            return Completable.complete()
        }

        return Completable.create { emitter ->
            try {
                driver.setDeviceParameter(CommandSet.ping())

                if (!emitter.isDisposed)
                    emitter.onComplete()
            } catch (failureReason: FailureReason) {
                emitter.tryOnError(failureReason)
            }
        }
    }

    @Suppress("MagicNumber", "LongMethod")
    final override fun playModeLedPattern(
        pwmLed0: Int,
        pwmLed1: Int,
        pwmLed2: Int,
        pwmLed3: Int,
        pwmLed4: Int,
        patternDuration: Duration
    ): Completable = when {
        !model.hasModeLed -> Completable.error(CommandNotSupportedException())

        pwmLed0 + pwmLed1 + pwmLed2 + pwmLed3 + pwmLed4 !in 0..100 ->
            Completable.error(IllegalArgumentException("The sum of all PWMs must be in [0, 100]"))

        else -> driver.setAndGetDeviceParameterOnce(
            PayloadWriter(8)
                .writeByte(DEVICE_PARAMETERS_MODE_LED_PATTERN)
                .writeByte(pwmLed0.toByte())
                .writeByte(pwmLed1.toByte())
                .writeByte(pwmLed2.toByte())
                .writeByte(pwmLed3.toByte())
                .writeByte(pwmLed4.toByte())
                .writeUnsignedInt16(patternDuration.toMillis().toInt())
                .bytes
        ).ignoreElement()
    }

    // https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit?pli=1#gid=506526620
    @Suppress("MagicNumber")
    final override fun setSpecialLedPwm(led: SpecialLed, pwm: Int): Completable = when {
        !model.isGlint -> Completable.error(CommandNotSupportedException())
        pwm !in 0..255 -> Completable.error(IllegalArgumentException("PWM must be in [0, 255]"))
        else -> driver.setAndGetDeviceParameterOnce(
            PayloadWriter(3)
                .writeByte(DEVICE_PARAMETERS_SPECIAL_LED_CONTROL)
                .writeByte(getSpecialLedBleIndex(led))
                .writeUnsignedInt8(pwm.toShort())
                .bytes
        ).ignoreElement()
    }

    @Suppress("MagicNumber")
    final override fun getSpecialLedPwm(led: SpecialLed): Single<Int> = when {
        !model.isGlint -> Single.error(CommandNotSupportedException())
        else -> Single.fromCallable {
            driver.getDeviceParameter(
                PayloadWriter(2)
                    .writeByte(DEVICE_PARAMETERS_SPECIAL_LED_CONTROL)
                    .writeByte(getSpecialLedBleIndex(led))
                    .bytes
            ).skip(3).readInt8().toInt()
        }
    }

    @VisibleForTesting
    fun getSpecialLedBleIndex(led: SpecialLed): Byte = when (led) {
        SpecialLed.WarningLed -> 0x00
        SpecialLed.StrengthLedNominal -> 0x01
        SpecialLed.StrengthLedLow -> 0x02
    }

    override fun switchOffDevice(
        mode: SwitchOffMode
    ): Completable =
        driver.setAndGetDeviceParameterOnce(
            CommandSet.switchOffDevice(mode)
        ).flatMapCompletable(::onSwitchOffResponse)

    private fun onSwitchOffResponse(reader: PayloadReader): Completable {
        reader.skip(1) // skip commandId
        return if (reader.length == 2) {
            when (reader.readInt8()) {
                UNSUPPORTED_SWITCH_OFF -> Completable.error(IllegalArgumentException("unsupported switch-off mode"))
                MALFORMED_SWITCH_OFF_COMMAND -> Completable.error(IllegalArgumentException("malformed command"))
                UNKNOWN_ERROR_SWITCH_OFF_COMMAND -> Completable.error(IllegalStateException("unknown error"))
                else -> Completable.complete()
            }
        } else {
            Completable.complete()
        }
    }
}

@VisibleForTesting
internal const val UNSUPPORTED_SWITCH_OFF = 0xfd.toByte()

@VisibleForTesting
internal const val MALFORMED_SWITCH_OFF_COMMAND = 0xfe.toByte()

@VisibleForTesting
internal const val UNKNOWN_ERROR_SWITCH_OFF_COMMAND = 0xff.toByte()
