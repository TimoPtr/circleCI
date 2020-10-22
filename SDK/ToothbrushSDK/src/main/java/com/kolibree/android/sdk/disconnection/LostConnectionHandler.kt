/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.disconnection

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import javax.inject.Inject

@SuppressLint("DeobfuscatedPublicSdkClass")
interface LostConnectionHandler {

    enum class State {
        CONNECTION_LOST,
        CONNECTING,
        CONNECTION_ACTIVE
    }

    fun connectionObservable(toothbrushMac: String): Observable<State>
}

internal class LostConnectionHandlerImpl @Inject constructor(
    val serviceProvider: ServiceProvider
) : LostConnectionHandler {

    @VisibleForTesting
    var previousBtStateActivated: KLTBConnectionState = KLTBConnectionState.ACTIVE

    @VisibleForTesting
    val currentStatePublishSubject = PublishRelay.create<State>()

    @VisibleForTesting
    var registeredState: WeakReference<ConnectionState?> = WeakReference(null)

    override fun connectionObservable(
        toothbrushMac: String
    ): Observable<State> = serviceProvider
        .connectStream()
        .subscribeOn(Schedulers.io())
        .switchMap { serviceResult ->
            onServiceStateChanged(serviceResult, toothbrushMac)
            currentStatePublishSubject.hide()
        }
        .doOnDispose { unregisterConnectionState() }

    @VisibleForTesting
    fun onServiceStateChanged(
        serviceResult: ServiceProvisionResult,
        toothbrushMac: String
    ) {
        when (serviceResult) {
            is ServiceConnected -> registerConnectionState(serviceResult.service, toothbrushMac)
            is ServiceDisconnected -> unregisterConnectionState()
        }
    }

    @VisibleForTesting
    fun registerConnectionState(service: KolibreeService, toothbrushMac: String) {
        val connection = service.getConnection(toothbrushMac)
        if (connection != null) {
            registeredState = WeakReference(connection.state())
            registeredState.get()?.register(connectionStateListener)
        }
    }

    @VisibleForTesting
    val connectionStateListener = object : ConnectionStateListener {
        override fun onConnectionStateChanged(
            connection: KLTBConnection,
            newState: KLTBConnectionState
        ) {
            stateChanged(newState)
        }
    }

    @VisibleForTesting
    fun stateChanged(newState: KLTBConnectionState) {
        when {
            checkIfTheConnectionIsLost(newState) -> {
                currentStatePublishSubject.accept(State.CONNECTION_LOST)
            }
            checkIfTheConnectionHasBeenLostAndItsConnecting(newState) -> {
                currentStatePublishSubject.accept(State.CONNECTING)
            }
            checkIfTheConnectionIsNowActive(newState) -> {
                currentStatePublishSubject.accept(State.CONNECTION_ACTIVE)
            }
        }
        previousBtStateActivated = newState
    }

    @VisibleForTesting
    fun unregisterConnectionState() {
        registeredState.get()?.unregister(connectionStateListener)
    }

    @VisibleForTesting
    fun checkIfTheConnectionIsLost(newState: KLTBConnectionState) =
        newState == KLTBConnectionState.TERMINATED

    @VisibleForTesting
    fun checkIfTheConnectionHasBeenLostAndItsConnecting(newState: KLTBConnectionState) =
        previousBtStateActivated == KLTBConnectionState.TERMINATED &&
            newState == KLTBConnectionState.ESTABLISHING

    @VisibleForTesting
    fun checkIfTheConnectionIsNowActive(newState: KLTBConnectionState) =
        previousBtStateActivated != KLTBConnectionState.ACTIVE &&
            newState == KLTBConnectionState.ACTIVE
}
