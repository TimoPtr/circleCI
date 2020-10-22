package com.kolibree.android.sdk.core.ota.kltb002.updater

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Test

class ConnectionStateMonitorTest : BaseUnitTest() {

    private lateinit var monitor: ConnectionStateMonitor

    private val connection = mock<InternalKLTBConnection>()

    private var timeScheduler = TestScheduler()

    override fun setup() {
        super.setup()

        monitor = spy(ConnectionStateMonitor(connection))
        doReturn(timeScheduler).whenever(monitor).timeControlScheduler
    }

    @Test
    fun `waitForActiveConnection completes immediately if connection is active`() {
        doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ACTIVE)
                .build()
                .state())
            .whenever(connection)
            .state()

        val observer = monitor.waitForActiveConnection().test()

        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun `waitForActiveConnection fails immediately if connection is terminating`() {
        doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.TERMINATING)
                .build()
                .state())
            .whenever(connection)
            .state()

        val observer = monitor.waitForActiveConnection().test()

        observer.assertError(IllegalStateException::class.java)
    }

    @Test
    fun `waitForActiveConnection fails immediately if connection is terminated`() {
        doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.TERMINATED)
                .build()
                .state())
            .whenever(connection)
            .state()

        val observer = monitor.waitForActiveConnection().test()

        observer.assertError(IllegalStateException::class.java)
    }

    @Test
    fun `waitForActiveConnection waits for the connection until it is established`() {
        doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ESTABLISHING)
                .build()
                .state())
            .whenever(connection)
            .state()

        val observer = monitor.waitForActiveConnection().test()
        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertNotComplete()

        doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ACTIVE)
                .build()
                .state())
            .whenever(connection)
            .state()

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
    }
}
