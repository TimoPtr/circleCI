package com.kolibree.android.sdk.core.toothbrush

import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import io.reactivex.Completable

/**
 * Common interface for [Toothbrush] implementations
 */
internal interface ToothbrushImplementation : Toothbrush {

    /**
     * Internally set the toothbrush serial number
     *
     * @param serialNumber non null [String] 19 bytes long at most
     */
    fun setSerialNumber(serialNumber: String)

    /**
     * Set the BLE advertising intervals for 2nd generation toothbrushes
     *
     * Compatibles models are 2nd generation ones (Ara and Connect E1)
     *
     * Setting an interval to 0ms will make the device use its default value
     *
     * @param fastModeIntervalMs [Long] Fast mode advertising delay in millis
     * 0L will reset the interval to the default value.
     * @param slowModeIntervalMs [Long] Slow mode advertising delay in millis
     * 0L will reset the interval to the default value.
     * @return [Completable] that will emit success if the command has been executed, or an error
     * otherwise. Will emit a CommandNotSupportedException on all non 2nd generation devices
     */
    fun setAdvertisingIntervals(fastModeIntervalMs: Long, slowModeIntervalMs: Long): Completable
}
