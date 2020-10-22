package com.kolibree.android.sdk.connection.brushing

import androidx.annotation.Keep
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by aurelien on 10/08/17.
 *
 * Brushing data and commands interface
 */

@Keep
interface Brushing {

    /**
     * Get default brushing session duration
     *
     * @return non null [Integer] [Single] seconds
     */
    val defaultDuration: Single<Int>

    /**
     * Get how many stored brushing sessions are available in the toothbrush memory
     *
     * @return non null [Single]
     */
    val recordCount: Single<Int>

    /**
     * Mark brushing as monitored
     *
     * Tells the driver that you handle the outgoing brushing data. The brushing session
     * will not be recorded onto the toothbrush memory
     *
     * @return non null [Completable]
     */
    fun monitorCurrent(): Completable

    /**
     * Set default brushing duration in seconds
     *
     * @param defaultDurationSeconds int seconds
     * @return non null [Completable]
     */
    fun setDefaultDuration(defaultDurationSeconds: Int): Completable

    /**
     * Start stored brushing records retrieval
     *
     * @param l non null [OfflineBrushingConsumer]
     */
    fun pullRecords(l: OfflineBrushingConsumer)
}

@Keep
val DEFAULT_GOAL_DURATION: Duration =
    Duration.of(DEFAULT_BRUSHING_GOAL.toLong(), ChronoUnit.SECONDS)
