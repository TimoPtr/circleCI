/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.extensions.withFixedInstant
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

internal class MonitorCurrentInteractorTest : BaseUnitTest() {
    private val lifecycle = mock<Lifecycle>()

    private val testScheduler = TestScheduler()

    private lateinit var monitorInteractor: MonitorCurrentBrushingInteractor

    /*
    Init
     */
    @Test
    fun `init subscribes to Brushing Events`() = withFixedInstant {
        val subject = PublishSubject.create<GameToothbrushEvent>()
        initInteractor(subject)

        monitorInteractor.onVibratorOffTimestamp = 5435L

        assertTrue(subject.hasObservers())

        val expectedEvent = GameToothbrushEvent.VibratorOff(mock())
        subject.onNext(expectedEvent)

        assertEquals(
            TrustedClock.getNowInstant().toEpochMilli(),
            monitorInteractor.onVibratorOffTimestamp
        )
    }

    /*
    ON DESTROY
     */

    @Test
    fun `onDestroy disposes monitor current brushing`() {
        // make sure monitorCurrentDisposable is added to Disposables
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val monitorCurrentSubject = CompletableSubject.create()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withSupportMonitorCurrent(monitorCurrentSubject)
                .build()

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        val monitorCurrentDisposable = monitorInteractor.monitorCurrentDisposable!!
        assertFalse(monitorCurrentDisposable.isDisposed)

        monitorInteractor.onDestroy(mock())

        assertTrue(monitorCurrentDisposable.isDisposed)
    }

    /*
    onToothbrushEvent
     */

    @Test
    fun `onToothbrushEvent ConnectionLost invokes onConnectionLost`() {
        spyInteractor()

        doNothing().whenever(monitorInteractor).onConnectionLost()

        monitorInteractor.onToothbrushEvent(GameToothbrushEvent.ConnectionLost(mock()))

        verify(monitorInteractor).onConnectionLost()
    }

    @Test
    fun `onToothbrushEvent VibratorOff invokes onVibratorOff`() {
        spyInteractor()

        doNothing().whenever(monitorInteractor).onVibratorOff()

        monitorInteractor.onToothbrushEvent(GameToothbrushEvent.VibratorOff(mock()))

        verify(monitorInteractor).onVibratorOff()
    }

    @Test
    fun `onToothbrushEvent VibratorOn invokes onVibratorOn`() {
        spyInteractor()

        doNothing().whenever(monitorInteractor).onVibratorOn(any())

        val expectedConnection = mock<KLTBConnection>()
        monitorInteractor.onToothbrushEvent(GameToothbrushEvent.VibratorOn(expectedConnection))

        verify(monitorInteractor).onVibratorOn(expectedConnection)
    }

    /*
    onVibratorOn
     */

    @Test
    fun `onVibratorOn invokes maybeSendMonitorCurrentBrushing`() {
        val connection = createSensorTestConnection()

        spyInteractor()

        doNothing().whenever(monitorInteractor).maybeSendMonitorCurrentBrushing(connection)

        monitorInteractor.onVibratorOn(connection)

        verify(monitorInteractor).maybeSendMonitorCurrentBrushing(connection)
    }

    /*
    onVibratorOff
     */

    @Test
    fun `onVibratorOff stores onVibratorOffTimestamp`() {
        initInteractor()

        assertEquals(0, monitorInteractor.onVibratorOffTimestamp)

        monitorInteractor.onVibratorOff()

        assertNotSame(0, monitorInteractor.onVibratorOffTimestamp)
    }

    /*
    onConnectionLost
     */

    @Test
    fun `onConnectionLost forces to resend monitor command after 20 secs`() = withFixedInstant {
        val subject = PublishSubject.create<GameToothbrushEvent>()
        initInteractor(subject)

        val connection = connectionWithMonitorCurrentSupport()

        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())

        subject.onNext(GameToothbrushEvent.VibratorOn(connection))

        subject.onNext(GameToothbrushEvent.VibratorOff(connection))

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        monitorInteractor.onConnectionLost()

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        advanceSeconds(5)

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        advanceSeconds(15)

        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())
    }

    @Test
    fun `onConnectionLost forces to resend monitor command 20 secs after first connection lost event, even if we receive consecutive onConnectionLost events`() = withFixedInstant {
        val subject = PublishSubject.create<GameToothbrushEvent>()
        initInteractor(subject)

        val connection = connectionWithMonitorCurrentSupport()

        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())

        subject.onNext(GameToothbrushEvent.VibratorOn(connection))

        subject.onNext(GameToothbrushEvent.VibratorOff(connection))

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        monitorInteractor.onConnectionLost()

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        advanceSeconds(5)

        monitorInteractor.onConnectionLost()

        advanceSeconds(5)

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        monitorInteractor.onConnectionLost()

        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())

        advanceSeconds(10)

        // event received right after reconnection
        subject.onNext(GameToothbrushEvent.VibratorOff(connection))

        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())
    }

    /*
    MAYBE SEND MONITOR CURRENT BRUSHING
     */
    @Test
    fun `maybeSendMonitorCurrentBrushing does nothing if shouldSendMonitorCurrentCommand is false`() {
        spyInteractor()

        doReturn(false).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        monitorInteractor.maybeSendMonitorCurrentBrushing(mock())

        assertNull(monitorInteractor.monitorCurrentDisposable)
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing invokes monitorCurrent if shouldSendMonitorCurrentCommand is true`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        verify(connection.brushing()).monitorCurrent()
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing stores monitorCurrentDisposable`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()

        assertNull(monitorInteractor.monitorCurrentDisposable)

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        assertNotNull(monitorInteractor.monitorCurrentDisposable)
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing nullifies monitorCurrentDisposable once Completable completes`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()

        val subject = CompletableSubject.create()
        whenever(connection.brushing().monitorCurrent()).thenReturn(subject)

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        assertNotNull(monitorInteractor.monitorCurrentDisposable)

        subject.onComplete()

        assertNull(monitorInteractor.monitorCurrentDisposable)
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing sets brushingIsMonitored to true once Completable completes`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()

        val subject = CompletableSubject.create()
        whenever(connection.brushing().monitorCurrent()).thenReturn(subject)

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        assertFalse(monitorInteractor.brushingIsMonitored.get())

        subject.onComplete()

        assertTrue(monitorInteractor.brushingIsMonitored.get())
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing nullifies monitorCurrentDisposable once Completable errors`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()

        val subject = CompletableSubject.create()
        whenever(connection.brushing().monitorCurrent()).thenReturn(subject)

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        assertNotNull(monitorInteractor.monitorCurrentDisposable)

        subject.onError(Exception("Test forced exception"))

        assertNull(monitorInteractor.monitorCurrentDisposable)
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing sets brushingIsMonitored to false when Completable fails after retries`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()
        monitorInteractor.brushingIsMonitored.set(true)

        val connection = createSensorTestConnection()

        whenever(connection.brushing().monitorCurrent()).thenReturn(Completable.error(Exception()))

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        assertFalse(monitorInteractor.brushingIsMonitored.get())
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing disposes previous monitorCurrentDisposable`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()

        val previousDisposable = mock<Disposable>()
        monitorInteractor.monitorCurrentDisposable = previousDisposable

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)

        verify(previousDisposable).forceDispose()
    }

    @Test
    fun `maybeSendMonitorCurrentBrushing retries MONITOR_CURRENT_RETRY_COUNT times`() {
        spyInteractor()

        doReturn(true).whenever(monitorInteractor).shouldSendMonitorCurrentCommand()

        val connection = createSensorTestConnection()
        var tryCount = 0
        val monitorCompletable = Completable.error(Exception("Timeout")).doOnError { tryCount++ }

        whenever(connection.brushing().monitorCurrent())
            .thenReturn(monitorCompletable)

        monitorInteractor.maybeSendMonitorCurrentBrushing(connection)
        // 1 try + MONITOR_CURRENT_RETRY_COUNT (re)tries
        assertEquals(1 + MONITOR_CURRENT_RETRY_COUNT.toInt(), tryCount)
    }

    /*
    IS MONITOR DEBOUNCE DELAY ELAPSED
     */
    @Test
    fun `isMonitorDebounceDelayElapsed returns false if less than MONITOR_CURRENT_DEBOUNCE_SECONDS have elapsed`() {
        initInteractor()

        monitorInteractor.onVibratorOffTimestamp =
            System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(MONITOR_CURRENT_DEBOUNCE_SECONDS - 1)

        assertFalse(monitorInteractor.isMonitorDebounceDelayElapsed())
    }

    @Test
    fun `isMonitorDebounceDelayElapsed returns true if onVibratorOffTimestamp is 0`() {
        initInteractor()

        assertTrue(monitorInteractor.isMonitorDebounceDelayElapsed())
    }

    @Test
    fun `isMonitorDebounceDelayElapsed returns true if more than MONITOR_CURRENT_DEBOUNCE_SECONDS have elapsed`() {
        initInteractor()

        monitorInteractor.onVibratorOffTimestamp =
            System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(MONITOR_CURRENT_DEBOUNCE_SECONDS + 1)

        assertTrue(monitorInteractor.isMonitorDebounceDelayElapsed())
    }

    /*
    shouldSendMonitorCurrentCommand (see method comment)
     */

    @Test
    fun `shouldSendMonitorCurrentCommand returns true when brushingIsMonitored is false and debounce period is not elapsed`() {
        spyInteractor()

        monitorInteractor.brushingIsMonitored.set(false)
        doReturn(false).whenever(monitorInteractor).isMonitorDebounceDelayElapsed()
        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())
    }

    @Test
    fun `shouldSendMonitorCurrentCommand returns false when brushingIsMonitored is true and debounce period is not elapsed`() {
        spyInteractor()

        monitorInteractor.brushingIsMonitored.set(true)
        doReturn(false).whenever(monitorInteractor).isMonitorDebounceDelayElapsed()
        assertFalse(monitorInteractor.shouldSendMonitorCurrentCommand())
    }

    @Test
    fun `shouldSendMonitorCurrentCommand returns true when brushingIsMonitored is false and debounce period is elapsed`() {
        spyInteractor()

        monitorInteractor.brushingIsMonitored.set(false)
        doReturn(true).whenever(monitorInteractor).isMonitorDebounceDelayElapsed()
        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())
    }

    @Test
    fun `shouldSendMonitorCurrentCommand returns true when brushingIsMonitored is true and debounce period is elapsed`() {
        spyInteractor()

        monitorInteractor.brushingIsMonitored.set(true)
        doReturn(true).whenever(monitorInteractor).isMonitorDebounceDelayElapsed()
        assertTrue(monitorInteractor.shouldSendMonitorCurrentCommand())
    }

    /*
    constants
     */

    @Test
    fun `value of MONITOR_CURRENT_RETRY_COUNT is 2`() {
        assertEquals(2L, MONITOR_CURRENT_RETRY_COUNT)
    }

    /*
    integration test
     */
    @Test
    fun `when connetion is lost for more than 20 seconds and then it's followed by VibratorOff, VibratorOn invoked monitorCurrent`() =
        withFixedInstant {
            val subject = PublishSubject.create<GameToothbrushEvent>()
            initInteractor(subject)

            val connection = connectionWithMonitorCurrentSupport()

            subject.onNext(GameToothbrushEvent.VibratorOn(connection))

            advanceSeconds(11)

            subject.onNext(GameToothbrushEvent.ConnectionLost(connection))

            advanceSeconds(30)

            subject.onNext(GameToothbrushEvent.VibratorOff(connection))

            advanceSeconds(11)

            verify(connection.brushing(), times(1)).monitorCurrent()

            subject.onNext(GameToothbrushEvent.VibratorOn(connection))

            verify(connection.brushing(), times(2)).monitorCurrent()
        }

    /*
    Utils
     */

    private fun connectionWithMonitorCurrentSupport(): KLTBConnection {
        return KLTBConnectionBuilder
            .createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withSupportMonitorCurrent()
            .build()
    }

    private fun advanceSeconds(seconds: Long) {
        Timber.tag("offline").d("advanceSeconds $seconds")
        TrustedClock.advanceTimeBy(seconds, ChronoUnit.SECONDS)

        testScheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
    }

    private fun initInteractor(eventSubject: PublishSubject<GameToothbrushEvent> = PublishSubject.create()) {
        monitorInteractor =
            MonitorCurrentBrushingInteractor(
                lifecycle = lifecycle,
                brushingToothbrushEventProvider = mockGameToothbrushEventProvider(
                    eventSubject
                ),
                resetScheduler = testScheduler
            )
    }

    private fun spyInteractor(eventSubject: PublishSubject<GameToothbrushEvent> = PublishSubject.create()) {
        initInteractor(eventSubject)

        monitorInteractor = spy(monitorInteractor)
    }
}
