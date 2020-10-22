package com.kolibree.android.sdk.core

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.notification.ListenerPool
import com.kolibree.android.sdk.core.notification.UniqueListenerPool
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import timber.log.Timber

/**
 * Created by aurelien on 10/08/17.
 *
 * [ConnectionState] implementation
 */

internal class ConnectionStateImpl
@VisibleForTesting
constructor(
    eventsSource: KLTBConnection,
    private val listenerPool: ListenerPool<ConnectionStateListener>,
    private val mainHandler: Handler
) : ConnectionState, DataCache {

    constructor(
        eventsSource: KLTBConnection,
        mainHandler: Handler = Handler(Looper.getMainLooper())
    ) : this(
        eventsSource = eventsSource,
        listenerPool = UniqueListenerPool<ConnectionStateListener>(
            "connection state",
            true
        ),
        mainHandler = mainHandler
    )

    /**
     * Current state
     */
    @VisibleForTesting
    val state: AtomicReference<KLTBConnectionState> = AtomicReference(KLTBConnectionState.NEW)

    private val stateRelay = BehaviorRelay.createDefault(state.get())

    /**
     * Event source
     */
    private val source: WeakReference<KLTBConnection> = WeakReference(eventsSource)

    override val current: KLTBConnectionState
        get() = state.get()

    override fun register(l: ConnectionStateListener) {
        val sizePreAdd = listenerPool.size()
        val sizePostAdd = listenerPool.add(l)

        if (sizePostAdd > sizePreAdd) {
            /*
            If we notify on the same thread, we provoke an ANR
             */
            listenerPool.notifyListeners { listener ->
                if (listener === l) notifyListener(listener, current)
            }
        }
    }

    override fun unregister(l: ConnectionStateListener) {
        listenerPool.remove(l)
    }

    override fun clearCache() {
        if (state.get() !== KLTBConnectionState.OTA) {
            state.set(KLTBConnectionState.NEW)
        }
    }

    override val stateStream: Flowable<KLTBConnectionState>
        get() = stateRelay.hide().toFlowable(BackpressureStrategy.LATEST)

    /**
     * Set the connection state
     *
     * @param newState non null [KLTBConnectionState]
     */
    fun set(newState: KLTBConnectionState) {
        state.set(newState)
        mainHandler.post {
            stateRelay.accept(newState)
        }

        listenerPool.notifyListeners { listener ->
            notifyListener(listener, newState)
        }
    }

    private fun notifyListener(
        listener: ConnectionStateListener,
        newState: KLTBConnectionState
    ) {
        source.get()?.let { connection ->
            Timber.tag(TAG).d("Notifying $newState to listener: $listener")
            listener.onConnectionStateChanged(connection, newState)
        } ?: Timber.w("Connection source in is null %s", this)
    }

    companion object {
        private val TAG =
            bluetoothTagFor(ConnectionStateImpl::class)
    }
}
