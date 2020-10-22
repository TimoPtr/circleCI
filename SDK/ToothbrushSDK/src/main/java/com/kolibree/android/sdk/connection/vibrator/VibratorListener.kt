package com.kolibree.android.sdk.connection.vibrator

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection

/**
 * Created by aurelien on 19/12/16.
 *
 * Toothbrush vibrator callback
 */

@Keep
interface VibratorListener {

    /**
     * Called on the main thread when the vibrator starts or stops
     *
     * @param connection non null [KLTBConnection] event source
     * @param on true if the toothbrush is vibrating, false otherwise
     */
    fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean)
}
