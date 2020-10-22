/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.lifecycle

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.lifecycle.GameLifecycle.Background
import com.kolibree.android.game.lifecycle.GameLifecycle.Finished
import com.kolibree.android.game.lifecycle.GameLifecycle.Foreground
import com.kolibree.android.game.lifecycle.GameLifecycle.Idle
import com.kolibree.android.game.lifecycle.GameLifecycle.Paused
import com.kolibree.android.game.lifecycle.GameLifecycle.Restarted
import com.kolibree.android.game.lifecycle.GameLifecycle.Resumed
import com.kolibree.android.game.lifecycle.GameLifecycle.Started
import com.kolibree.android.game.lifecycle.GameLifecycle.Terminated
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionEstablished
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.ConnectionLost
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.VibratorOff
import com.kolibree.android.game.toothbrush.GameToothbrushEvent.VibratorOn
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

@Keep
interface GameLifecycleCoordinator {
    fun onGameRestarted()

    fun onGameFinished()
}

/**
 * Exposes [GameLifecycle]s and hooks to modify the [GameLifecycle]
 *
 * Once a connection is established, it subscribes itself to the toothbrush events needed to be
 * aware of the state of the brushing session and emits the appropriate BrushingState
 */
@Keep
@GameScope
@Suppress("TooManyFunctions")
class GameLifecycleCoordinatorImpl @Inject constructor(
    lifecycle: Lifecycle,
    toothbrushEventProvider: GameToothbrushEventProvider
) : DefaultLifecycleObserver,
    GameLifecycleProvider,
    GameLifecycleCoordinator {
    init {
        lifecycle.addObserver(this)
    }

    @VisibleForTesting
    var stagePriorToBackground: GameLifecycle? = null

    @VisibleForTesting
    val coachStateRelay = BehaviorRelay.createDefault(Idle)

    @VisibleForTesting
    fun lifecycleState(): GameLifecycle = coachStateRelay.value!!

    /**
     * Observable that will emit [GameLifecycle]
     *
     * It won't emit any event until the Brushing either starts or exits
     *
     * It won't emit the same [GameLifecycle] two times in a row
     *
     * Events will be emitted on MainThread
     */
    override fun gameLifecycleStream(): Observable<GameLifecycle> = coachStateRelay
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .hide()

    private val toothbrushEventsDisposable: Disposable =
        toothbrushEventProvider.connectionEventStream()
            /*
            Events are not data classes, thus we have to avoid duplicates by returning class name
             */
            .distinctUntilChanged { event -> event.javaClass.simpleName }
            .subscribe(
                ::onToothbrushEvent,
                Timber::e
            )

    @VisibleForTesting
    fun onToothbrushEvent(brushingToothbrushEvent: GameToothbrushEvent) {
        when (brushingToothbrushEvent) {
            is ConnectionEstablished -> onConnectionEstablished()
            is ConnectionLost -> onConnectionLost()
            is VibratorOn -> onVibratorOn()
            is VibratorOff -> onVibratorOff()
        }
    }

    @VisibleForTesting
    fun onConnectionEstablished() {
        emitIfPlaying(Paused)
    }

    @VisibleForTesting
    fun onConnectionLost() {
        emitIfPlaying(Paused)
    }

    @VisibleForTesting
    fun onVibratorOn() {
        if (lifecycleState() == Idle || lifecycleState() == Restarted) {
            setBrushingStage(Started)
        } else {
            setBrushingStage(Resumed)
        }
    }

    @VisibleForTesting
    fun onVibratorOff() {
        emitIfPlaying(Paused)
    }

    override fun onGameRestarted() {
        setBrushingStage(Restarted)
    }

    override fun onGameFinished() {
        setBrushingStage(Finished)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        emitIfPlaying(Paused)

        maybeEmitBackground()
    }

    @VisibleForTesting
    fun maybeEmitBackground() {
        when (lifecycleState()) {
            Idle, Paused, Restarted -> {
                stagePriorToBackground = lifecycleState()

                setBrushingStage(Background)
            }

            else -> {
                stagePriorToBackground = null
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        maybeRestoreBeforeOnStopStage()
    }

    @VisibleForTesting
    fun maybeRestoreBeforeOnStopStage() {
        stagePriorToBackground?.let {
            setBrushingStage(Foreground)
            setBrushingStage(it)

            stagePriorToBackground = null
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        toothbrushEventsDisposable.forceDispose()

        setBrushingStage(Terminated)
    }

    @VisibleForTesting
    fun setBrushingStage(newState: GameLifecycle) {
        lifecycleState().validateTransition(newState)
        coachStateRelay.accept(newState)
    }

    private fun emitIfPlaying(@Suppress("SameParameterValue") gameLifecycle: GameLifecycle) {
        if (lifecycleState() == Started || lifecycleState() == Resumed) {
            setBrushingStage(gameLifecycle)
        }
    }
}
