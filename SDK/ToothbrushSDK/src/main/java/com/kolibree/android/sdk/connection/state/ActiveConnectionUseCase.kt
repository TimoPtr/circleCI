/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.state

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Notifies clients when any [KLTBConnection] enters [KLTBConnectionState.ACTIVE] state
 *
 * Internally, it registers as connection state listener to all known connections and emits a
 * [KLTBConnection] each time it becomes active
 */
@Keep
class ActiveConnectionUseCase @Inject constructor(
    private val serviceProvider: ServiceProvider
) : ConnectionStateListener {

    @VisibleForTesting
    internal val connectionsNotifyingStateChanges = mutableSetOf<WeakReference<KLTBConnection>>()

    @VisibleForTesting
    internal var serviceWeak = WeakReference<KolibreeService>(null)

    private val connectionActiveProcessor = PublishProcessor.create<ConnectionActiveItem>()

    /**
     * @return [Observable] that will emit the [KLTBConnection] each time it becomes active
     */
    fun onConnectionsUpdatedStream(): Flowable<KLTBConnection> {
        return Flowable.mergeArray(
            connectionActiveProcessor,
            registerAsConnectionListener()
        )
            .filter { it is ConnectionIsActive }
            .map { item ->
                (item as ConnectionIsActive).connection
            }
            .doFinally {
                stopListeningToConnectionStates()
            }
    }

    private fun registerAsConnectionListener(): Flowable<ConnectionActiveItem> {
        return readConnectionsAndStoreServiceOnceAndStream()
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .doOnNext { listenToConnectionsState(it) }
            .map { SignalRegisteredAsListener }
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        if (newState == KLTBConnectionState.ACTIVE) {
            notifyConnectionActive(connection)
        }
    }

    private fun notifyConnectionActive(connection: KLTBConnection) {
        connectionActiveProcessor.onNext(ConnectionIsActive(connection))
    }

    /**
     * Emits a [List]<[KLTBConnection]> every time the service connects
     *
     * [KolibreeService] can be destroyed and recreated during a user session, and that will create
     * new instances of [KLTBConnection]. Each time that happens, we need to re-register as state
     * listeners
     */
    private fun readConnectionsAndStoreServiceOnceAndStream(): Flowable<List<KLTBConnection>> {
        return serviceProvider.connectStream()
            .doOnNext {
                onKolibreeServiceStateChanged(it)
            }
            .filter { it is ServiceConnected }
            .toFlowable(BackpressureStrategy.BUFFER)
            .switchMap {
                (it as ServiceConnected).service.knownConnectionsOnceAndStream
            }
    }

    private fun onKolibreeServiceStateChanged(serviceProvisionResult: ServiceProvisionResult) {
        serviceWeak = when (serviceProvisionResult) {
            is ServiceConnected -> WeakReference(serviceProvisionResult.service)
            is ServiceDisconnected -> WeakReference<KolibreeService>(null)
        }
    }

    @VisibleForTesting
    internal fun listenToConnectionsState(connections: List<KLTBConnection>) {
        connections
            .filterNot { areWeListeningToConnection(it) }
            .forEach {
                registerAsConnectionStateListener(it)
            }
    }

    private fun areWeListeningToConnection(connection: KLTBConnection): Boolean {
        return connectionsNotifyingStateChanges
            .mapNotNull { it.get() }
            .any { it.toothbrush().mac == connection.toothbrush().mac }
    }

    @VisibleForTesting
    internal fun registerAsConnectionStateListener(connection: KLTBConnection) {
        connection.state().unregister(this)

        connection.state().register(this)

        connectionsNotifyingStateChanges.add(WeakReference(connection))
    }

    @VisibleForTesting
    internal fun stopListeningToConnectionStates() {
        connectionsNotifyingStateChanges
            .mapNotNull { it.get() }
            .forEach {
                it.state().unregister(this)
            }

        connectionsNotifyingStateChanges.clear()
    }
}

private sealed class ConnectionActiveItem
private data class ConnectionIsActive(val connection: KLTBConnection) :
    ConnectionActiveItem()

private object SignalRegisteredAsListener : ConnectionActiveItem()
