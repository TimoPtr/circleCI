/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.lifecycle.GameLifecycle
import com.kolibree.android.game.lifecycle.GameLifecycleCoordinator
import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.SensorConfiguration
import com.kolibree.android.game.sensors.di.DaggerToothbrushInteractorComponent
import com.kolibree.android.game.sensors.di.ToothbrushInteractorComponent
import com.kolibree.android.game.sensors.di.ToothbrushInteractorModule
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.KLTBConnection
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.CompletableSubject
import javax.inject.Inject
import timber.log.Timber

/**
 * Creates and manages the lifecycle of the [ToothbrushInteractor] needed to run a Coach session
 * for a given [KLTBConnection]
 *
 * [GameLifecycleCoordinator] and [GameToothbrushEventProvider] will be created once the connection
 * is established. Each [ToothbrushInteractor] can then listen to events and react accordingly
 *
 * If it's a manual game, invocations to this class won't have any effect, since its goal is to
 * interact with the Toothbrush
 */
@Keep
@GameScope
class GameToothbrushInteractorFacade constructor(
    private val context: Context,
    private val sensorConfigurationFactory: SensorConfiguration.Factory,
    private val listener: GameSensorListener,
    private val lifecycle: Lifecycle
) {

    @Inject
    lateinit var gameLifecycleCoordinator: GameLifecycleCoordinator

    @VisibleForTesting
    @Inject
    lateinit var gameLifecycleProvider: GameLifecycleProvider

    @Inject
    lateinit var gameToothbrushEventProvider: GameToothbrushEventProvider

    /*
    This is the only way to have all the interactors to be injected into the graph, don't remove
    this line even if the field is unused.
     */
    @Suppress("unused")
    @Inject
    internal lateinit var toothbrushInteractors: Set<@JvmSuppressWildcards ToothbrushInteractor>

    @VisibleForTesting
    internal var toothbrushInteractorComponent: ToothbrushInteractorComponent? = null

    @VisibleForTesting
    internal val initializedSubject = CompletableSubject.create()

    fun gameLifeCycleObservable(): Observable<GameLifecycle> {
        return initializedSubject
            .andThen(Observable.defer {
                gameLifecycleProvider.gameLifecycleStream()
            })
    }

    fun onGameRestarted(): Completable {
        if (initialized()) {
            gameLifecycleCoordinator.onGameRestarted()
        }
        return Completable.complete()
    }

    fun onGameFinished(): Completable {
        return if (initialized()) {
            Completable.fromCallable {
                gameLifecycleCoordinator.onGameFinished()
            }
        } else {
            Completable.complete()
        }
    }

    fun onConnectionEstablished(connection: KLTBConnection) {
        FailEarly.failInConditionMet(
            initializedForDifferentConnection(connection),
            alreadyInvokedErrorMessage(connection)
        )

        if (!initialized()) {
            injectSelf(connection)
            initializedSubject.onComplete()

            gameToothbrushEventProvider.onConnectionEstablished()
        } else {
            Timber.w("facade already initialized for $connection")
        }
    }

    @VisibleForTesting
    fun initialized() = ::gameLifecycleCoordinator.isInitialized

    @VisibleForTesting
    fun initializedForDifferentConnection(newConnection: KLTBConnection): Boolean {
        return toothbrushInteractorComponent?.let {
            it.connection().toothbrush().mac != newConnection.toothbrush().mac
        } ?: false
    }

    private fun alreadyInvokedErrorMessage(newConnection: KLTBConnection) =
        "Already invoked onConnectionEstablished for connection (" +
            "${toothbrushInteractorComponent?.connection()
                .printMac()}). Can't invoke for ${newConnection.printMac()}"

    @VisibleForTesting
    fun injectSelf(connection: KLTBConnection) {
        DaggerToothbrushInteractorComponent.factory().create(
            modelInteractorModule = ToothbrushInteractorModule(
                sensorConfigurationFactory.configurationForConnection(connection)
            ),
            context = context.applicationContext,
            kltbConnection = connection,
            lifecycle = lifecycle,
            gameSensorListener = listener
        ).apply {
            toothbrushInteractorComponent = this

            inject(this@GameToothbrushInteractorFacade)
        }
    }
}

private fun KLTBConnection?.printMac(): String {
    return if (this == null) {
        "null"
    } else {
        toothbrush().mac
    }
}
