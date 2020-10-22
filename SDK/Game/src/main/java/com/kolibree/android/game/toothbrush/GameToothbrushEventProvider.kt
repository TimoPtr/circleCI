/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.toothbrush

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.game.GameScope
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Exposes the events of the [KLTBConnection] performing the Brushing Session
 */
@Keep
@GameScope
class GameToothbrushEventProvider @Inject constructor(
    private val connection: KLTBConnection,
    lifecycle: Lifecycle
) : DefaultLifecycleObserver,
    VibratorListener,
    ConnectionStateListener {
    init {
        lifecycle.addObserver(this)
    }

    private val connectionEventRelay = PublishRelay.create<GameToothbrushEvent>()

    /**
     * Observable that will emit [GameToothbrushEvent] by the brushing session [KLTBConnection]
     *
     * It will not emit until there's a [KLTBConnection]
     *
     * It won't emit the same [GameToothbrushEvent] two times in a row
     *
     * Events will be emitted on MainThread
     */
    fun connectionEventStream(): Observable<GameToothbrushEvent> = connectionEventRelay
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .hide()

    fun onConnectionEstablished() {
        emitEvent(GameToothbrushEvent.ConnectionEstablished(connection))
    }

    override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
        if (on) {
            onVibratorOn()
        } else {
            onVibratorOff()
        }
    }

    @VisibleForTesting
    fun onVibratorOn() {
        emitEvent(GameToothbrushEvent.VibratorOn(connection))
    }

    @VisibleForTesting
    fun onVibratorOff() {
        emitEvent(GameToothbrushEvent.VibratorOff(connection))
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        if (newState == ACTIVE) {
            onConnectionActive()
        } else {
            onConnectionLost()
        }
    }

    @VisibleForTesting
    fun onConnectionActive() {
        emitEvent(GameToothbrushEvent.ConnectionActive(connection))

        if (connection.vibrator().isOn) {
            onVibratorOn()
        }
    }

    @VisibleForTesting
    fun onConnectionLost() {
        emitEvent(GameToothbrushEvent.ConnectionLost(connection))
    }

    private fun emitEvent(event: GameToothbrushEvent) {
        connectionEventRelay.accept(event)
    }

    override fun onStart(owner: LifecycleOwner) {
        connection.apply {
            vibrator().register(this@GameToothbrushEventProvider)
            state().register(this@GameToothbrushEventProvider)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        connection.apply {
            if (vibrator().isOn) {
                delegateSubscribe(vibrator().off().onTerminateDetach(), Timber::e)
            }

            vibrator().unregister(this@GameToothbrushEventProvider)
            state().unregister(this@GameToothbrushEventProvider)
        }
    }
}
