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
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.SensorConfiguration
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GameToothbrushInteractorFacadeTest : BaseUnitTest() {

    private val context: Context = mock()
    private val sensorConfigurationFactory: SensorConfiguration.Factory = mock()
    private val listener: GameSensorListener = mock()
    private val lifecycle: Lifecycle = mock()

    private var interactorFacade = spy(
        GameToothbrushInteractorFacade(
            context,
            sensorConfigurationFactory,
            listener,
            lifecycle
        )
    )

    /*
    onGameRestartedCompletable
    */
    @Test
    fun `onGameRestarted invokes gameLifecycleCoordinator onGameRestarted is instance was initialized`() {
        spyFacade()
        setInitialized()

        interactorFacade
            .onGameRestarted()
            .test()
            .assertComplete()
            .assertNoErrors()

        verify(interactorFacade.gameLifecycleCoordinator).onGameRestarted()
    }

    /*
    initializedForDifferentConnection
     */
    @Test
    fun `initializedForDifferentConnection returns false if component is null`() {
        assertNull(interactorFacade.toothbrushInteractorComponent)

        assertFalse(interactorFacade.initializedForDifferentConnection(mock()))
    }

    @Test
    fun `initializedForDifferentConnection returns false if component is not null and newConnection is same mac address`() {
        interactorFacade.toothbrushInteractorComponent = mock()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(interactorFacade.toothbrushInteractorComponent!!.connection())
            .thenReturn(connection)

        assertFalse(
            interactorFacade.initializedForDifferentConnection(
                KLTBConnectionBuilder.createAndroidLess().build()
            )
        )
    }

    @Test
    fun `initializedForDifferentConnection returns true if component is not null and newConnection is different mac address`() {
        interactorFacade.toothbrushInteractorComponent = mock()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(interactorFacade.toothbrushInteractorComponent!!.connection())
            .thenReturn(connection)

        assertTrue(
            interactorFacade.initializedForDifferentConnection(
                KLTBConnectionBuilder.createAndroidLess()
                    .withMac("dadada")
                    .build()
            )
        )
    }

    /*
    onGameFinished
     */
    @Test
    fun `onGameFinished returns empty completable if instance wasn't initialized`() {
        interactorFacade.onGameFinished().test().assertComplete()
    }

    @Test
    fun `onGameFinished executes finished completables if initialized`() {
        spyFacade()
        setInitialized()

        interactorFacade
            .onGameFinished()
            .test()
            .assertComplete()
            .assertNoErrors()

        verify(interactorFacade.gameLifecycleCoordinator).onGameFinished()
    }

    /*
    onConnectionEstablished
     */
    @Test(expected = AssertionError::class)
    fun `onConnectionEstablished kicks FailEarly if instance was already injected for a different connection`() {
        spyFacade()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        doReturn(true).whenever(interactorFacade).initializedForDifferentConnection(connection)

        interactorFacade.onConnectionEstablished(connection)
    }

    @Test
    fun `gameLifeCycleObservable exposes gameLifecycleStream()`() {
        interactorFacade.gameLifecycleProvider = mock()
        whenever(interactorFacade.gameLifecycleProvider.gameLifecycleStream())
            .thenReturn(mock())

        interactorFacade.gameLifeCycleObservable().test()
        interactorFacade.initializedSubject.onComplete()

        verify(interactorFacade.gameLifecycleProvider).gameLifecycleStream()
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun `gameLifeCycleObservable never invokes gameToothbrushEventProvider onConnectionEstablished if it's already initialized`() {
        spyFacade()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        doReturn(true).whenever(interactorFacade).initialized()

        interactorFacade.onConnectionEstablished(connection)

        interactorFacade.gameToothbrushEventProvider
    }

    @Test
    fun `gameLifeCycleObservable invokes gameToothbrushEventProvider onConnectionEstablished on first initialization`() {
        spyFacade()

        val gameToothbrushEventProvider: GameToothbrushEventProvider = mock()
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        doAnswer {
            interactorFacade.gameToothbrushEventProvider = gameToothbrushEventProvider
        }.whenever(interactorFacade).injectSelf(connection)

        doReturn(false).whenever(interactorFacade).initialized()

        interactorFacade.onConnectionEstablished(connection)

        assertSame(gameToothbrushEventProvider, interactorFacade.gameToothbrushEventProvider)

        verify(gameToothbrushEventProvider).onConnectionEstablished()
    }

    @Test
    fun `gameLifeCycleObservable completes initializedSubject on first initialization`() {
        spyFacade()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        doAnswer {
            interactorFacade.gameToothbrushEventProvider = mock()
        }.whenever(interactorFacade).injectSelf(connection)

        doReturn(false).whenever(interactorFacade).initialized()

        val observer = interactorFacade.initializedSubject.test().assertNotComplete()

        interactorFacade.onConnectionEstablished(connection)

        observer.assertComplete()
    }

    @Test
    fun `gameLifeCycleObservable never completes initializedSubject if already initialized`() {
        spyFacade()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        doNothing().whenever(interactorFacade).injectSelf(connection)

        doReturn(true).whenever(interactorFacade).initialized()

        val observer = interactorFacade.initializedSubject.test().assertNotComplete()

        interactorFacade.onConnectionEstablished(connection)

        observer.assertNotComplete()
    }

    /*
    utils
     */
    private fun spyFacade() {
        interactorFacade = spy(interactorFacade)
    }

    private fun setInitialized() {
        interactorFacade.gameLifecycleCoordinator = mock()
    }
}
