package com.kolibree.android.sdk.connection.detectors

import androidx.annotation.Keep

/**
 * Created by aurelien on 10/08/17.
 *
 * Toothbrush movement detector interface
 *
 * @param <T> Movement detector listener type
</T> */

@Keep
internal interface Detector<T> {

    /**
     * Register a listener to get notified on toothbrush detector data changes
     *
     * @param listener non null listener instance
     */
    fun register(listener: T)

    /**
     * Unregister from detector events
     *
     * @param listener non null listener instance
     */
    fun unregister(listener: T)
}
