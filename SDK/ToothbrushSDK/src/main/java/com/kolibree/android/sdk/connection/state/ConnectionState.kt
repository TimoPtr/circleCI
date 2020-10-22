package com.kolibree.android.sdk.connection.state

import androidx.annotation.Keep
import io.reactivex.Flowable

/**
 * Created by aurelien on 08/08/17.
 *
 * Interface for accessing the connection's [KLTBConnectionState]
 */

@Keep
interface ConnectionState {

    /**
     * Get the current state of the connection
     *
     * @return non null [KLTBConnectionState]
     */
    val current: KLTBConnectionState

    /**
     * Register to toothbrush connection state changes
     *
     * It'll immediately notify the listener of the current state, unless [l] was already registered
     *
     * @param l non null [ConnectionStateListener]
     */
    fun register(l: ConnectionStateListener)

    /**
     * Unregister from toothbrush connection state changes
     *
     * @param l non null [ConnectionStateListener]
     */
    fun unregister(l: ConnectionStateListener)

    /**
     * This stream emits the connection state on main thread, does not complete and does not emit
     * any errors
     */
    val stateStream: Flowable<KLTBConnectionState>
}
