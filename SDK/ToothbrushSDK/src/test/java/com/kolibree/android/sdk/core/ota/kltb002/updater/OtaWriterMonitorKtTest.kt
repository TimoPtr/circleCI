package com.kolibree.android.sdk.core.ota.kltb002.updater

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_COMPLETED
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.error.FailureReason
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class OtaWriterMonitorKtTest : BaseUnitTest() {

    private val connectionStateMonitor = mock<ConnectionStateMonitor>()

    private val timeScheduler = TestScheduler()

    @Before
    override fun setup() {
        super.setup()
        doReturn(timeScheduler).whenever(connectionStateMonitor).timeControlScheduler
        doReturn(Completable.complete()).whenever(connectionStateMonitor).waitForActiveConnection()
    }

    @Test
    fun `monitorOtaWriteObservable passes through update values if connection is in OTA state`() {
        doReturn(Observable.just(KLTBConnectionState.OTA))
            .whenever(connectionStateMonitor)
            .connectionStateObservable()

        val expectedEvents = arrayOf(
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 99)
        )
        val publishSubject = PublishSubject.create<OtaUpdateEvent>()

        val testSubscriber =
            monitorOtaWriteObservable(connectionStateMonitor, publishSubject).test()
        expectedEvents.forEach { event -> publishSubject.onNext(event) }

        testSubscriber.assertValues(*expectedEvents)
    }

    @Test
    fun `monitorOtaWriteObservable throws exception if connection is not in OTA state`() {
        doReturn(Observable.just(KLTBConnectionState.TERMINATING))
            .whenever(connectionStateMonitor)
            .connectionStateObservable()

        val expectedEvents = arrayOf(
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 99)
        )
        val publishSubject = PublishSubject.create<OtaUpdateEvent>()

        val testSubscriber =
            monitorOtaWriteObservable(connectionStateMonitor, publishSubject).test()
        expectedEvents.forEach { event -> publishSubject.onNext(event) }

        testSubscriber.assertNoValues()
        testSubscriber.assertError(FailureReason::class.java)
        testSubscriber.assertErrorMessage("Connection is not in OTA state!")
    }

    @Test
    fun `monitorOtaWriteObservable completes if any event has 100% progress`() {
        doReturn(Observable.just(KLTBConnectionState.OTA))
            .whenever(connectionStateMonitor)
            .connectionStateObservable()

        val events = arrayOf(
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 100)
        )

        val expectedEvents = arrayOf(
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50)
        )

        val publishSubject = PublishSubject.create<OtaUpdateEvent>()

        val testSubscriber =
            monitorOtaWriteObservable(connectionStateMonitor, publishSubject).test()
        events.forEach { event -> publishSubject.onNext(event) }
        publishSubject.onNext(OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 9))
        publishSubject.onNext(OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 12))
        publishSubject.onNext(OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 99))
        publishSubject.onNext(OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_COMPLETED, 100))

        testSubscriber.assertComplete()
        testSubscriber.assertValues(*expectedEvents)

        /*
        I was seeing deadlocks when using takeUntil, which completes _after_ emitting the item.

        takeWhile completes _before_ emitting the item, which makes an important difference here
         */
        testSubscriber.assertValueCount(expectedEvents.size)
    }

    @Test
    fun `monitorOtaWriteObservable does not emit duplicates`() {
        doReturn(Observable.just(KLTBConnectionState.OTA))
            .whenever(connectionStateMonitor)
            .connectionStateObservable()

        val events = arrayOf(
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 70)
        )

        val expectedEvents = arrayOf(
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 10),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 50),
            OtaUpdateEvent.fromProgressiveAction(OTA_UPDATE_INSTALLING, 70)
        )
        val publishSubject = PublishSubject.create<OtaUpdateEvent>()

        val testSubscriber =
            monitorOtaWriteObservable(connectionStateMonitor, publishSubject).test()
        events.forEach { event -> publishSubject.onNext(event) }

        testSubscriber.assertValues(*expectedEvents)
    }
}
