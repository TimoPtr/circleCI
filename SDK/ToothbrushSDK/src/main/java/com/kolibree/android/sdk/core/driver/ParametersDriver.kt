package com.kolibree.android.sdk.core.driver

import com.kolibree.android.sdk.error.FailureReason
import org.threeten.bp.OffsetDateTime

/**
 * Created by aurelien on 10/08/17.
 *
 *
 * Toothbrush parameters driver interface
 */
internal interface ParametersDriver {
    /**
     * Get toothbrush time
     *
     * @return non null [OffsetDateTime]
     * @throws FailureReason if the command could not be executed
     */
    @Throws(FailureReason::class)
    fun getTime(): OffsetDateTime

    /**
     * Set auto reconnect timeout
     *
     * @param timeout timeout
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun setAutoReconnectTimeout(timeout: Int)

    /**
     * Set/Get the toothbrush owner device ID
     *
     * @return long owner device ID
     * @param ownerDevice long owner device ID
     * @throws FailureReason if something wrong happened
     */
    @get:Throws(FailureReason::class)
    @set:Throws(FailureReason::class)
    var ownerDevice: Long

    /**
     * Get auto shutdown timeout
     *
     * @return auto shutdown timeout in seconds
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun getAutoShutdownTimeout(): Int

    /**
     * Set auto shutdown timeout
     *
     * @param autoShutdownTimeout auto shutdown timeout in seconds
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun setAutoShutdownTimeout(autoShutdownTimeout: Int)
}
