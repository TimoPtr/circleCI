package com.kolibree.android.sdk.connection.state

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection

/**
 * Created by aurelien on 04/11/16.
 *
 * Implement this interface to get notified on Kolibree toothbrush connection state changes
 */

@Keep
interface ConnectionStateListener {

    /**
     * Callback called when connection state changes
     *
     * Called in UI thread
     *
     * @param connection the event source [KLTBConnection]
     * @param newState the new connection state
     */
    fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    )
}
