/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrushupdate

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.ota.AvailableUpdateStep
import com.kolibree.android.app.ui.ota.OtaSteps
import com.kolibree.android.app.ui.ota.OtaSteps.Companion.EMPTY
import com.kolibree.android.app.ui.ota.OtaSteps.Companion.create
import com.kolibree.android.app.ui.ota.OtaUpdater
import com.kolibree.android.commons.AvailableUpdate.Companion.empty
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_COMPLETED
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_ERROR
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_REBOOTING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromAction
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromProgressiveAction
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createAndroidLess
import com.kolibree.android.test.mocks.OtaUpdates.createFirmwareUpdate
import com.kolibree.android.test.mocks.OtaUpdates.createGruUpdate
import com.kolibree.android.test.utils.randomPositiveInt
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class OtaUpdaterTest : BaseUnitTest() {

    private val otaUpdater = spy(OtaUpdater())

    /*
      adjustProgressFunction
       */

    @Test
    fun `adjustProgressFunction returns equal event if progressDividend is 1 and startingProgress is 0`() {
        val step = AvailableUpdateStep(createFirmwareUpdate(), 0, 1)
        val event =
            fromProgressiveAction(OTA_UPDATE_INSTALLING, 50)
        assertEquals(event, otaUpdater.adjustProgressFunction(event, step))
    }

    @Test
    fun `adjustProgressFunction returns startingProgress if event progress is 0`() {
        val startingProgress = 17
        val step =
            AvailableUpdateStep(createFirmwareUpdate(), startingProgress, 1)
        val event =
            fromProgressiveAction(OTA_UPDATE_INSTALLING, 0)
        assertEquals(
            fromProgressiveAction(OTA_UPDATE_INSTALLING, startingProgress),
            otaUpdater.adjustProgressFunction(event, step)
        )
    }

    @Test
    fun `adjustProgressFunction returns startingProgress plus progress divided by dividend, independently of update OtaUpdateAction`() {
        val startingProgress = 17
        val dividend = 2
        val step =
            AvailableUpdateStep(createFirmwareUpdate(), startingProgress, dividend)
        listOf(
            OTA_UPDATE_INSTALLING,
            OTA_UPDATE_REBOOTING,
            OTA_UPDATE_ERROR,
            OTA_UPDATE_COMPLETED
        ).forEach { action ->
            listOf(
                randomPositiveInt(maxValue = 100),
                randomPositiveInt(maxValue = 100),
                randomPositiveInt(maxValue = 100),
                100
            ).forEach { progress ->
                val event = OtaUpdateEvent(action = action, progress = progress)

                assertEquals(
                    event.copy(progress = startingProgress + progress / dividend),
                    otaUpdater.adjustProgressFunction(event, step)
                )
            }
        }
    }

    /*
  UPDATE FIRMWARE OBSERVABLE
   */
    @Test
    fun `updateObservable empty FW update returns OTA_UPDATE_COMPLETED`() {
        otaUpdater
            .updateObservable(mock(), empty(UpdateType.TYPE_FIRMWARE))
            .test()
            .assertValue(fromAction(OTA_UPDATE_COMPLETED))
            .assertComplete()
    }

    @Test
    fun `updateObservable empty GRUUpdate returns OTA_UPDATE_COMPLETED`() {
        otaUpdater
            .updateObservable(mock(), empty(UpdateType.TYPE_GRU))
            .test()
            .assertValue(fromAction(OTA_UPDATE_COMPLETED))
            .assertComplete()
    }

    /*
  updateToothbrushObservable
   */
    @Test
    fun `updateToothbrushObservable subscribes to observable returned by AvailableUpdatesFilter`() {
        val connection = createAndroidLess().withOTAAvailable().build()
        val subject = SingleSubject.create<OtaSteps>()

        doReturn(subject).whenever(otaUpdater).createOtaSteps(connection)
        otaUpdater.updateToothbrushObservable(connection).test()
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `updateToothbrushObservable otaSteps isEmpty immediatelyCompletes`() {
        val connection = createAndroidLess().withOTAAvailable().build()
        doReturn(Single.just(EMPTY)).whenever(otaUpdater).createOtaSteps(connection)

        otaUpdater.updateToothbrushObservable(connection).test().assertComplete()
    }

    @Test
    fun `updateToothbrushObservable singleStep subscribes to update observable for that step`() {
        val connection = createAndroidLess().withOTAAvailable().build()

        val steps = create(listOf(createFirmwareUpdate()))
        doReturn(Single.just(steps)).whenever(otaUpdater)
            .createOtaSteps(connection)
        val updateEventsSubject =
            PublishSubject.create<OtaUpdateEvent>()
        doReturn(updateEventsSubject)
            .whenever(otaUpdater)
            .updateObservable(connection, steps[0].availableUpdate)
        otaUpdater.updateToothbrushObservable(connection).test()
        assertTrue(updateEventsSubject.hasObservers())
    }

    @Test
    fun `updateToothbrushObservable multipleSteps starts second step only after first is completed`() {
        val connection = createAndroidLess().withOTAAvailable().build()

        val gruUpdate = createGruUpdate()
        val firmwareUpdate = createFirmwareUpdate()
        val steps = create(listOf(firmwareUpdate, gruUpdate))
        doReturn(Single.just(steps)).whenever(otaUpdater).createOtaSteps(connection)
        val firstStepSubject = PublishSubject.create<OtaUpdateEvent>()
        doReturn(firstStepSubject).whenever(otaUpdater)
            .updateObservable(connection, steps[0].availableUpdate)
        val secondStepSubject =
            PublishSubject.create<OtaUpdateEvent>()
        doReturn(secondStepSubject).whenever(otaUpdater)
            .updateObservable(connection, steps[1].availableUpdate)
        otaUpdater.updateToothbrushObservable(connection).test()
        assertTrue(firstStepSubject.hasObservers())
        assertFalse(secondStepSubject.hasObservers())
        firstStepSubject.onComplete()
        assertTrue(secondStepSubject.hasObservers())
    }

    @Test
    fun `updateToothbrushObservable maps events through adjustProgressFunction`() {
        val connection = createAndroidLess().withOTAAvailable().build()

        val steps = create(listOf(createFirmwareUpdate()))
        doReturn(Single.just(steps)).whenever(otaUpdater).createOtaSteps(connection)
        val step = steps[0]
        val eventsSubject = PublishSubject.create<OtaUpdateEvent>()
        doReturn(eventsSubject).whenever(otaUpdater)
            .updateObservable(connection, step.availableUpdate)
        val observer: TestObserver<OtaUpdateEvent> =
            otaUpdater.updateToothbrushObservable(connection).test()
        val event = fromAction(OTA_UPDATE_REBOOTING)
        val expectedEvent =
            fromAction(OTA_UPDATE_COMPLETED)
        doReturn(expectedEvent).whenever(otaUpdater).adjustProgressFunction(event, step)
        eventsSubject.onNext(event)
        observer.assertValue(expectedEvent)
    }

    @Test
    fun `updateToothbrushObservable does not emit duplicated consecutive events`() {
        val connection = createAndroidLess().withOTAAvailable().build()

        val steps = create(listOf(createFirmwareUpdate()))
        doReturn(Single.just(steps)).whenever(otaUpdater).createOtaSteps(connection)
        val step = steps[0]
        val eventsSubject = PublishSubject.create<OtaUpdateEvent>()
        doReturn(eventsSubject).whenever(otaUpdater)
            .updateObservable(connection, step.availableUpdate)
        val observer: TestObserver<OtaUpdateEvent> =
            otaUpdater.updateToothbrushObservable(connection).test()
        val event = fromAction(OTA_UPDATE_REBOOTING)
        val expectedEvent =
            fromAction(OTA_UPDATE_COMPLETED)
        doReturn(expectedEvent).whenever(otaUpdater).adjustProgressFunction(event, step)
        eventsSubject.onNext(event)
        eventsSubject.onNext(event)
        eventsSubject.onNext(event)
        eventsSubject.onNext(event)
        observer.assertValueCount(1).assertValue(expectedEvent)
    }

    @Test
    fun `updateToothbrushObservable does not emit OTA_UPDATE_COMPLETED event`() {
        val connection = createAndroidLess().withOTAAvailable().build()

        val steps = create(listOf(createFirmwareUpdate()))
        doReturn(Single.just(steps)).whenever(otaUpdater)
            .createOtaSteps(connection)
        val (availableUpdate) = steps[0]
        val eventsSubject = PublishSubject.create<OtaUpdateEvent>()
        doReturn(eventsSubject).whenever(otaUpdater).updateObservable(connection, availableUpdate)
        val observer = otaUpdater.updateToothbrushObservable(connection).test()
        observer.assertNoValues()
        eventsSubject.onNext(fromAction(OTA_UPDATE_COMPLETED))
        observer.assertNoValues()
        verify(otaUpdater, never()).adjustProgressFunction(any(), any())
    }
}
