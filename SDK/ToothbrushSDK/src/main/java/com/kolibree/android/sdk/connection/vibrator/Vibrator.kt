package com.kolibree.android.sdk.connection.vibrator

import androidx.annotation.Keep
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Created by aurelien on 09/08/17.
 *
 * Toothbrush vibrator interface
 */

@Keep
interface Vibrator {

    /**
     * Get the vibrator current state
     *
     * @return true if the vibrator is on, false otherwise
     */
    val isOn: Boolean

    /**
     * Register to vibrator state events
     *
     * @param l non null [VibratorListener]
     */
    fun register(l: VibratorListener)

    /**
     * Unregister from vibrator state events
     *
     * @param l non null [VibratorListener]
     */
    fun unregister(l: VibratorListener)

    /**
     * Start vibration
     *
     * @return non null [Completable]
     */
    fun on(): Completable

    /**
     * Stop vibration
     *
     * @return non null [Completable]
     */
    fun off(): Completable

    /**
     * Stop vibration and stop offline brushing recording
     *
     * @return non null [Completable]
     */
    fun offAndStopRecording(): Completable

    /**
     * Set Ara toothbrush vibration level
     *
     * This method will do nothing on other toothbrush generations
     *
     * @param percents integer value in percents [0, 100]
     */
    fun setLevel(percents: Int): Completable

    /**
     * This stream emits the vibrator state (on = true, off = false) on main thread, does not complete and does not emit
     * any errors
     * and start with the current state on each subscription if the connection is active.
     *
     * It can send multiple time the same values
     */
    val vibratorStream: Flowable<Boolean>
}
