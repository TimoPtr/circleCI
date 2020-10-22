package com.kolibree.android.sdk.connection.parameters

import androidx.annotation.Keep
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime

/**
 * Created by aurelien on 10/08/17.
 *
 * Toothbrush parameters interface
 */

@Keep
interface Parameters {

    /**
     * Get owner device
     *
     * @return non null [Long] invoker
     */
    val ownerDevice: Single<Long>

    /**
     * Let implementations get toothbrush time
     *
     * @return non null [OffsetDateTime] invoker
     */
    val time: Single<OffsetDateTime>

    /**
     * Get auto shutdown timeout
     *
     * @return non null [Integer] invoker
     */
    val autoShutdownTimeout: Single<Int>

    /**
     * Set owner device ID
     *
     * @param ownerDevice long ID
     * @return non null [Completable]
     */
    fun setOwnerDevice(ownerDevice: Long): Completable

    /**
     * Set auto shutdown timeout
     *
     * @param autoShutdownTimeout auto shutdown timeout in seconds
     * @return non null [Completable]
     */
    fun setAutoShutdownTimeout(autoShutdownTimeout: Int): Completable
}
