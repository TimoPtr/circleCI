package com.kolibree.android.sdk.wrapper

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import io.reactivex.Observable

/**
 *
 * Interface for accessing the connection's [KLTBConnectionState]
 */

@Keep
interface ConnectionStateWrapper {

    /**
     * Get the current state of the connection
     *
     * @return non null [KLTBConnectionState]
     */
    fun getCurrent(): KLTBConnectionState

    /**
     * Return true if connected to toothbrush connection, false otherwise
     */
    fun isActive(): Boolean

    /**
     * check if the connection is registered or not
     */
    fun isRegistered(): Observable<Boolean>
}
