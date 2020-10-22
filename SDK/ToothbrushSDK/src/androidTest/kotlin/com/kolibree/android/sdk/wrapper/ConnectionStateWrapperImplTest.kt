package com.kolibree.android.sdk.wrapper

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.ConnectionStateImpl
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.extensions.reset
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.nhaarman.mockitokotlin2.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConnectionStateWrapperImplTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @JvmField
    @Rule
    val mOverrideSchedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    private lateinit var connectionStateWrapper: ConnectionStateWrapperImpl

    private val connection = mock<KLTBConnection>()

    private val connectionState = ConnectionStateImpl(connection)

    @After
    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
        TrustedClock.reset()
    }

    @Before
    override fun setUp() {
        super.setUp()
        connectionStateWrapper = ConnectionStateWrapperImpl(connectionState)
    }

    @Test
    fun verifyInitialyState() {
        assertEquals(KLTBConnectionState.NEW, connectionStateWrapper.getCurrent())

        checkIfRegisterGivenState(KLTBConnectionState.NEW)
    }

    @Test
    fun verifyIfRegistered() {
        checkIfRegisterGivenState(KLTBConnectionState.ACTIVE, true)
    }

    @Test
    fun verifyWhenNotRegistered() {
        checkIfRegisterGivenState(KLTBConnectionState.OTA)
        checkIfRegisterGivenState(KLTBConnectionState.TERMINATED)
        checkIfRegisterGivenState(KLTBConnectionState.TERMINATING)
        checkIfRegisterGivenState(KLTBConnectionState.ESTABLISHING)
    }

    private fun checkIfRegisterGivenState(
        newState: KLTBConnectionState,
        isActive: Boolean = false
    ) {
        /*
        connectionState.set happens on the testing thread, and it queues the listener notification
        on to the main thread.

        This cause flakyness, as sometimes our check occurred before the message was run on the main
        thread, so the new state hadn't been propagated.

        By registering ourselves and placing a countdown latch, we make sure that at least one of the
        listeners has been notified
         */
        val countdownLatch = CountDownLatch(1)

        connectionState.register(object : ConnectionStateListener {
            override fun onConnectionStateChanged(
                connection: KLTBConnection,
                newState: KLTBConnectionState
            ) {
                countdownLatch.countDown()
            }
        })

        connectionState.set(newState)

        countdownLatch.await(1, SECONDS)

        connectionStateWrapper.isRegistered()
            .test()
            .assertNoErrors()
            .assertValue(isActive)
    }
}
