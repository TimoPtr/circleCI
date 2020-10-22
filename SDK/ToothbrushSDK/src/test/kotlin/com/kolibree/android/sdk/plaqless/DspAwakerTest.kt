/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Test

class DspAwakerTest : BaseUnitTest() {
    private val serviceProvider: ServiceProvider = mock()

    private val testScheduler = TestScheduler()

    private var dspAwaker = DspAwakerImpl(serviceProvider, testScheduler)

    /*
    keepAlive
     */

    @Test
    fun `keepAlive invokes wakeUp on subscription`() {
        val keepAliveTime = 30L
        val unit = TimeUnit.SECONDS

        spyAwaker()

        completeOnWakeUp()

        val observer = dspAwaker.keepAlive(keepAliveTime, unit).test()

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        verify(dspAwaker).wakeUp()
    }

    @Test
    fun `keepAlive invokes wakeUp every DSP_SHUTDOWN_SECONDS divided 2 that fit in the specified amount of time`() {
        val keepAliveTime = 30L
        val unit = TimeUnit.SECONDS

        spyAwaker()

        val expectedInvocations = keepAliveTime / (DSP_SHUTDOWN_SECONDS / 2)
        val expectedInvokedEvery = keepAliveTime / expectedInvocations

        completeOnWakeUp()

        val observer = dspAwaker.keepAlive(keepAliveTime, unit).test().assertNotComplete()

        /*
        Because of TestScheduler, the wakeUp that should happen after delay=0, only occurs after we
        advance time for the first time. So after advancing time for the first time, we actually
        expect 2 events
         */
        var expectedTimes = 2
        (0 until (expectedInvocations - 1)).forEach {
            observer.assertNotComplete()

            testScheduler.advanceTimeBy(expectedInvokedEvery, unit)

            verify(dspAwaker, times(expectedTimes++)).wakeUp()
        }

        verify(dspAwaker, times(expectedInvocations.toInt())).wakeUp()

        observer.assertComplete()
    }

    @Test
    fun `keepAlive stops invoking wakeUp after disposal`() {
        val keepAliveTime = 30L
        val unit = TimeUnit.SECONDS

        spyAwaker()

        completeOnWakeUp()

        val observer = dspAwaker.keepAlive(keepAliveTime, unit).test().assertNotComplete()

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        verify(dspAwaker).wakeUp()

        // from now own, advancing time should be ignored
        observer.dispose()

        testScheduler.advanceTimeBy(keepAliveTime, unit)

        verify(dspAwaker).wakeUp()

        testScheduler.advanceTimeBy(keepAliveTime, unit)

        verify(dspAwaker).wakeUp()
    }

    /*
    wakeUp
     */

    @Test
    fun `wakeUp completes if there isn't any connection`() {
        prepareServiceProvide()

        dspAwaker.wakeUp().test().assertComplete()
    }

    @Test
    fun `wakeUp never sends ping to non Active connection, even if it has DSP processor`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withState(state)
                    .withModel(ToothbrushModel.PLAQLESS)
                    .build()

                prepareServiceProvide(connection)

                dspAwaker.wakeUp().test().assertComplete()

                verify(connection.toothbrush(), never()).ping()
            }
    }

    @Test
    fun `wakeUp never sends ping to connection without DSP support, even if it's Active`() {
        ToothbrushModel.values()
            .filterNot { it.hasDsp }
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withState(KLTBConnectionState.ACTIVE)
                    .withModel(model)
                    .build()

                prepareServiceProvide(connection)

                dspAwaker.wakeUp().test().assertComplete()

                verify(connection.toothbrush(), never()).ping()
            }
    }

    @Test
    fun `wakeUp sends ping to Active connection with DSP support`() {
        val plaqlessConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withModel(ToothbrushModel.PLAQLESS)
            .withPingSupport()
            .build()

        prepareServiceProvide(plaqlessConnection)

        spyAwaker()

        dspAwaker.wakeUp().test().assertComplete()

        verify(plaqlessConnection.toothbrush()).ping()
    }

    /*
    sendPing
     */
    @Test
    fun `sendPing success sends ping to connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withPingSupport()
            .build()

        dspAwaker.sendPing(connection).test()

        verify(connection.toothbrush()).ping()
    }

    /*
    Utils
     */

    private fun spyAwaker() {
        dspAwaker = spy(dspAwaker)
    }

    private fun prepareServiceProvide(vararg connections: KLTBConnection) {
        val service = mock<KolibreeService>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))

        whenever(service.knownConnections).thenReturn(connections.toList())
    }

    private fun completeOnWakeUp() {
        doReturn(Completable.complete()).whenever(dspAwaker).wakeUp()
    }
}
