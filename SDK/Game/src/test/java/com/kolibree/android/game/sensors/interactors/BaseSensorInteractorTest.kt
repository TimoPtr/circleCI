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
import com.kolibree.android.game.lifecycle.GameLifecycle.Paused
import com.kolibree.android.game.lifecycle.GameLifecycle.Resumed
import com.kolibree.android.game.lifecycle.GameLifecycle.Started
import com.kolibree.android.game.lifecycle.GameLifecycle.Terminated
import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.KLTBConnection
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class BaseSensorInteractorTest :
    BaseSensorInteractorTestHelper<TestBrushingSensorManager>() {

    /*
    Init
     */

    @Test
    fun `init starts with stage null`() {
        initSensorInteractor()

        assertNull(sensorInteractor.getGameLifecycle())
    }

    @Test
    fun `init subscribes to GameToothbrushEvent`() {
        val subject = PublishSubject.create<GameToothbrushEvent>()
        initSensorInteractor(gameToothbrushEventObservable = subject)

        assertTrue(subject.hasObservers())

        val expectedConnection = mock<KLTBConnection>()
        val expectedEvent = GameToothbrushEvent.ConnectionLost(expectedConnection)
        subject.onNext(expectedEvent)

        /*
        We can't verify onToothbrushEvent
         */
        assertEquals(expectedConnection, sensorInteractor.getTestConnection())
    }

    @Test
    fun `init subscribes to GameLifecycle`() {
        val subject = PublishSubject.create<GameLifecycle>()
        initSensorInteractor(gameLifecycleObservable = subject)

        assertTrue(subject.hasObservers())

        assertNull(sensorInteractor.getGameLifecycle())

        val expectedEvent = Paused
        subject.onNext(expectedEvent)

        /*
        We can't verify onToothbrushEvent
         */
        assertEquals(expectedEvent, sensorInteractor.getGameLifecycle())
    }

    /*
    isPlaying
     */

    @Test
    fun `isPlaying returns false if stage is null`() {
        initSensorInteractor()

        assertNull(sensorInteractor.getGameLifecycle())

        assertFalse(sensorInteractor.getTestIsPlaying())
    }

    @Test
    fun `isPlaying returns true if stage is Started`() {
        initSensorInteractor()

        sensorInteractor.setGameLifecycle(Started)

        assertTrue(sensorInteractor.getTestIsPlaying())
    }

    @Test
    fun `isPlaying returns true if stage is Resumed`() {
        initSensorInteractor()

        sensorInteractor.setGameLifecycle(Resumed)

        assertTrue(sensorInteractor.getTestIsPlaying())
    }

    @Test
    fun `isPlaying returns false if stage is nor Resumed nor Started`() {
        initSensorInteractor()

        gameLifecycleInstances()
            .filterNot { it == Resumed }
            .filterNot { it == Started }
            .forEach {
                sensorInteractor.setGameLifecycle(it)

                assertFalse("False for $it", sensorInteractor.getTestIsPlaying())
            }
    }

    /*
    onGameLifecycleTransition
     */

    @Test
    fun `onGameLifecycleTransition stores stage`() {
        spySensorInteractor()

        assertNull(sensorInteractor.getGameLifecycle())

        val expectedStage = Terminated
        sensorInteractor.onGameLifecycleTransition(expectedStage)

        assertEquals(expectedStage, sensorInteractor.getGameLifecycle())
    }

    @Test
    fun `onGameLifecycleTransition replaces stage`() {
        spySensorInteractor()

        val previousStage = GameLifecycle.Restarted
        sensorInteractor.setGameLifecycle(previousStage)

        val expectedStage = Terminated
        sensorInteractor.onGameLifecycleTransition(expectedStage)

        assertEquals(expectedStage, sensorInteractor.getGameLifecycle())
    }

    @Test
    fun `onGameLifecycleTransition invokes unregisterListeners if event is Terminated`() {
        spySensorInteractor()

        sensorInteractor.onGameLifecycleTransition(Terminated)

        verify(sensorInteractor).unregisterListeners()
    }

    @Test
    fun `onGameLifecycleTransition disposes subscription to providers`() {
        val toothbrushEventSubject = PublishSubject.create<GameToothbrushEvent>()
        val gameLifecycleSubject = PublishSubject.create<GameLifecycle>()
        initSensorInteractor(
            gameToothbrushEventObservable = toothbrushEventSubject,
            gameLifecycleObservable = gameLifecycleSubject
        )

        assertTrue(toothbrushEventSubject.hasObservers())
        assertTrue(gameLifecycleSubject.hasObservers())

        sensorInteractor.onGameLifecycleTransition(Terminated)

        assertFalse(toothbrushEventSubject.hasObservers())
        assertFalse(gameLifecycleSubject.hasObservers())
    }

    @Test
    fun `onGameLifecycleTransition disposes disposables if event is Terminated`() {
        initSensorInteractor()

        assertFalse(sensorInteractor.getTestDisposables().isDisposed)

        sensorInteractor.onGameLifecycleTransition(Terminated)

        assertTrue(sensorInteractor.getTestDisposables().isDisposed)
    }

    /*
    onVibratorOn
     */

    @Test
    fun `onVibratorOn invokes registerDelayableListeners`() {
        spySensorInteractor()

        sensorInteractor.onVibratorOn()

        verify(sensorInteractor).registerDelayableListeners()
    }

    /*
    onConnectionActive
     */

    @Test
    fun `onConnectionActive invokes registerListeners`() {
        spySensorInteractor()

        sensorInteractor.onConnectionActive()

        verify(sensorInteractor).registerListeners()
    }

    /*
    onToothbrushEvent
     */

    @Test
    fun `onConnectionLost invokes unregisterListeners`() {
        spySensorInteractor()

        sensorInteractor.onConnectionLost()

        verify(sensorInteractor).unregisterListeners()
    }

    @Test
    fun `onToothbrushEvent stores connection`() {
        spySensorInteractor()

        var unitinialized = false
        try {
            sensorInteractor.getTestConnection()
        } catch (e: UninitializedPropertyAccessException) {
            // ignore
            unitinialized = true
        }
        assertTrue(unitinialized)

        val expectedConnection = mock<KLTBConnection>()
        sensorInteractor.onToothbrushEvent(GameToothbrushEvent.VibratorOff(expectedConnection))

        assertEquals(expectedConnection, sensorInteractor.getTestConnection())
    }

    @Test
    fun `onToothbrushEvent ConnectionEstablished does nothing`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).registerListeners()

        val onConnectionEstablished = GameToothbrushEvent.ConnectionEstablished(mock())
        sensorInteractor.onToothbrushEvent(onConnectionEstablished)

        verify(sensorInteractor).onToothbrushEvent(onConnectionEstablished)
        verifyNoMoreInteractions(sensorInteractor)
    }

    @Test
    fun `onToothbrushEvent ConnectionActive invokes onConnectionActive`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).onConnectionActive()

        sensorInteractor.onToothbrushEvent(GameToothbrushEvent.ConnectionActive(mock()))

        verify(sensorInteractor).onConnectionActive()
    }

    @Test
    fun `onToothbrushEvent ConnectionLost invokes onConnectionLost`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).onConnectionLost()

        sensorInteractor.onToothbrushEvent(GameToothbrushEvent.ConnectionLost(mock()))

        verify(sensorInteractor).onConnectionLost()
    }

    @Test
    fun `onToothbrushEvent VibratorOff does nothing`() {
        spySensorInteractor()

        sensorInteractor.onToothbrushEvent(GameToothbrushEvent.VibratorOff(mock()))

        verify(sensorInteractor).onToothbrushEvent(any())

        verifyNoMoreInteractions(sensorInteractor)
    }

    @Test
    fun `onToothbrushEvent VibratorOn invokes registerDelayableListeners`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).registerDelayableListeners()

        sensorInteractor.onToothbrushEvent(GameToothbrushEvent.VibratorOn(mock()))

        verify(sensorInteractor).registerDelayableListeners()
    }

    /*
    utils
     */

    override fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ): TestBrushingSensorManager {
        return TestBrushingSensorManager(
            brushingToothbrushEventProvider = brushingEventProvider,
            brushingStageProvider = stageProvider
        )
    }
}

internal class TestBrushingSensorManager(
    brushingStageProvider: GameLifecycleProvider,
    brushingToothbrushEventProvider: GameToothbrushEventProvider
) : BaseSensorInteractor(brushingStageProvider, brushingToothbrushEventProvider) {

    fun getTestConnection(): KLTBConnection? = connection

    fun getTestIsPlaying() = isPlaying()

    fun setGameLifecycle(brushingStageEvent: GameLifecycle?) {
        lifecycleState = brushingStageEvent
    }

    fun getGameLifecycle() = lifecycleState

    override fun registerListeners() {
        // no-op
    }

    override fun unregisterListeners() {
        // no-op
    }

    override fun registerDelayableListeners() {
        // no-op
    }

    public override fun onToothbrushEvent(gameToothbrushEvent: GameToothbrushEvent) {
        super.onToothbrushEvent(gameToothbrushEvent)
    }

    public override fun onGameLifecycleTransition(newLifecycleState: GameLifecycle) {
        super.onGameLifecycleTransition(newLifecycleState)
    }

    fun getTestDisposables() = disposables
}
