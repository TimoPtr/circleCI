package com.kolibree.android.sdk.connection.toothbrush

import androidx.annotation.Keep
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.battery.Battery
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.kml.MouthZone16
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration

/**
 * Created by aurelien on 10/08/17.
 *
 * Kolibree toothbrush interface
 */
@Suppress("TooManyFunctions")
@Keep
interface Toothbrush {

    /**
     * Get the toothbrush bluetooth name
     *
     * @return bluetooth device name
     */
    fun getName(): String

    /**
     * Set the toothbrush bluetooth name
     *
     * @param name non null name
     * @return non null [Completable]
     */
    fun setAndCacheName(name: String): Completable

    /**
     * Set the toothbrush name in the App
     *
     * @param name non null name
     */
    fun cacheName(name: String)

    /**
     * Get toothbrush bluetooth mac address
     *
     * @return non null String encoded mac address
     */
    val mac: String

    /**
     * Get toothbrush model
     *
     * @return non null [ToothbrushModel]
     */
    val model: ToothbrushModel

    /**
     * Get the toothbrush hardware version
     *
     * @return non null [HardwareVersion]
     */
    val hardwareVersion: HardwareVersion

    /**
     * Get the toothbrush serial number
     *
     * @return non null serial number
     */
    val serialNumber: String

    /**
     * Get the firmware version
     *
     * @return non null [SoftwareVersion]
     */
    val firmwareVersion: SoftwareVersion

    /**
     * Get the bootloader version
     *
     * @return non null [SoftwareVersion]
     */
    val bootloaderVersion: SoftwareVersion

    /**
     * Get the dsp version
     *
     * @return non null [DspVersion]
     */
    val dspVersion: DspVersion

    /**
     * Check if the firmware is running in bootloader mode
     *
     * @return true if running in bootloader mode, false otherwise
     */
    val isRunningBootloader: Boolean

    /**
     * Get the toothbrush battery interface
     *
     * @return [Battery]
     */
    fun battery(): Battery

    /**
     * Write an update over the air
     *
     * @param update non null [AvailableUpdate]
     * @return non null [OtaUpdateEvent] [Observable]
     */
    fun update(update: AvailableUpdate): Observable<OtaUpdateEvent>

    /**
     * Play LED signal
     *
     * @param red red intensity [0, 100]
     * @param green green intensity [0, 100]
     * @param blue blue intensity [0, 100]
     * @param pattern non null [LedPattern]
     * @param period period in millis
     * @param duration duration in millis
     * @return non null [Completable]
     */
    fun playLedSignal(
        red: Byte,
        green: Byte,
        blue: Byte,
        pattern: LedPattern,
        period: Int,
        duration: Int
    ): Completable

    /**
     * Play Mode LEDs pattern
     *
     * Set a test pattern for mode LEDs. A different PWM can be chosen for each LED, and a duration
     * for which the pattern will be displayed.
     * PWM values can be from 0 (LED off) to 100 (LED fully on).
     *
     * Please note: the sum of all PWMs should less or equal to 100.
     *
     * @param pwmLed0 LED 0 PWM value in percents [Int]
     * @param pwmLed1 LED 1 PWM value in percents [Int]
     * @param pwmLed2 LED 2 PWM value in percents [Int]
     * @param pwmLed3 LED 3 PWM value in percents [Int]
     * @param pwmLed4 LED 4 PWM value in percents [Int]
     * @param patternDuration Pattern duration [Duration]
     * @return [Completable]
     */
    fun playModeLedPattern(
        pwmLed0: Int = 0,
        pwmLed1: Int = 0,
        pwmLed2: Int = 0,
        pwmLed3: Int = 0,
        pwmLed4: Int = 0,
        patternDuration: Duration
    ): Completable

    /**
     * Set the duty cycle (PWM) of a [SpecialLed]
     *
     * Only compatible with Glint. Other devices will emit a
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     *
     * @param led [SpecialLed]
     * @param pwm [0, 255] PWM [Int]
     * @return [Completable]
     */
    fun setSpecialLedPwm(led: SpecialLed, pwm: Int): Completable

    fun getSpecialLedPwm(led: SpecialLed): Single<Int>

    fun calibrateAccelerometerAndGyrometer(): Single<Boolean>

    /**
     * Set the zone on the toothbrush and give the id of the Sequence
     */
    fun setSupervisedMouthZone(zone: MouthZone16, sequenceId: Byte): Single<Boolean>

    /**
     * Send a ping to the toothbrush
     */
    fun ping(): Completable

    /**
     * Get the toothbrush's DSP state (information about firmware, pushable flash file, etc...)
     *
     * will emit a CommandNotSupportedException on non DSP-enabled devices
     *
     * @return [DspState] [Single]
     */
    fun dspState(): Single<DspState>

    /**
     * @return true if connection to the given toothbrush is allowed
     *
     * In some case we want to prevent automatic reconnection
     */
    fun isConnectionAllowed(): Boolean = true

    /**
     * Send switchOffDevice command to the toothbrush with a given mode
     */
    fun switchOffDevice(
        mode: SwitchOffMode
    ): Completable
}
