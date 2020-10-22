package com.kolibree.android.sdk.core

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATED
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.TERMINATING
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.core.VibratorImpl.Companion.maybeCreateAwaker
import com.kolibree.android.sdk.core.driver.VibratorDriver
import com.kolibree.android.sdk.core.driver.VibratorMode
import com.kolibree.android.sdk.e1.ToothbrushAwaker
import com.kolibree.android.sdk.test.FakeListenerPool
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.concurrent.atomic.AtomicReference
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mock

internal class VibratorImplTest : BaseUnitTest() {
    private val eventsSource: KLTBConnection = KLTBConnectionBuilder.createAndroidLess().build()

    @Mock
    lateinit var vibratorDriver: VibratorDriver

    private val listenerPool = FakeListenerPool<VibratorListener>()

    private val immediateNotifyStates = listOf(ACTIVE, TERMINATING, TERMINATED, OTA)

    private lateinit var vibrator: VibratorImpl

    override fun setup() {
        super.setup()

        initVibrator()
    }

    private fun initVibrator(e1Awaker: ToothbrushAwaker? = null) {
        val handler: Handler = mock()
        whenever(handler.post(any())).thenAnswer {
            it.getArgument<Runnable>(0).run()
            true
        }
        vibrator = VibratorImpl(eventsSource, vibratorDriver, listenerPool, e1Awaker, handler)
    }

    @Test
    fun `On sends VibratorMode START`() {
        initVibrator()

        vibrator.on()

        verify(vibratorDriver).setVibratorMode(VibratorMode.START)
    }

    @Test
    fun `Off sends VibratorMode STOP`() {
        initVibrator()

        vibrator.off()

        verify(vibratorDriver).setVibratorMode(VibratorMode.STOP)
    }

    @Test
    fun `Off and stop recording sends VibratorMode STOP_AND_HALT_RECORDING`() {
        initVibrator()

        vibrator.offAndStopRecording()

        verify(vibratorDriver).setVibratorMode(VibratorMode.STOP_AND_HALT_RECORDING)
    }

    /*
    maybeCreateAwaker
     */
    @Test
    fun `maybeCreateAwaker returns non null for model E1`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(ToothbrushModel.CONNECT_E1)
            .build()

        assertNotNull(maybeCreateAwaker(connection))
    }

    @Test
    fun `maybeCreateAwaker returns null for any model different than E1`() {
        ToothbrushModel.values()
            .filterNot { it == ToothbrushModel.CONNECT_E1 }
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withModel(model)
                    .build()

                assertNull("Expected null for $model", maybeCreateAwaker(connection))
            }
    }

    /*
    register
     */

    @Test
    fun `register always invokes keepAlive if e1Awaker is not null`() {
        val e1Awaker = mock<ToothbrushAwaker>()
        initVibrator(e1Awaker)

        (1 until 10).forEach { nbOflisteners ->
            vibrator.register(mock())

            verify(e1Awaker, times(nbOflisteners)).keepAlive()
        }
    }

    @Test
    fun `register does not crash if listenerPool returns 1 after add and e1Awaker is null`() {
        initVibrator()

        assertEquals(0, listenerPool.size())

        vibrator.register(mock())
    }

    /*
    unregister
     */
    @Test
    fun `unregister invokes allowShutdown if listenerPool returns 0 after remove and e1Awaker is not null`() {
        val e1Awaker = mock<ToothbrushAwaker>()
        initVibrator(e1Awaker)

        assertEquals(0, listenerPool.size())

        vibrator.unregister(mock())

        verify(e1Awaker).allowShutdown()
    }

    @Test
    fun `unregister does not crash if listenerPool returns 0 after remove and e1Awaker is null`() {
        initVibrator()

        assertEquals(0, listenerPool.size())

        vibrator.unregister(mock())
    }

    @Test
    fun `unregister does not invoke allowShutdown if listenerPool returns anything different than 1 after remove`() {
        val e1Awaker = mock<ToothbrushAwaker>()
        initVibrator(e1Awaker)

        (0 until 10).filterNot { it == 0 }.forEach {
            listenerPool.add(mock())
            listenerPool.add(mock())

            vibrator.unregister(mock())

            assertNotSame(0, listenerPool.size())
        }

        verify(e1Awaker, never()).allowShutdown()
    }

    /*
    clearCache
     */
    @Test
    fun `clearCache sets vibratorOn to false`() {
        initVibrator()

        vibrator.onVibratorStateChanged(true)

        assertTrue(vibrator.isOn)

        vibrator.clearCache()

        assertFalse(vibrator.isOn)
    }

    @Test
    fun `clearCache invokes allowShutdown on E1Awaker if it's not null`() {
        val e1Awaker = mock<ToothbrushAwaker>()
        initVibrator(e1Awaker)

        vibrator.clearCache()

        verify(e1Awaker, never()).keepAlive()
    }

    @Test
    fun `clearCache doesn't crash if E1Awaker is null`() {
        initVibrator()

        vibrator.clearCache()
    }

    /*
    onVibratorStateChanged
     */
    @Test
    fun `onVibratorStateChanged stores vibration state`() {
        assertFalse(vibrator.isOn)

        vibrator.onVibratorStateChanged(true)

        assertTrue(vibrator.isOn)
    }

    @Test
    fun `onVibratorStateChanged notifies listeners emits immediately if connection is ACTIVE, OTA, TERMINATED or TERMINATING`() {
        var onVibratorStateChanged: Boolean
        val vibratorListener = object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                onVibratorStateChanged = on
            }
        }
        val testObserver = vibrator.vibratorStream.test()
        var counter = 1

        listenerPool.add(vibratorListener)

        immediateNotifyStates.forEach { connectionState ->
            onVibratorStateChanged = false

            mockConnectionState(connectionState)

            vibrator.onVibratorStateChanged(!onVibratorStateChanged)
            testObserver.assertLastValue(onVibratorStateChanged).assertValueCount(++counter)
            assertTrue(onVibratorStateChanged)
        }
    }

    @Test
    fun `onVibratorStateChanged doesn't notify listeners nor emits if connection is not ACTIVE, OTA, TERMINATED or TERMINATING`() {
        var onVibratorStateChanged = false
        val vibratorListener = object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                onVibratorStateChanged = on
            }
        }
        listenerPool.add(vibratorListener)
        val testObserver = vibrator.vibratorStream.test()

        KLTBConnectionState.values()
            .filterNot { it in immediateNotifyStates }
            .forEach { connectionState ->
                mockConnectionState(connectionState)

                vibrator.onVibratorStateChanged(true)

                testObserver.assertValue(false).assertValueCount(1)
                assertFalse(onVibratorStateChanged)
            }
    }

    @Test
    fun `onVibratorStateChanged waits until connection is ACTIVE before notifying listeners and stream`() {
        var onVibratorStateChanged = false
        val vibratorListener = object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                onVibratorStateChanged = on
            }
        }

        listenerPool.add(vibratorListener)
        mockConnectionState(ESTABLISHING)

        val testObserver = vibrator.vibratorStream.test()
        testObserver.assertValueCount(0)

        val connectionStateListeners = prepareConnectionStateListener()

        vibrator.onVibratorStateChanged(!onVibratorStateChanged)

        testObserver.assertValueCount(0)

        assertFalse(onVibratorStateChanged)

        assertNotNull(connectionStateListeners)

        connectionStateListeners.forEach {
            it.get()!!.onConnectionStateChanged(eventsSource, ESTABLISHING)
        }

        assertFalse(onVibratorStateChanged)

        testObserver.assertValueCount(0)

        connectionStateListeners.forEach {
            it.get()!!.onConnectionStateChanged(eventsSource, ACTIVE)
        }

        assertTrue(onVibratorStateChanged)

        testObserver.assertValueCount(1).assertValues(true)
    }

    @Test
    fun `onVibratorStateChanged waits until connection is ACTIVE before notifying multiple listeners and stream`() {
        var firstOnVibratorStateChanged = false
        val firstVibratorListener = object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                firstOnVibratorStateChanged = on
            }
        }

        var secondOnVibratorStateChanged = false
        val secondVibratorListener = object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                secondOnVibratorStateChanged = on
            }
        }

        listenerPool.add(firstVibratorListener)
        listenerPool.add(secondVibratorListener)
        mockConnectionState(ESTABLISHING)

        val testObserver = vibrator.vibratorStream.test()
        testObserver.assertValueCount(0)

        val connectionStateListeners = prepareConnectionStateListener()

        vibrator.onVibratorStateChanged(!firstOnVibratorStateChanged)

        testObserver.assertValueCount(0)

        assertFalse(firstOnVibratorStateChanged)
        assertFalse(secondOnVibratorStateChanged)

        assertNotNull(connectionStateListeners)

        connectionStateListeners.forEach {
            it.get()!!.onConnectionStateChanged(eventsSource, ESTABLISHING)
        }

        assertFalse(firstOnVibratorStateChanged)
        assertFalse(secondOnVibratorStateChanged)

        testObserver.assertValueCount(0)

        connectionStateListeners.forEach {
            it.get()!!.onConnectionStateChanged(eventsSource, ACTIVE)
        }

        assertTrue(secondOnVibratorStateChanged)
        assertTrue(firstOnVibratorStateChanged)

        testObserver.assertValueCount(1).assertValues(true)
    }

    @Test
    fun `onVibratorStateChanged unregisters as state listener if state is ACTIVE, TERMINATING or TERMINATED`() {
        mockConnectionState(ESTABLISHING)

        registerNoOpVibratorListener()

        val connectionStateListeners = prepareConnectionStateListener()

        vibrator.onVibratorStateChanged(true)

        immediateNotifyStates.forEach { connectionState ->
            connectionStateListeners.forEach {
                it.get()!!.onConnectionStateChanged(eventsSource, connectionState)
            }
        }

        connectionStateListeners.forEach {
            verify(eventsSource.state(), times(3)).unregister(it.get()!!)
        }
    }

    @Test
    fun `onVibratorStateChanged never unregisters as state listener if state is not ACTIVE, TERMINATING or TERMINATED`() {
        mockConnectionState(ESTABLISHING)

        registerNoOpVibratorListener()

        val connectionStateListeners = prepareConnectionStateListener()

        vibrator.onVibratorStateChanged(true)

        KLTBConnectionState.values()
            .filterNot { it in immediateNotifyStates }
            .forEach { connectionState ->
                connectionStateListeners.forEach {
                    it.get()!!.onConnectionStateChanged(eventsSource, connectionState)
                }
            }

        connectionStateListeners.forEach {
            verify(eventsSource.state(), never()).unregister(it.get()!!)
        }
    }

    @Test
    fun `onVibratorStateChanged only notifies the last received state once connection is active`() {
        var onVibratorStateChanged = false
        var counter = 0
        val vibratorListener = object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                onVibratorStateChanged = on
                counter++
            }
        }
        listenerPool.add(vibratorListener)
        mockConnectionState(ESTABLISHING)

        val testObserver = vibrator.vibratorStream.test()

        val connectionStateListeners = prepareConnectionStateListener()

        vibrator.onVibratorStateChanged(true)
        vibrator.onVibratorStateChanged(false)
        vibrator.onVibratorStateChanged(true)

        testObserver.assertValueCount(0)

        connectionStateListeners.forEach {
            it.get()!!.onConnectionStateChanged(eventsSource, ACTIVE)
        }

        vibrator.onVibratorStateChanged(true)

        assertTrue(onVibratorStateChanged)
        assertEquals(1, counter)
        testObserver.assertValues(true)
    }

    /*
    vibratorStream
     */
    @Test
    fun `vibratorStream emits on subscribe the current state of the vibrator if the connection is active`() {
        mockConnectionState(ACTIVE)

        val testObserver = vibrator.vibratorStream.test().assertValue(false)

        vibrator.onVibratorStateChanged(true)

        testObserver.assertLastValue(true)
    }

    @Test
    fun `vibratorStream does not emits on subscribe the current state of the vibrator if the connection is not active`() {
        mockConnectionState(ESTABLISHING)

        vibrator.vibratorStream.test().assertNoValues()
    }

    /*
    Utils
     */

    private fun registerNoOpVibratorListener() {
        listenerPool.add(object : VibratorListener {
            override fun onVibratorStateChanged(connection: KLTBConnection, on: Boolean) {
                // no-op, I'm dumb
            }
        })
    }

    private fun mockConnectionState(state: KLTBConnectionState) {
        whenever(eventsSource.state().current).thenReturn(state)
    }

    private fun prepareConnectionStateListener(): Set<AtomicReference<ConnectionStateListener?>> {
        val listenerReferences = mutableSetOf<AtomicReference<ConnectionStateListener?>>()
        whenever(eventsSource.state().register(any()))
            .thenAnswer { args ->
                if (listenerReferences.find {
                        it.get() == args.getArgument(0)
                    } == null) {
                    listenerReferences.add(AtomicReference(args.getArgument(0) as ConnectionStateListener))
                }

                Unit
            }
        return listenerReferences
    }
}
