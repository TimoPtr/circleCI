package com.kolibree.android.sdk.wrapper

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import io.reactivex.Observable

/**
 * Thread safety strategy for this class is to lock the instance when R/W on state
 */
data class ConnectionStateWrapperImpl(val connectionState: ConnectionState) :
    ConnectionStateWrapper {

    private val publisher: BehaviorRelay<Boolean> = BehaviorRelay.create()
    private val statusObservable = publisher.replay(1).autoConnect()
    private var state = KLTBConnectionState.NEW

    init {
        connectionState.register(object : ConnectionStateListener {
            override fun onConnectionStateChanged(
                connection: KLTBConnection,
                newState: KLTBConnectionState
            ) {
                checkStateConnexion(newState)
            }
        })
    }

    /**
     * publish True is the connection is registered, false otherwise
     */
    private fun checkStateConnexion(newState: KLTBConnectionState) {
        synchronized(this) {
            state = newState
        }

        when (newState) {
            ACTIVE -> publisher.accept(true)
            else -> publisher.accept(false)
        }
    }

    /**
     * Get the current state of the connection
     *
     * @return non null [KLTBConnectionState]
     */
    override fun getCurrent(): KLTBConnectionState {
        return connectionState.current
    }

    /**
     * Return true if connected to toothbrush connection, false otherwise
     */
    override fun isActive(): Boolean {
        synchronized(this) {
            return state == ACTIVE
        }
    }

    /**
     * check if the connection is registered or not
     */
    override fun isRegistered(): Observable<Boolean> {
        return statusObservable.distinctUntilChanged()
    }
}
