/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.kml.ProcessedBrushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration

class WorldControllerTest : BaseUnitTest() {

    private lateinit var worldController: WorldController

    private val checkupCalculator = mock<CheckupCalculator>()

    @Before
    fun setUp() {
        worldController = spy(object : WorldController(true, checkupCalculator) {
            override fun setPrescribedZoneId(prescribedZoneId: Int) {
                // no op
            }

            override fun init(targetBrushingTime: Int) {
                // no op
            }
        })

        doNothing().whenever(worldController).changeLane(any())
    }

    @Test
    fun `run sets isPlaying to true`() {
        worldController.run()
        assertTrue(worldController.isPlaying.get())
    }

    @Test
    fun `pause sets isPlaying to false`() {
        worldController.pause()
        assertFalse(worldController.isPlaying.get())
    }

    @Test
    fun `stop sets isPlaying to false`() {
        worldController.pause()
        assertFalse(worldController.isPlaying.get())
    }

    @Test
    fun `addGoldEarned add gold to current value`() {
        worldController.gold = 50
        worldController.addGoldEarned(1)
        assertEquals(51, worldController.gold)
    }

    @Test
    fun `shouldChangeLane sets waitingForChangeLaneMessage to false`() {
        worldController.shouldChangeLane()
        assertFalse(worldController.waitingForChangeLaneMessage.get())
    }

    @Test
    fun `maybeChangeLane invokes changeLane if waitingForChangeLaneMessage equals false`() {
        val expectedLane = "lane"
        worldController.waitingForChangeLaneMessage.set(false)

        worldController.maybeChangeLane(expectedLane, "", false)

        verify(worldController).changeLane(expectedLane)
    }

    @Test
    fun `maybeChangeLane does not invoke changeLane if waitingForChangeLaneMessage equals true`() {
        val expectedLane = "lane"
        worldController.waitingForChangeLaneMessage.set(true)

        worldController.maybeChangeLane(expectedLane, "", false)

        verify(worldController, never()).changeLane(any())
    }

    @Test
    fun `createBrushingData returns CreateBrushingData valid when processedBrushingGetter returns null`() {
        TrustedClock.setFixedDate()
        worldController.gold = 5
        val dateTime = TrustedClock.getNowOffsetDateTime()
        val data = worldController.createBrushingData(10) { null }

        assertEquals(0, data.coverage)
        assertEquals("{}", data.getProcessedData())
        assertEquals(0, data.duration)
        assertEquals(GameApiConstants.GAME_GO_PIRATE, data.game)
        assertEquals(dateTime, data.date)
        assertEquals(10, data.goalDuration)
        assertEquals(5, data.coins)
    }

    @Test
    fun `createBrushingData sets shouldContinueProcessing to false`() {
        worldController.shouldContinueProcessingData.set(true)
        val data = worldController.createBrushingData(10) { null }
        assertFalse(worldController.shouldContinueProcessingData.get())
    }

    @Test
    fun `createBrushingData invokes processedBrushingGetter and set fields in createbrushingData`() {
        TrustedClock.setFixedDate()
        worldController.gold = 5
        val dateTime = TrustedClock.getNowOffsetDateTime()
        val processBrushing = mock<ProcessedBrushing>()
        val expectedDuration = Duration.ofMinutes(2)
        val expectedProcessedData = """{"value":0}"""
        val checkup = mock<CheckupData>()
        val expectedSurface = 98

        whenever(checkup.surfacePercentage).thenReturn(expectedSurface)
        whenever(checkupCalculator.calculateCheckup(processBrushing)).thenReturn(checkup)
        whenever(processBrushing.toJSON()).thenReturn(expectedProcessedData)
        val data = worldController.createBrushingData(10) { Pair(expectedDuration, processBrushing) }

        assertEquals(expectedSurface, data.coverage)
        assertEquals(expectedProcessedData, data.getProcessedData())
        assertEquals(expectedDuration, data.durationObject)
        assertEquals(GameApiConstants.GAME_GO_PIRATE, data.game)
        assertEquals(dateTime, data.date)
        assertEquals(10, data.goalDuration)
        assertEquals(5, data.coins)
    }
}
