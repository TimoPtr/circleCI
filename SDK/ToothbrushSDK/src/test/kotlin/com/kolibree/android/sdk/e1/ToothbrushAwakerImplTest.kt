/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.e1

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToothbrushAwakerImplTest : BaseUnitTest() {
    private val testScheduler = TestScheduler()

    private lateinit var e1Awaker: ToothbrushAwakerImpl

    private var connection: KLTBConnection? = null

    /*
    Init
     */
    @Test
    fun `init throws IllegalArgumentException if connection's model doesn't support keep alive`() {
        ToothbrushModel.values()
            .filterNot { it.canBeKeptAwake }
            .forEach { model ->
                var exceptionThrown = false
                try {
                    init(model)
                } catch (iae: IllegalArgumentException) {
                    exceptionThrown = true
                }

                assertTrue(exceptionThrown)
            }
    }

    @Test
    fun `init doesn't throw exception if connection is E1 or E2`() {
        ToothbrushModel.values()
            .filter { it.canBeKeptAwake }
            .forEach { model ->
                init(model)
            }
    }

    /*
    keepAlive
     */
    @Test
    fun `keepAlive does nothing if there's a previous subscriber`() {
        init()

        e1Awaker.keepAliveDisposable = mock()
        whenever(e1Awaker.keepAliveDisposable!!.isDisposed).thenReturn(false)

        e1Awaker.keepAlive()

        verify(connection!!.toothbrush(), never()).ping()
    }

    @Test
    fun `keepAlive sends ping on subscription`() {
        init()

        e1Awaker.keepAlive()

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        verify(connection!!.toothbrush()).ping()
    }

    @Test
    fun `keepAlive invokes wakeUp every E1_SHUTDOWN_SECONDS minus 1`() {
        init()

        val expectedInvokedEvery = SHUTDOWN_SECONDS - 1

        e1Awaker.keepAlive()

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        verify(connection!!.toothbrush()).ping()

        testScheduler.advanceTimeBy(expectedInvokedEvery, TimeUnit.SECONDS)

        verify(connection!!.toothbrush(), times(2)).ping()

        testScheduler.advanceTimeBy(expectedInvokedEvery, TimeUnit.SECONDS)

        verify(connection!!.toothbrush(), times(3)).ping()
    }

    @Test
    fun `keepAlive stops invoking wakeUp after we invoke allowShutdown`() {
        init()

        val expectedInvokedEvery = SHUTDOWN_SECONDS - 1

        e1Awaker.keepAlive()

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        verify(connection!!.toothbrush()).ping()

        e1Awaker.allowShutdown()

        testScheduler.advanceTimeBy(expectedInvokedEvery, TimeUnit.SECONDS)

        verify(connection!!.toothbrush(), times(1)).ping()
    }

    /*
    sendPing
     */

    @Test
    fun `sendPing sends ping to connection if it's active`() {
        val subject = CompletableSubject.create()
        init(pingCompletable = subject)

        e1Awaker.sendPing().test()

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `sendPing doesn't ping connection if it's not active`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                init(state = state)

                e1Awaker.sendPing().test()

                verify(connection!!.toothbrush(), never()).ping()
            }
    }

    /*
    Utils
     */

    private fun init(
        model: ToothbrushModel = ToothbrushModel.CONNECT_E1,
        state: KLTBConnectionState = KLTBConnectionState.ACTIVE,
        pingCompletable: Completable = Completable.complete()
    ) {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(model)
            .withPingSupport(pingCompletable)
            .withState(state)
            .build()

        e1Awaker = ToothbrushAwakerImpl(connection!!, testScheduler)
    }
}
