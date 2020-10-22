/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import com.kolibree.android.game.lifecycle.GameLifecycle
import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal abstract class BaseSensorInteractorTestHelper<T : BaseSensorInteractor> {
    protected lateinit var sensorInteractor: T

    private lateinit var brushingEventSubject: PublishSubject<GameToothbrushEvent>
    private lateinit var gameLifecycleSubject: PublishSubject<GameLifecycle>

    /*
    utils
     */
    protected fun initSensorInteractor(
        gameToothbrushEventObservable: PublishSubject<GameToothbrushEvent> = PublishSubject.create(),
        gameLifecycleObservable: PublishSubject<GameLifecycle> = PublishSubject.create(),
        connection: KLTBConnection? = null
    ) {
        this.brushingEventSubject = gameToothbrushEventObservable
        this.gameLifecycleSubject = gameLifecycleObservable

        val gameToothbrushEventProvider =
            mockGameToothbrushEventProvider(
                gameToothbrushEventObservable
            )
        val gameLifecycleProvider =
            mockGameLifecycleProvider(
                gameLifecycleObservable
            )

        sensorInteractor = createInstance(gameToothbrushEventProvider, gameLifecycleProvider)

        connection?.let { setConnection(it) }
    }

    protected fun spySensorInteractor(
        gameToothbrushEventObservable: PublishSubject<GameToothbrushEvent> = PublishSubject.create(),
        gameLifecycleObservable: PublishSubject<GameLifecycle> = PublishSubject.create(),
        connection: KLTBConnection? = null
    ) {
        initSensorInteractor(gameToothbrushEventObservable, gameLifecycleObservable, connection)

        sensorInteractor = spy(sensorInteractor)
    }

    protected fun setConnection(connecton: KLTBConnection) {
        sensorInteractor.connection = connecton
    }

    protected fun setIsPlaying(isPlaying: Boolean) {
        val gameLifecycleStatus = if (isPlaying) GameLifecycle.Started else GameLifecycle.Paused
        gameLifecycleSubject.onNext(gameLifecycleStatus)
    }

    protected fun emitGameLifecycleEvent(gameLifecycle: GameLifecycle) {
        gameLifecycleSubject.onNext(gameLifecycle)
    }

    abstract fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ): T
}

internal fun mockGameLifecycleProvider(stageObservable: Observable<GameLifecycle> = Observable.empty()): GameLifecycleProvider {
    return object : GameLifecycleProvider {
        override fun gameLifecycleStream() = stageObservable
    }
}

internal fun mockGameToothbrushEventProvider(eventObservable: Observable<GameToothbrushEvent> = Observable.empty()): GameToothbrushEventProvider {
    val eventProvider = mock<GameToothbrushEventProvider>()

    whenever(eventProvider.connectionEventStream()).thenReturn(eventObservable)

    return eventProvider
}

fun createSensorTestConnection(state: KLTBConnectionState = KLTBConnectionState.ACTIVE) =
    KLTBConnectionBuilder
        .createAndroidLess()
        .withDetectorsSupport()
        .withState(state)
        .build()

fun gameLifecycleInstances(): List<GameLifecycle> = GameLifecycle.values().toList()
