/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.interactor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.google.common.base.Optional
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.kolibree.android.app.ui.activity.BaseGameActivity
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.interactor.LifecycleAwareInteractor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.error.callSafely
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import timber.log.Timber

typealias ToothbrushMacGetter = () -> String?

/**
 * Base interactor for games. Encapsulates logic previously stored in [BaseGameActivity].
 * Notifies about changes in toothbrush connection state and vibration.
 *
 * @author lookashc
 * @see [BaseGameActivity]
 * @date 28/03/19
 */
@Keep
@SuppressWarnings("TooManyFunctions") // TODO remove this once compat API will be removed
@GameScope
class GameInteractor @Inject constructor(
    private val kolibreeServiceInteractor: KolibreeServiceInteractor,
    @ToothbrushMac preFilledToothbrushMac: Optional<String>
) : LifecycleAwareInteractor<GameInteractor.Listener>(),
    VibratorListener, ConnectionStateListener, KolibreeServiceInteractor.Listener {

    /**
     * Public interface
     */
    @Keep
    interface Listener : KolibreeServiceInteractor.Listener {

        /**
         * Called when connection was established
         */
        fun onConnectionEstablished()

        /**
         * Called when connection state changed
         * @param connection toothbrush connection
         * @param newState new state of the connection
         */
        fun onConnectionStateChanged(connection: KLTBConnection, newState: KLTBConnectionState)

        /**
         * Notifies that vibration are on for particular connection
         * @param connection toothbrush connection
         * @throws FailureReason
         */
        @Throws(FailureReason::class)
        fun onVibratorOn(connection: KLTBConnection)

        /**
         * Notifies that vibration are off for particular connection
         * @param connection toothbrush connection
         */
        fun onVibratorOff(connection: KLTBConnection)
    }

    var connection: KLTBConnection? = null
        @VisibleForTesting set

    var toothbrushMac: String? = null
        private set

    @Deprecated("should not use it anymore")
    var toothbrushMacGetter: ToothbrushMacGetter? = null

    @VisibleForTesting
    val macSubject: Relay<String> = PublishRelay.create()

    @VisibleForTesting
    val vibratorOn = AtomicReference(false)

    @VisibleForTesting
    val connectionWasLost = AtomicReference(false)

    private val service: KolibreeService?
        get() = kolibreeServiceInteractor.service

    @VisibleForTesting
    val isDestroyed = AtomicBoolean(false)

    var shouldProceedWithVibrationDelegate: () -> Boolean = { isDestroyed.get().not() }

    init {
        @SuppressLint("ExperimentalClassUse")
        toothbrushMac = preFilledToothbrushMac.orNull()
    }

    override fun onCreateInternal(savedInstanceState: Bundle?) {
        super.onCreateInternal(savedInstanceState)
        kolibreeServiceInteractor.addListener(this)
        maybeRegisterToMainConnection()
        maybeRegisterToAllConnections()
    }

    override fun onDestroyInternal() {
        unregisterFromAllConnections()
        unregisterFromMainConnection()
        kolibreeServiceInteractor.removeListener(this)
        isDestroyed.set(true)
        super.onDestroyInternal()
    }

    override fun setLifecycleOwnerInternal(lifecycleOwner: LifecycleOwner) {
        // Because we have composition of interactors, we need to pass the same owner to
        // kolibreeServiceInteractor - and we need to do that before setting our own,
        // so kolibreeServiceInteractor's create() will call first.
        kolibreeServiceInteractor.setLifecycleOwner(lifecycleOwner)
        super.setLifecycleOwnerInternal(lifecycleOwner)
    }

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        toothbrushMac?.let {
            onToothbrushMacReceived(it)
        } ?: run {
            if (service.knownConnections.size == 1) {
                onToothbrushMacReceived(service.knownConnections[0].toothbrush().mac)
            } else {
                maybeRegisterToAllConnections()
            }
        }
        forEachListener { listener -> listener.onKolibreeServiceConnected(service) }

        service.knownConnections
            .firstOrNull { weCareAboutThisConnection(it) }
            ?.let {
                onVibratorStateChanged(it, it.vibrator().isOn)
            }
    }

    override fun onKolibreeServiceDisconnected() {
        Timber.w("onKolibreeServiceDisconnected")
        unregisterFromAllConnections()
        unregisterFromMainConnection()
        connection = null
        forEachListener { listener -> listener.onKolibreeServiceDisconnected() }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
        Timber.v(
            "onVibratorStateChanged for %s is vibrating %s. Previous connection is %s. vibratorOn is %s. connection was lost %s",
            connection.toothbrush().mac,
            on,
            connection.mac(),
            vibratorOn.get(),
            connectionWasLost.get()
        )

        if (shouldProceedWithVibrationDelegate()) {
            if (this.connection == null && on) {
                onToothbrushMacReceived(connection.toothbrush().mac)
            }

            if (weCareAboutThisConnection(connection) && shouldInvokeListenerOnNewVibratorState(on)) {
                if (on) {
                    forEachListener { listener -> callSafely { listener.onVibratorOn(connection) } }
                } else {
                    forEachListener { listener -> callSafely { listener.onVibratorOff(connection) } }
                }
            }
        }
    }

    private fun shouldInvokeListenerOnNewVibratorState(on: Boolean) =
        connectionWasLost.get() || vibratorOn.compareAndSet(!on, on)

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        if (newState != KLTBConnectionState.ACTIVE) connectionWasLost.set(true)

        Timber.v("onConnectionStateChanged $connection, connection $newState")
        forEachListener { listener -> listener.onConnectionStateChanged(connection, newState) }
    }

    fun getToothbrushMacSingle(): Single<String> {
        val macSingle: Single<String> = toothbrushMacGetter?.invoke()?.let {
            Single.just(it)
        } ?: macSubject.take(1).singleOrError()

        return macSingle.doOnSuccess { mac -> toothbrushMac = mac }
    }

    @VisibleForTesting
    fun weCareAboutThisConnection(connection: KLTBConnection): Boolean =
        toothbrushMac != null && toothbrushMac.equals(connection.toothbrush().mac)

    @VisibleForTesting
    fun onToothbrushMacReceived(toothbrushMac: String) {
        service?.let {
            connection = it.getConnection(toothbrushMac)
            maybeRegisterToMainConnection()

            this.toothbrushMac = toothbrushMac
            Timber.v("onToothbrushMacReceived $toothbrushMac, connection $connection in $this")

            macSubject.accept(toothbrushMac)

            unregisterFromAllConnections(exceptThisMac = toothbrushMac)

            forEachListener { listener -> listener.onConnectionEstablished() }
        }
    }

    // TODO revise games to check if this method can be private after further refactor
    fun maybeRegisterToMainConnection() {
        Timber.v("maybeRegisterToMainConnection $connection")
        connection?.let {
            it.state().register(this)
            it.vibrator().register(this)
        }
    }

    @VisibleForTesting
    fun maybeRegisterToAllConnections() {
        Timber.v("maybeRegisterToAllConnections")
        service?.knownConnections?.forEach { knownConnection ->
            knownConnection.state().register(this)
            knownConnection.vibrator().register(this)
            knownConnection.mac()?.let {
                if (connection == null && knownConnection.vibrator().isOn) {
                    onToothbrushMacReceived(it)
                }
            }
        }
    }

    // TODO revise games to check if this method can be private after further refactor
    fun unregisterFromMainConnection() {
        Timber.v("unregisterFromMainConnection")
        connection?.let {
            it.state().unregister(this)
            unregisterAsVibratorListenerFromMainConnection()
        }
    }

    private fun unregisterAsVibratorListenerFromMainConnection() {
        connection?.vibrator()?.unregister(this)

        vibratorOn.set(false)
    }

    @VisibleForTesting
    fun unregisterFromAllConnections(exceptThisMac: String? = null) {
        Timber.v("unregisterFromAllConnections")
        service?.knownConnections?.forEach { knownConnection ->
            if (knownConnection.toothbrush().mac != exceptThisMac) {
                knownConnection.state().unregister(this)
                knownConnection.vibrator().unregister(this)
            }
        }
    }

    override fun onStopInternal() {
        super.onStopInternal()

        unregisterAsVibratorListenerFromMainConnection()
    }

    override fun onStartInternal() {
        super.onStartInternal()

        registerAsVibratorListenerFromMainConnection()
    }

    private fun registerAsVibratorListenerFromMainConnection() {
        connection?.vibrator()?.register(this)
    }

    @Deprecated(
        "This is kept for temporary compatibility and will be removed",
        ReplaceWith("nothing"),
        DeprecationLevel.WARNING
    )
    @Keep
    fun resetToothbrushConnection() {
        Timber.v("resetToothbrushConnection in $this")
        toothbrushMac = null
        connection = null
    }
}
