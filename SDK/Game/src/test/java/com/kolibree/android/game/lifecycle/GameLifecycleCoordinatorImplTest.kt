/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.lifecycle.GameLifecycle.Background
import com.kolibree.android.game.lifecycle.GameLifecycle.Finished
import com.kolibree.android.game.lifecycle.GameLifecycle.Foreground
import com.kolibree.android.game.lifecycle.GameLifecycle.Idle
import com.kolibree.android.game.lifecycle.GameLifecycle.Paused
import com.kolibree.android.game.lifecycle.GameLifecycle.Restarted
import com.kolibree.android.game.lifecycle.GameLifecycle.Resumed
import com.kolibree.android.game.lifecycle.GameLifecycle.Started
import com.kolibree.android.game.lifecycle.GameLifecycle.Terminated
import com.kolibree.android.game.sensors.interactors.gameLifecycleInstances
import com.kolibree.android.game.sensors.interactors.mockGameToothbrushEventProvider
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GameLifecycleCoordinatorImplTest : BaseUnitTest() {
    private lateinit var gameToothbrushEventSubject: PublishSubject<GameToothbrushEvent>

    private val lifecycle: Lifecycle = mock()

    private lateinit var gameLifecycleManager: GameLifecycleCoordinatorImpl

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    /*
    init
     */
    @Test
    fun `eventProvider starts with Lifecycle Idle`() {
        initEventProvider()

        gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)
    }

    @Test
    fun `eventProvider registers as lifecycle observer on construction`() {
        initEventProvider()

        verify(lifecycle).addObserver(gameLifecycleManager)
    }

    @Test
    fun `eventProvider subscribes to gameLifecycleManager on creation`() {
        initEventProvider()

        assertTrue(gameToothbrushEventSubject.hasObservers())
    }

    @Test
    fun `new GameToothbrushEvent invokes onToothbrushEvent`() {
        initEventProvider()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)

        gameLifecycleManager.setBrushingStage(Started)

        observable.assertLastValue(Started)

        val expectedEvent = GameToothbrushEvent.VibratorOff(mock())
        gameToothbrushEventSubject.onNext(expectedEvent)

        observable.assertLastValue(Paused)
    }

    @Test
    fun `gameLifecycleStream does not emit duplicates`() {
        initEventProvider()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValueCount(1).assertValue(Idle)

        gameLifecycleManager.coachStateRelay.accept(Idle)

        observable.assertValueCount(1).assertValue(Idle)
    }

    /*
    onStop
     */
    @Test
    fun `onStop emits event Paused if current state is Started`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).maybeEmitBackground()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)

        gameLifecycleManager.setBrushingStage(Started)

        observable.assertLastValue(Started)

        gameLifecycleManager.onStop(mock())

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onStop emits event Paused if current state is Resumed`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).maybeEmitBackground()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)

        gameLifecycleManager.setBrushingStage(Started)
        gameLifecycleManager.setBrushingStage(Paused)
        gameLifecycleManager.setBrushingStage(Resumed)

        observable.assertLastValue(Resumed)

        gameLifecycleManager.onStop(mock())

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onStop invokes maybeEmitBackground`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).maybeEmitBackground()

        gameLifecycleManager.onStop(mock())

        verify(gameLifecycleManager).maybeEmitBackground()
    }

    @Test
    fun `onStop doesn't emit for anything for states different than Started or Paused`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).maybeEmitBackground()

        val lifecycleOwner = mock<LifecycleOwner>()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)

        gameLifecycleInstances()
            .filterNot { it == Resumed }
            .filterNot { it == Started }
            .forEachIndexed { index, newGameLifecycle ->
                setLifecycleState(newGameLifecycle)

                val preOnStopValueCount = observable.valueCount()

                gameLifecycleManager.onStop(lifecycleOwner)

                observable.assertLastValue(newGameLifecycle)

                assertEquals(preOnStopValueCount, observable.valueCount())
            }
    }

    /*
    maybeEmitBackground
     */
    @Test
    fun `maybeEmitBackground emits Background and stores stage if it's Idle, Paused or Restarted `() {
        initEventProvider()

        gameLifecycleInstances()
            .filter { it == Idle || it == Paused || it == Restarted }
            .forEach { stage ->
                val observable =
                    gameLifecycleManager.gameLifecycleStream().test()

                gameLifecycleManager.setBrushingStage(stage)

                observable.assertLastValue(stage)

                gameLifecycleManager.maybeEmitBackground()

                observable.assertLastValue(Background)
                assertEquals(stage, gameLifecycleManager.stagePriorToBackground)
            }
    }

    @Test
    fun `maybeEmitBackground never emits Background and nullifies stagePriorToBackground if it's not Idle, Paused nor Restarted `() {
        initEventProvider()

        gameLifecycleInstances()
            .filterNot { it == Idle || it == Paused || it == Restarted }
            .forEach { stage ->
                val observable = gameLifecycleManager.gameLifecycleStream().test()

                gameLifecycleManager.setBrushingStage(stage)

                observable.assertLastValue(stage)
                assertNull(gameLifecycleManager.stagePriorToBackground)

                gameLifecycleManager.maybeEmitBackground()

                observable.assertLastValue(stage)
                assertNull(gameLifecycleManager.stagePriorToBackground)
            }
    }

    /*
    maybeRestoreBeforeOnStopStage
     */
    @Test
    fun `maybeRestoreBeforeOnStopStage does nothing if stagePriorToBackground is null`() {
        initEventProvider()
        val observable = gameLifecycleManager.gameLifecycleStream().test()

        val nbBeforeMaybeRestore = observable.valueCount()

        assertNull(gameLifecycleManager.stagePriorToBackground)

        gameLifecycleManager.maybeRestoreBeforeOnStopStage()

        assertEquals(nbBeforeMaybeRestore, observable.valueCount())
    }

    @Test
    fun `maybeRestoreBeforeOnStopStage emits Foreground followed by stagePriorToBackground if the latter is not null`() {
        initEventProvider()
        val observable = gameLifecycleManager.gameLifecycleStream().test()
            .assertValue(Idle)

        val expectedState = Restarted
        gameLifecycleManager.stagePriorToBackground = expectedState

        gameLifecycleManager.maybeRestoreBeforeOnStopStage()

        observable.assertValues(Idle, Foreground, expectedState)
    }

    @Test
    fun `maybeRestoreBeforeOnStopStage nullifies stagePriorToBackground if it's not null`() {
        initEventProvider()

        gameLifecycleManager.stagePriorToBackground = Restarted

        gameLifecycleManager.maybeRestoreBeforeOnStopStage()

        assertNull(gameLifecycleManager.stagePriorToBackground)
    }

    /*
    onStart
     */
    @Test
    fun `onStart invokes maybeRestoreBeforeOnStopStage`() {
        spyEventProvider()

        gameLifecycleManager.onStart(mock())

        verify(gameLifecycleManager).maybeRestoreBeforeOnStopStage()
    }

    /*
    onDestroy
     */
    @Test
    fun `onDestroy emits event Terminated`() {
        initEventProvider()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)

        gameLifecycleManager.onDestroy(mock())

        observable.assertLastValue(Terminated)
    }

    @Test
    fun `onDestroy unsubscribes from game toothbrush event`() {
        initEventProvider()

        assertTrue(gameToothbrushEventSubject.hasObservers())

        gameLifecycleManager.onDestroy(mock())

        assertFalse(gameToothbrushEventSubject.hasObservers())
    }

    /*
    onGameFinished
     */
    @Test
    fun `onGameFinished emits event Finished`() {
        initEventProvider()

        val observable =
            gameLifecycleManager.gameLifecycleStream().test().assertValue(Idle)

        gameLifecycleManager.setBrushingStage(Started)

        gameLifecycleManager.onGameFinished()

        observable.assertLastValue(Finished)
    }

    /*
    onGameRestarted
     */
    @Test
    fun `onGameRestarted emits event Restarted`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        gameLifecycleManager.setBrushingStage(Started)
        gameLifecycleManager.setBrushingStage(Paused)

        gameLifecycleManager.onGameRestarted()

        observable.assertLastValue(Restarted)
    }

    /*
    gameLifecycleStream
     */

    @Test
    fun `gameLifecycleStream returns last emitted stage`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()
            .assertValueCount(1)
            .assertValue(Idle)

        assertEquals(Idle, gameLifecycleManager.lifecycleState())

        setLifecycleState(Started)
        setLifecycleState(Paused)

        val newState = Resumed
        gameLifecycleManager.setBrushingStage(newState)

        observable.assertValueCount(4)

        observable.assertLastValue(newState)

        assertEquals(newState, gameLifecycleManager.lifecycleState())
    }

    @Test
    fun `gameLifecycleStream emits last emitted stage to new subscribers`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()
            .assertValueCount(1)
            .assertValue(Idle)

        gameLifecycleManager.setBrushingStage(Started)
        gameLifecycleManager.setBrushingStage(Paused)

        val newState = Resumed
        gameLifecycleManager.setBrushingStage(newState)

        observable.assertValueCount(4)

        observable.assertLastValue(newState)

        gameLifecycleManager.gameLifecycleStream().test()
            .assertValueCount(1)
            .assertValue(Resumed)
    }

    /*
    onVibratorOn
     */
    @Test
    fun `onVibratorOn emits Started if lifecycleState is Idle`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        gameLifecycleManager.onVibratorOn()

        observable.assertValueAt(1, Started)
    }

    @Test
    fun `onVibratorOn emits Started if lifecycleState is Restarted`() {
        initEventProvider()

        setLifecycleState(Restarted)

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        gameLifecycleManager.onVibratorOn()

        observable.assertValueAt(1, Started)
    }

    @Test
    fun `onVibratorOn emits Resumed if lifecycleState is different than Idle`() {
        spyEventProvider()

        assertEquals(Idle, gameLifecycleManager.lifecycleState())

        // we don't test against stream() because we don't emit duplicates
        gameLifecycleInstances()
            .filterNot { it == Idle }
            .filterNot { it == Restarted }
            .forEachIndexed { index, newGameLifecycle ->
                setLifecycleState(newGameLifecycle)

                gameLifecycleManager.onVibratorOn()

                verify(
                    gameLifecycleManager,
                    times(index + 1)
                ).setBrushingStage(Resumed)
            }
    }

    /*
    onConnectionEstablished
     */
    @Test
    fun `onConnectionEstablished emits Paused if lifecycleState is Started`() {
        initEventProvider()

        setLifecycleState(Started)

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        gameLifecycleManager.onConnectionEstablished()

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onConnectionEstablished emits Paused if lifecycleState is Resumed`() {
        initEventProvider()

        setLifecycleState(Resumed)

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        gameLifecycleManager.onConnectionEstablished()

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onConnectionEstablished emits nothing if lifecycleState is different than Started or Resumed`() {
        spyEventProvider()

        // we don't test against stream() because we don't emit duplicates
        gameLifecycleInstances()
            .filterNot { it == Started }
            .filterNot { it == Resumed }
            .forEachIndexed { index, newGameLifecycle ->
                setLifecycleState(newGameLifecycle)

                gameLifecycleManager.onConnectionEstablished()
            }

        verify(gameLifecycleManager, never()).setBrushingStage(any())
    }

    /*
    onVibratorOff
     */
    @Test
    fun `onVibratorOff emits Paused if state is Started`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        setLifecycleState(Started)

        gameLifecycleManager.onVibratorOff()

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onVibratorOff emits Paused if state is Resumed`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        setLifecycleState(Resumed)

        gameLifecycleManager.onVibratorOff()

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onVibratorOff emits nothing if lifecycleState is different than Started and Resumed`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        observable.assertValueCount(1)

        gameLifecycleInstances()
            .filterNot { it == Started }
            .filterNot { it == Resumed }
            .forEachIndexed { index, newGameLifecycle ->
                setLifecycleState(newGameLifecycle)

                gameLifecycleManager.onVibratorOff()

                // assert we only emit the event due to setLifecycleState
                observable.assertValueCount(index + 2)
            }
    }

    /*
    onConnectionLost
     */
    @Test
    fun `onConnectionLost emits Paused if state is Started`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        setLifecycleState(Started)

        gameLifecycleManager.onConnectionLost()

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onConnectionLost emits Paused if state is Resumed`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        setLifecycleState(Resumed)

        gameLifecycleManager.onConnectionLost()

        observable.assertLastValue(Paused)
    }

    @Test
    fun `onConnectionLost emits nothing if lifecycleState is different than Started and Resumed`() {
        initEventProvider()

        val observable = gameLifecycleManager.gameLifecycleStream().test()

        observable.assertValueCount(1)

        gameLifecycleInstances()
            .filterNot { it == Started }
            .filterNot { it == Resumed }
            .forEachIndexed { index, gameLifecycle ->
                setLifecycleState(gameLifecycle)

                gameLifecycleManager.onConnectionLost()

                // assert we only emit the event due to setLifecycleState
                observable.assertValueCount(index + 2)
            }
    }

    /*
    onToothbrushEvent
     */
    @Test
    fun `onToothbrushEvent ConnectionEstablished invokes onConnectionEstablished`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).onConnectionEstablished()

        gameLifecycleManager.onToothbrushEvent(GameToothbrushEvent.ConnectionEstablished(mock()))

        verify(gameLifecycleManager).onConnectionEstablished()
    }

    @Test
    fun `onToothbrushEvent ConnectionLost invokes onConnectionLost`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).onConnectionLost()

        gameLifecycleManager.onToothbrushEvent(GameToothbrushEvent.ConnectionLost(mock()))

        verify(gameLifecycleManager).onConnectionLost()
    }

    @Test
    fun `onToothbrushEvent VibratorOn invokes onVibratorOn`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).onVibratorOn()

        gameLifecycleManager.onToothbrushEvent(GameToothbrushEvent.VibratorOn(mock()))

        verify(gameLifecycleManager).onVibratorOn()
    }

    @Test
    fun `onToothbrushEvent VibratorOff invokes onVibratorOff`() {
        spyEventProvider()

        doNothing().whenever(gameLifecycleManager).onVibratorOff()

        gameLifecycleManager.onToothbrushEvent(GameToothbrushEvent.VibratorOff(mock()))

        verify(gameLifecycleManager).onVibratorOff()
    }

    /*
    UTILS
     */

    private fun spyEventProvider(gameToothbrushEventSubject: PublishSubject<GameToothbrushEvent> = PublishSubject.create()) {
        initEventProvider(gameToothbrushEventSubject)

        gameLifecycleManager = spy(gameLifecycleManager)
    }

    private fun initEventProvider(gameToothbrushEventSubject: PublishSubject<GameToothbrushEvent> = PublishSubject.create()) {
        this.gameToothbrushEventSubject = gameToothbrushEventSubject
        val gameToothbrushEventProvider =
            mockGameToothbrushEventProvider(gameToothbrushEventSubject)

        gameLifecycleManager = GameLifecycleCoordinatorImpl(
            lifecycle,
            gameToothbrushEventProvider
        )
    }

    private fun setLifecycleState(newGameLifecycle: GameLifecycle) {
        gameLifecycleManager.coachStateRelay.accept(newGameLifecycle)
    }
}
