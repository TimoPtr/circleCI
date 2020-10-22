/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromProgressiveAction
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.CommandSet
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PUSH_DSP
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushDfuUpdater
import com.kolibree.android.sdk.test.mockDvpCommand
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import timber.log.Timber

class ToothbrushDspUpdaterTest : BaseUnitTest() {
    private val connection: InternalKLTBConnection =
        KLTBConnectionBuilder.createAndroidLess().build()
    private val bleDriver: BleDriver = mock()
    private val dfuUpdater: ToothbrushDfuUpdater = mock()
    private val wakeUpDspUseCase: WakeUpDspUseCase = mock()
    private val delayScheduler = TestScheduler()
    private val timeoutScheduler = TestScheduler()

    private val updater = ToothbrushDspUpdater(
        connection = connection,
        bleDriver = bleDriver,
        dfuUpdater = dfuUpdater,
        wakeUpDspUseCase = wakeUpDspUseCase,
        timeoutScheduler = timeoutScheduler,
        delayScheduler = delayScheduler
    )

    private val updateType = AvailableUpdate.empty(UpdateType.TYPE_DSP)

    private val dfuUpdateSubject = dfuUpdateSubject()
    private val wakeDspSubject = wakeDspSubject()
    private val pushDspSubject = bleDriver.mockDvpCommand(CommandSet.pushDspFirmware())

    @Test
    fun `when dfu step fails, dsp update fails`() {
        val observable = updater.update(updateType).test()
            .assertNoErrors()

        assertTrue(dfuUpdateSubject.hasObservers())
        dfuUpdateSubject.onError(TestForcedException())

        observable.assertError(TestForcedException::class.java)
    }

    @Test
    fun `when dfu step emits an event, the emitted event divides its progress by two`() {
        val observable = updater.update(updateType).test()
            .assertNoErrors()

        val progress = 76
        val event = fromProgressiveAction(OTA_UPDATE_INSTALLING, progress)
        dfuUpdateSubject.onNext(event)

        val expectedProgress = progress / 2
        observable.assertValue(fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress))
    }

    @Test
    fun `when dfu step completes, then attempt to wake up dsp`() {
        updater.update(updateType).test()

        dfuUpdateSubject.onComplete()

        assertTrue(wakeDspSubject.hasObservers())
    }

    @Test
    fun `when wake up dsp completes, then send push dsp command`() {
        updater.update(updateType).test()

        dfuUpdateSubject.onComplete()

        wakeDspSubject.onComplete()

        assertTrue(pushDspSubject.hasObservers())
    }

    @Test
    fun `when push dsp command completes, then the stream starts emitting the progress reported by DVP characteristic`() {
        val observer = updater.update(updateType).test()

        dfuUpdateSubject.onComplete()

        wakeDspSubject.onComplete()

        val dvpProcessor = dvpNotificationsProcessor()

        // command returned value is ignored
        pushDspSubject.onSuccess(PayloadReader(byteArrayOf()))

        val progress = 48
        dvpProcessor.onNext(pushDspPayload(progress))

        // dsp progress is translated like this in PushDspState.adjustDspProgress()
        val expectedProgress = 50 + progress / 2
        observer
            .assertValue(fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress))
            .assertNotComplete()
    }

    @Test
    fun `when push dsp command completes and 51 - 00 - 00 - 64 is emitted on DVP characteristic, stream completes`() {
        val observer = updater.update(updateType).test()

        dfuUpdateSubject.onComplete()

        wakeDspSubject.onComplete()

        val dvpProcessor = dvpNotificationsProcessor()

        // command returned value is ignored
        pushDspSubject.onSuccess(PayloadReader(byteArrayOf()))

        dvpProcessor.onNext(pushDspPayload(100))

        observer.assertComplete()
    }

    @Test
    fun `when push dsp command completes and Recoverable Error is emitted on DVP characteristic, retry wakeUp + pushDsp with a small delay`() {
        PushDspLastStatus.values()
            .filter { it.isRecoverableByPushError() }
            .forEach { error ->
                Timber.d("\n\n\nTesting $error\n\n\n")
                var subscriptionsToWakeUpDsp = 0
                whenever(wakeUpDspUseCase.wakeUpCompletable()).thenReturn(Completable.create {
                    subscriptionsToWakeUpDsp++

                    it.onComplete()
                })

                var subscriptionsToPushDsp = 0
                whenever(bleDriver.setAndGetDeviceParameterOnce(CommandSet.pushDspFirmware()))
                    .thenReturn(Single.create {
                        subscriptionsToPushDsp++

                        it.onSuccess(PayloadReader(byteArrayOf()))
                    })

                var subscriptionsToDVP = 0
                whenever(bleDriver.deviceParametersCharacteristicChangedStream())
                    .thenReturn(
                        Flowable.create({ e: FlowableEmitter<ByteArray> ->
                            subscriptionsToDVP++

                            e.onNext(pushDspPayload(lastStatus = error))
                        }, BackpressureStrategy.BUFFER)
                    )

                val observer = updater.update(updateType).test()

                dfuUpdateSubject.onComplete()

                observer.assertNotComplete().assertNoErrors()

                assertFalse(dfuUpdateSubject.hasObservers())

                assertEquals(1, subscriptionsToWakeUpDsp)
                assertEquals(1, subscriptionsToPushDsp)
                assertEquals(1, subscriptionsToDVP)

                advanceDelaySeconds()

                assertEquals(2, subscriptionsToWakeUpDsp)
                assertEquals(2, subscriptionsToPushDsp)
                assertEquals(2, subscriptionsToDVP)

                observer.dispose()
            }
    }

    @Test
    fun `when DVP emits RecoverableError after 5 retries, stop retrying and emit error`() {
        val error = PushDspLastStatus.values().let {
            it.shuffle()
            it.first { it.isRecoverableByPushError() }
        }
        Timber.d("Testing $error")

        var subscriptionsToWakeUpDsp = 0
        whenever(wakeUpDspUseCase.wakeUpCompletable()).thenReturn(Completable.create {
            subscriptionsToWakeUpDsp++

            it.onComplete()
        })

        var subscriptionsToPushDsp = 0
        whenever(bleDriver.setAndGetDeviceParameterOnce(CommandSet.pushDspFirmware()))
            .thenReturn(Single.create {
                subscriptionsToPushDsp++

                it.onSuccess(PayloadReader(byteArrayOf()))
            })

        var subscriptionsToDVP = 0
        whenever(bleDriver.deviceParametersCharacteristicChangedStream())
            .thenReturn(
                Flowable.create({ e: FlowableEmitter<ByteArray> ->
                    subscriptionsToDVP++

                    e.onNext(pushDspPayload(lastStatus = error))
                }, BackpressureStrategy.BUFFER)
            )

        val observer = updater.update(updateType).test()

        dfuUpdateSubject.onComplete()

        observer.assertNotComplete().assertNoErrors()

        assertFalse(dfuUpdateSubject.hasObservers())

        assertEquals(1, subscriptionsToWakeUpDsp)
        assertEquals(1, subscriptionsToPushDsp)
        assertEquals(1, subscriptionsToDVP)

        advanceDelaySeconds()

        assertEquals(2, subscriptionsToWakeUpDsp)
        assertEquals(2, subscriptionsToPushDsp)
        assertEquals(2, subscriptionsToDVP)

        advanceDelaySeconds()

        assertEquals(3, subscriptionsToWakeUpDsp)
        assertEquals(3, subscriptionsToPushDsp)
        assertEquals(3, subscriptionsToDVP)

        advanceDelaySeconds()

        assertEquals(4, subscriptionsToWakeUpDsp)
        assertEquals(4, subscriptionsToPushDsp)
        assertEquals(4, subscriptionsToDVP)

        advanceDelaySeconds()

        observer.assertError(UnrecoverableDspUpdateException)
    }

    @Test
    fun `when push dsp command completes and Unrecoverable Error is emitted on DVP characteristic, never retry wakeUp and emit error`() {
        dfuUpdateSubject.onComplete()

        wakeDspSubject.onComplete()

        val dvpProcessor = dvpNotificationsProcessor()

        PushDspLastStatus.values()
            .filter { it.isUnrecoverableError() }
            .forEach { error ->
                Timber.d("\n\n\nTesting $error\n\n\n")

                val observer = updater.update(updateType).test()

                dfuUpdateSubject.onComplete()

                // command returned value is ignored
                pushDspSubject.onSuccess(PayloadReader(byteArrayOf()))

                dvpProcessor.onNext(pushDspPayload(lastStatus = error))

                observer.assertNotComplete().assertError(UnrecoverableDspUpdateException)

                observer.dispose()
            }
    }

    /*
    Utils
     */
    private fun advanceDelaySeconds(value: Long = 2L) {
        delayScheduler.advanceTimeBy(value, TimeUnit.SECONDS)
    }

    private fun dfuUpdateSubject(): PublishSubject<OtaUpdateEvent> {
        return PublishSubject.create<OtaUpdateEvent>().apply {
            whenever(dfuUpdater.update(any())).thenReturn(this)
        }
    }

    private fun wakeDspSubject(): CompletableSubject {
        return CompletableSubject.create().apply {
            whenever(wakeUpDspUseCase.wakeUpCompletable()).thenReturn(this)
        }
    }

    private fun dvpNotificationsProcessor(): PublishProcessor<ByteArray> {
        return PublishProcessor.create<ByteArray>().apply {
            whenever(bleDriver.deviceParametersCharacteristicChangedStream())
                .thenReturn(this)
        }
    }

    private fun pushDspPayload(
        progress: Int = 0,
        lastStatus: PushDspLastStatus = PushDspLastStatus.NO_ERROR
    ) =
        byteArrayOf(
            DEVICE_PARAMETERS_PUSH_DSP,
            if (progress < 100 && lastStatus == PushDspLastStatus.NO_ERROR) 1 else 0,
            lastStatus.value,
            progress.toByte()
        )
}
