package com.kolibree.android.sdk.core.driver

import com.kolibree.android.sdk.error.AirplaneModeBugException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Completable

/**
 * Created by aurelien on 16/08/17.
 *
 *
 * Bluetooth device driver interface
 */
internal interface DeviceDriver {
    /**
     * Connect to a Kolibree toothbrush
     *
     * Blocking call
     *
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class, AirplaneModeBugException::class)
    fun connect()

    /**
     * Connect to a 3rd generation toothbrush in bootloader mode
     *
     * Blocking call
     *
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class, AirplaneModeBugException::class)
    fun connectDfuBootloader()

    /**
     * Disconnect the connected Kolibree toothbrush
     *
     * Blocking call
     *
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun disconnect()

    /**
     * Disconnect and then attempts to connect to the connected Kolibree toothbrush
     *
     * @return Completable that will complete on success, error on failure
     */
    fun reconnect(): Completable

    /**
     * Get serial number
     *
     * @return non null [String]
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun getSerialNumber(): String

    /**
     * Set the toothbrush internal time to now
     *
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun setTime()

    /**
     * Check if the driver has valid GRU data for RNN detector
     *
     * @return true if the GRU data is present and valid, false otherwise
     */
    fun hasValidGruData(): Boolean

    /**
     * Get Gru data version
     *
     * @return non null GRU data [SoftwareVersion] (Major.minor.revision)
     */
    val gruDataVersion: SoftwareVersion

    /**
     * Check if the driver supports GRU data for RNN detector
     *
     * @return true if the GRU data is supported, false otherwise
     */
    fun supportsGRUData(): Boolean

    /**
     * Disable MultiUser mode This should be done for legacy Toothbrush where this is enabled by
     * default and when this option is enabled offline brushing are not saved
     */
    @Throws(FailureReason::class)
    fun disableMultiUserMode()

    fun supportsBrushingEventsPolling(): Boolean
}
