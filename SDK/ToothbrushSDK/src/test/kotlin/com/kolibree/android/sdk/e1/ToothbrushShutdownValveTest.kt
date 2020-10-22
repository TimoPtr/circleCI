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
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.Test

class ToothbrushShutdownValveTest : BaseUnitTest() {
    private val mac: String = DEFAULT_MAC

    private val connectionProvider: KLTBConnectionProvider = mock()

    private val testScheduler = TestScheduler()

    private var valve = ToothbrushShutdownValveImpl(connectionProvider, mac, testScheduler)

    @Test
    fun `preventShutdownValve invokes keepAlive once`() {
        spyValve()

        val connection = createConnection()
        whenever(connectionProvider.existingActiveConnection(mac))
            .thenReturn(Single.just(connection))

        val e1Awaker = mock<ToothbrushAwaker>()
        doReturn(e1Awaker).whenever(valve).createToothbrushAwaker(connection)

        val observer = valve.preventShutdownValve().test().assertNotComplete()

        verify(e1Awaker).keepAlive()

        testScheduler.advanceTimeBy(ACTIVE_CONNECTION_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)

        // no further invocations
        verify(e1Awaker).keepAlive()

        observer.assertNotComplete()
    }

    @Test
    fun `preventShutdownValve invokes allowShutdown on dispose`() {
        spyValve()

        val connection = createConnection()
        whenever(connectionProvider.existingActiveConnection(mac))
            .thenReturn(Single.just(connection))

        val e1Awaker = mock<ToothbrushAwaker>()
        doReturn(e1Awaker).whenever(valve).createToothbrushAwaker(connection)

        val observer = valve.preventShutdownValve().test().assertNotComplete()

        verify(e1Awaker, never()).allowShutdown()

        testScheduler.advanceTimeBy(ACTIVE_CONNECTION_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)

        verify(e1Awaker, never()).allowShutdown()

        observer.dispose()

        verify(e1Awaker).allowShutdown()
    }

    @Test
    fun `preventShutdownValve emits error if connectionProvider emits error`() {
        whenever(connectionProvider.existingActiveConnection(mac))
            .thenReturn(Single.error(TestForcedException()))

        valve.preventShutdownValve().test().assertError(TestForcedException::class.java)
    }

    @Test
    fun `preventShutdownValve times out if connectionProvider doesn't return a connection in 10 seconds`() {
        whenever(connectionProvider.existingActiveConnection(mac))
            .thenReturn(Single.never())

        val observer = valve.preventShutdownValve().test()

        testScheduler.advanceTimeBy(ACTIVE_CONNECTION_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)

        observer.assertError(TimeoutException::class.java)
    }

    /*
    Utils
     */
    private fun spyValve() {
        valve = spy(valve)
    }

    private fun createConnection(model: ToothbrushModel = ToothbrushModel.CONNECT_E1): KLTBConnection {
        return KLTBConnectionBuilder.createAndroidLess().withModel(model).build()
    }
}
