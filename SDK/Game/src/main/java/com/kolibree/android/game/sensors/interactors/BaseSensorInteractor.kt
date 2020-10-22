/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.game.lifecycle.GameLifecycle
import com.kolibree.android.game.lifecycle.GameLifecycle.Background
import com.kolibree.android.game.lifecycle.GameLifecycle.Foreground
import com.kolibree.android.game.lifecycle.GameLifecycle.Resumed
import com.kolibree.android.game.lifecycle.GameLifecycle.Started
import com.kolibree.android.game.lifecycle.GameLifecycle.Terminated
import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionActive
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionLost
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.VibratorOn
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.utils.callSafely
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * Base helper class that a [ToothbrushInteractor] can extend and delegate [GameLifecycle] and
 * [GameToothbrushEvent] subscription
 *
 * It notifies descendants when to register/unregister to sensor listeners.
 *
 * It serves an important role in reducing the number of commands sent to the toothbrush by
 * keeping track of whether we need to reenable sensors on [onConnectionActive]. Before, we wrote
 * to sensor control characteristics every time the connection was active, which caused connection
 * drop on some toothbrushes, especially Ara/E1 2.4 and 2.5
 */
internal abstract class BaseSensorInteractor(
    brushingStageProvider: GameLifecycleProvider,
    brushingToothbrushEventProvider: GameToothbrushEventProvider
) : ToothbrushInteractor {
    @VisibleForTesting
    protected var lifecycleState: GameLifecycle? = null

    @VisibleForTesting
    protected val disposables = CompositeDisposable()

    @VisibleForTesting
    lateinit var connection: KLTBConnection

    protected fun isPlaying(): Boolean {
        return lifecycleState?.let {
            return@let it == Started || it == Resumed
        } ?: false
    }

    init {
        disposables.addSafely(
            brushingStageProvider.gameLifecycleStream()
                .subscribe(
                    // capture all exceptions to avoid stream disposal
                    { callSafely { onGameLifecycleTransition(it) } },
                    Timber::e
                )
        )

        disposables.addSafely(
            brushingToothbrushEventProvider.connectionEventStream()
                .subscribe(
                    // capture all exceptions to avoid stream disposal
                    { callSafely { onToothbrushEvent(it) } },
                    Timber::e
                )
        )
    }

    @CallSuper
    protected open fun onGameLifecycleTransition(newLifecycleState: GameLifecycle) {
        this.lifecycleState = newLifecycleState

        when (newLifecycleState) {
            Terminated -> {
                disposables.dispose()

                unregisterListeners()
            }
            Background -> unregisterListeners()
            Foreground -> registerListeners()
            else -> {
                // no-op
            }
        }
    }

    @CallSuper
    protected open fun onToothbrushEvent(gameToothbrushEvent: GameToothbrushEvent) {
        connection = gameToothbrushEvent.connection

        when (gameToothbrushEvent) {
            is ConnectionActive -> onConnectionActive()
            is VibratorOn -> onVibratorOn()
            is ConnectionLost -> onConnectionLost()
        }
    }

    @VisibleForTesting
    fun onVibratorOn() {
        registerDelayableListeners()
    }

    fun onConnectionActive() {
        registerListeners()
    }

    @CallSuper
    open fun onConnectionLost() {
        unregisterListeners()
    }

    protected fun addDisposable(disposable: Disposable) {
        disposables.addSafely(disposable)
    }

    abstract fun registerListeners()

    abstract fun unregisterListeners()

    /**
     * Register to listeners which data we only need while the toothbrush is vibrating
     */
    abstract fun registerDelayableListeners()
}
