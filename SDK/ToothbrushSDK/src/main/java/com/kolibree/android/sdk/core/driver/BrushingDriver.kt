package com.kolibree.android.sdk.core.driver

import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.error.BadRecordException
import com.kolibree.android.sdk.error.FailureReason
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by aurelien on 10/08/17.
 *
 *
 * Brushing commands driver
 */
internal interface BrushingDriver {
    /** Mark brushing as monitored.  */
    fun monitorCurrentBrushing(): Completable?

    /**
     * Get next record
     *
     * @return nullable [OfflineBrushing]
     * @throws FailureReason if something wrong happened, [BadRecordException] if the record is
     * corrupted
     */
    @get:Throws(BadRecordException::class, FailureReason::class)
    val nextRecord: OfflineBrushing?

    /**
     * Delete "next record" (must have been read before)
     *
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun deleteNextRecord(): Completable?

    /**
     * Get remaining stored record count
     *
     * @return Single that will emit remaining stored brushing count
     */
    val remainingRecordCount: Single<Int?>?
    fun startExtractFileSession(): Completable?
    fun finishExtractFileSession(): Completable?
    /**
     * Get default brushing duration
     *
     * @return int duration in seconds
     * @throws FailureReason if something wrong happened
     */
    /**
     * Set default brushing duration
     *
     * @param time default brushing duration in seconds
     * @throws FailureReason if something wrong happened
     */
    @get:Throws(FailureReason::class)
    @set:Throws(FailureReason::class)
    var defaultBrushingDuration: Int
}
