/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller.kml

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.pirate.controller.World2Constant
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone12
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.PirateHelper
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.kml.RawData
import com.kolibree.kml.SupervisedBrushingAppContext12
import com.kolibree.kml.SupervisedCallback12
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration

class World2KmlControllerTest : BaseUnitTest() {

    private lateinit var worldController: World2KmlController

    private val checkupCalculator = mock<CheckupCalculator>()

    private val appContext = mock<SupervisedBrushingAppContext12>()

    private val pirateHelper = mock<PirateHelper>()

    private val callback = mock<SupervisedCallback12>()

    @Before
    fun setUp() {
        worldController = spy(World2KmlController(true, checkupCalculator, appContext, pirateHelper))
        doReturn(callback).whenever(worldController).getInternalCallback()
    }

    @Test
    fun `init invokes start on appContext`() {
        worldController.init(0)
        verify(appContext).start(callback)
    }

    @Test
    fun `setPrescribedZoneId sets currentZone and waitingForChangeLaneMessage to true`() {
        worldController.setPrescribedZoneId(World2Constant.KLPirateLevel2PrescribedZone.KLPirateLevel2PrescribedZoneIncIntBottom.ordinal)

        assertTrue(worldController.waitingForChangeLaneMessage.get())
        assertEquals(MouthZone12.LoIncInt12, worldController.currentZone.get())
    }

    @Test
    fun `onRawData does not invokes addRawData when shouldContinueProcessingData is false`() {
        worldController.onRawData(mock())

        verify(appContext, never()).addRawData(any(), any(), any())
    }

    @Test
    fun `onRawData does not invokes addRawData when shouldContinueProcessingData is true and currentZone is null`() {
        worldController.shouldContinueProcessingData.set(true)
        worldController.onRawData(mock())

        verify(appContext, never()).addRawData(any(), any(), any())
    }

    @Test
    fun `onRawData invokes addRawData with Running when shouldContinueProcessingData is true and currentZone is set and isPlaying true`() {
        val expectedZone = MouthZone12.LoMolRiInt12
        val rawSensorState = mock<RawSensorState>()
        val rawData = mock<RawData>()
        whenever(rawSensorState.convertToKmlRawData()).thenReturn(rawData)
        worldController.shouldContinueProcessingData.set(true)
        worldController.isPlaying.set(true)
        worldController.currentZone.set(expectedZone)
        worldController.onRawData(rawSensorState)

        verify(appContext).addRawData(rawData, PauseStatus.Running, expectedZone)
    }

    @Test
    fun `onRawData invokes addRawData with InPause when shouldContinueProcessingData is true and currentZone is set and isPlaying false`() {
        val expectedZone = MouthZone12.LoMolRiInt12
        val rawSensorState = mock<RawSensorState>()
        val rawData = mock<RawData>()
        whenever(rawSensorState.convertToKmlRawData()).thenReturn(rawData)
        worldController.shouldContinueProcessingData.set(true)
        worldController.isPlaying.set(false)
        worldController.currentZone.set(expectedZone)
        worldController.onRawData(rawSensorState)

        verify(appContext).addRawData(rawData, PauseStatus.InPause, expectedZone)
    }

    @Test
    fun `getBrushingData invokes createBrushingData and getter invokes processFullBrushing when isFullBrushingProcessingPossible true`() {
        val expectedTargetTime = 11
        val processedFullBrushing = mock<ProcessedBrushing16>()
        val processedBrushing = mock<ProcessedBrushing>()
        val expectedDurationMillis = 101L
        val expectedResult = mock<CreateBrushingData>()
        whenever(processedFullBrushing.durationInMilliseconds).thenReturn(expectedDurationMillis)
        whenever(appContext.processFullBrushing()).thenReturn(processedFullBrushing)
        whenever(appContext.isFullBrushingProcessingPossible).thenReturn(true)

        doReturn(processedBrushing).whenever(worldController).convertProcessBrushing16ToProcessBrushing(processedFullBrushing)

        doAnswer {
            val result = it.getArgument<() -> Pair<Duration, ProcessedBrushing>?>(1)()
            assertNotNull(result)
            assertEquals(expectedDurationMillis, result!!.first.toMillis())
            assertEquals(processedBrushing, result.second)
            expectedResult
        }.whenever(worldController).createBrushingData(eq(expectedTargetTime), any())

        assertEquals(expectedResult, worldController.getBrushingData(expectedTargetTime))
    }

    @Test
    fun `getBrushingData invokes createBrushingData and getter does not invoke processFullBrushing when isFullBrushingProcessingPossible false`() {
        val expectedTargetTime = 11
        val expectedResult = mock<CreateBrushingData>()
        whenever(appContext.isFullBrushingProcessingPossible).thenReturn(false)

        doAnswer {
            val result = it.getArgument<() -> Pair<Duration, ProcessedBrushing>?>(1)()
            assertNull(result)

            expectedResult
        }.whenever(worldController).createBrushingData(eq(expectedTargetTime), any())

        assertEquals(expectedResult, worldController.getBrushingData(expectedTargetTime))

        verify(appContext, never()).processFullBrushing()
    }
}
