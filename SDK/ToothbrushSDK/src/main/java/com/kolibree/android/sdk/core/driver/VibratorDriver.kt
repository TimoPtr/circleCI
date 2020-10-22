package com.kolibree.android.sdk.core.driver

import com.kolibree.android.sdk.error.FailureReason
import io.reactivex.Completable

/**
 * Created by aurelien on 03/08/17.
 *
 * VibratorInterfaceImpl interface
 *
 * To be implemented by toothbrush drivers
 */
internal interface VibratorDriver {

    /**
     * Set vibrator mode
     *
     * Will do nothing on Connect M1 toothbrushes
     *
     * @param vibratorMode
     * @throws FailureReason if the command could not be sent
     */
    fun setVibratorMode(vibratorMode: VibratorMode): Completable

    /**
     * Set Ara toothbrush vibration level
     *
     * This method will do nothing on other toothbrush generations
     *
     * @param percents integer value in percents [0, 100]
     * @throws FailureReason if something wrong happened
     */
    @Throws(FailureReason::class)
    fun setVibrationLevel(percents: Int)
}
