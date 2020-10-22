/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.shared

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.bi.Contract
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.exception.ProcessedBrushingNotAvailableException
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.FreeBrushingAppContext
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.PlaqlessData
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.kml.RawData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Duration

class TestBrushingUseCaseImplTest : BaseUnitTest() {

    private val checkupCalculator: CheckupCalculator = mock()
    private val appContext: FreeBrushingAppContext = mock()
    private val avroCreator: KmlAvroCreator = mock()

    private lateinit var useCase: TestBrushingUseCaseImpl

    override fun setup() {
        super.setup()
        useCase = spy(
            TestBrushingUseCaseImpl(
                checkupCalculator,
                appContext,
                avroCreator
            )
        )
    }

    @Test
    fun `started is false after use case creation`() {
        assertFalse(useCase.started.get())
        verify(appContext, never()).start()
    }

    @Test
    fun `started is true after first onRawData call`() {
        useCase.onRawData(false, mock())
        useCase.onRawData(false, mock())
        useCase.onRawData(false, mock())

        assertTrue(useCase.started.get())
        verify(appContext, times(1)).start()
    }

    @Test
    fun `started is true after first onPlaqlessData call`() {
        useCase.onPlaqlessData(false, mock())
        useCase.onPlaqlessData(false, mock())
        useCase.onPlaqlessData(false, mock())

        assertTrue(useCase.started.get())
        verify(appContext, times(1)).start()
    }

    @Test
    fun `started is true after first onPlaqlessRawData call`() {
        useCase.onPlaqlessRawData(false, mock())
        useCase.onPlaqlessRawData(false, mock())
        useCase.onPlaqlessRawData(false, mock())

        assertTrue(useCase.started.get())
        verify(appContext, times(1)).start()
    }

    @Test
    fun `notifyReconnection invokes appContext`() {
        useCase.notifyReconnection()
        verify(appContext).notifyReconnection()
    }

    @Test
    fun `onRawData passes isPlaying and sensor state to appContext in a correct way`() {
        val sensorState: RawSensorState = mock()
        val rawData: RawData = mock()
        doReturn(rawData).whenever(sensorState).convertToKmlRawData()

        useCase.onRawData(false, sensorState)
        verify(appContext).addRawData(rawData, PauseStatus.InPause)
        useCase.onRawData(true, sensorState)
        verify(appContext).addRawData(rawData, PauseStatus.Running)
    }

    @Test
    fun `onPlaqlessData passes isPlaying and sensor state to appContext in a correct way`() {
        val sensorState: PlaqlessSensorState = mock()
        val plaqlessData: PlaqlessData = mock()
        doReturn(plaqlessData).whenever(sensorState).convertToKmlPlaqlessData()

        useCase.onPlaqlessData(false, sensorState)
        verify(appContext).addPlaqlessData(plaqlessData, PauseStatus.InPause)
        useCase.onPlaqlessData(true, sensorState)
        verify(appContext).addPlaqlessData(plaqlessData, PauseStatus.Running)
    }

    @Test
    fun `onPlaqlessRawData passes isPlaying and sensor state to appContext in a correct way`() {
        val sensorState: PlaqlessRawSensorState = mock()
        val plaqlessData: RawData = mock()
        doReturn(plaqlessData).whenever(sensorState).convertToKmlRawData()

        useCase.onPlaqlessRawData(false, sensorState)
        verify(appContext).addRawData(plaqlessData, PauseStatus.InPause)
        useCase.onPlaqlessRawData(true, sensorState)
        verify(appContext).addRawData(plaqlessData, PauseStatus.Running)
    }

    /*
    onOverpressureState
     */

    @Test
    fun `onOverpressureState updates appContext with expected parameters`() {
        val expectedSensorState = true
        val expectedNotificationState = false
        val sensorState = OverpressureState(expectedSensorState, expectedNotificationState)

        useCase.onOverpressureState(sensorState)
        verify(appContext).addOverpressureData(expectedSensorState, expectedNotificationState)
    }

    @Test
    fun `onOverpressureState doesn't start KML context if it is already started`() {
        useCase.started.set(true)
        useCase.onOverpressureState(
            OverpressureState(
                detectorIsActive = true,
                uiNotificationIsActive = true
            )
        )
        verify(appContext, never()).start()
    }

    @Test
    fun `onOverpressureState starts KML context if it has not started yet, and flags it to true`() {
        useCase.started.set(false)
        useCase.onOverpressureState(
            OverpressureState(
                detectorIsActive = true,
                uiNotificationIsActive = true
            )
        )
        verify(appContext).start()
        assertTrue(useCase.started.get())
    }

    @Test
    fun `createBrushingData throws an exception if isFullBrushingProcessingPossible returns false`() {
        doReturn(false).whenever(appContext).isFullBrushingProcessingPossible

        val observer = useCase.createBrushingData(mock()).test()
        observer.assertError(ProcessedBrushingNotAvailableException::class.java)
    }

    @Test
    fun `createBrushingData produces correct brushing if isFullBrushingProcessingPossible returns true`() {
        val connection: KLTBConnection = mock()
        val (checkupData, processedData) = mockCreateBrushingData()
        doReturn(true).whenever(appContext).isFullBrushingProcessingPossible
        whenever(appContext.getAvro(any())).thenReturn(mock())
        whenever(avroCreator.createBrushingSession(connection, Contract.ActivityName.FREE_BRUSHING))
            .thenReturn(Single.just(mock()))
        whenever(avroCreator.submitAvroData(any()))
            .thenReturn(Completable.complete())

        val observer = useCase.createBrushingData(connection).test()
        observer.assertNoErrors()
        verify(useCase).initBrushingData(checkupData)
        verify(avroCreator).createBrushingSession(connection, Contract.ActivityName.FREE_BRUSHING)
        verify(avroCreator).submitAvroData(any())
    }

    @Test
    fun `createBrushingData returns error if initBrushingData throws exception`() {
        val expectedException = RuntimeException()
        val connection: KLTBConnection = mock()
        val (checkupData, processedData) = mockCreateBrushingData()

        doReturn(true).whenever(appContext).isFullBrushingProcessingPossible
        doThrow(expectedException).whenever(useCase).initBrushingData(checkupData)

        val observer = useCase.createBrushingData(connection).test()
        observer.assertError(expectedException)
        verify(avroCreator, never()).createBrushingSession(
            connection,
            Contract.ActivityName.FREE_BRUSHING
        )
        verify(avroCreator, never()).submitAvroData(any())
    }

    @Test
    fun `createBrushingData recovers from error in avroCreator createBrushingSession`() {
        val expectedException = RuntimeException()
        val connection: KLTBConnection = mock()
        val (checkupData, processedData) = mockCreateBrushingData()

        doReturn(true).whenever(appContext).isFullBrushingProcessingPossible
        whenever(appContext.getAvro(any())).thenReturn(mock())
        whenever(avroCreator.createBrushingSession(connection, Contract.ActivityName.FREE_BRUSHING))
            .thenReturn(Single.error(expectedException))

        val observer = useCase.createBrushingData(connection).test()
        observer.assertNoErrors()
        verify(avroCreator, never()).submitAvroData(any())
    }

    @Test
    fun `createBrushingData recovers from error in avroCreator submitAvroData`() {
        val expectedException = RuntimeException()
        val connection: KLTBConnection = mock()
        val (checkupData, processedData) = mockCreateBrushingData()

        doReturn(true).whenever(appContext).isFullBrushingProcessingPossible
        whenever(appContext.getAvro(any())).thenReturn(mock())
        whenever(
            avroCreator.createBrushingSession(connection, Contract.ActivityName.FREE_BRUSHING)
        ).thenReturn(Single.just(mock()))
        whenever(avroCreator.submitAvroData(any())).thenThrow(expectedException)

        val observer = useCase.createBrushingData(connection).test()
        observer.assertNoErrors()
    }

    private fun mockCreateBrushingData(): Pair<CheckupData, String> {
        val processedBrushing16: ProcessedBrushing16 = mock()
        doReturn(processedBrushing16).whenever(appContext).processFullBrushing()
        val processedBrushing: ProcessedBrushing = mock()
        doReturn(processedBrushing).whenever(processedBrushing16).toProcessedBrushing()
        val processedData = "{ \"PROCESSED_DATA\":\"true\"}"
        doReturn(processedData).whenever(processedBrushing).toJSON()
        val checkupData: CheckupData = mock()
        whenever(checkupData.duration).thenReturn(Duration.ofSeconds(20))
        whenever(checkupData.dateTime).thenReturn(TrustedClock.getNowOffsetDateTime())
        whenever(checkupData.surfacePercentage).thenReturn(90)
        doReturn(checkupData).whenever(checkupCalculator).calculateCheckup(processedBrushing)
        return Pair(checkupData, processedData)
    }
}
