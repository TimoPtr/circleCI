/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.controller.kml.CLEANNESS_FULL
import com.kolibree.android.coachplus.controller.kml.CoachPlusKmlControllerImpl
import com.kolibree.android.coachplus.feedback.CoachPlusFeedbackMapper
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.kml.CharVector
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.PlaqlessData
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.kml.RawData
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.kml.SupervisedCallback16
import com.kolibree.kml.SupervisedResult16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import javax.inject.Provider
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Duration

class CoachPlusKmlControllerImplTest : BaseUnitTest() {

    private val checkupCalculator = mock<CheckupCalculator>()
    private val appContext = mock<SupervisedBrushingAppContext16>()
    private val appContextProvider = mock<Provider<SupervisedBrushingAppContext16>>()
    private val feedbackMapper = mock<CoachPlusFeedbackMapper>()
    private val zoneDurationAdjuster = mock<ZoneDurationAdjuster>()
    private val callbackMock = mock<SupervisedCallback16>()

    private lateinit var controller: CoachPlusKmlControllerImpl

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        setupMocks()
    }

    private inner class CoachPlusKmlControllerImplStub(
        goalBrushingDuration: Duration,
        tickPeriod: Long,
        maxFailTime: Long = DEFAULT_MAX_FAIL_TIME_MS,
        checkupCalculator: CheckupCalculator,
        supervisedBrushingAppContextProvider: Provider<SupervisedBrushingAppContext16>,
        coachPlusFeedbackMapper: CoachPlusFeedbackMapper,
        durationAdjuster: ZoneDurationAdjuster?
    ) : CoachPlusKmlControllerImpl(
        goalBrushingDuration,
        tickPeriod,
        maxFailTime,
        checkupCalculator,
        supervisedBrushingAppContextProvider,
        coachPlusFeedbackMapper,
        durationAdjuster
    ) {
        override val callback: SupervisedCallback16
            get() = callbackMock

        init {
            whenever(callback.onSupervisedResult(any())).then {
                val result = it.getArgument<SupervisedResult16>(0)
                lastSupervisedResult16.set(result)
            }
        }
    }

    private fun setupMocks(
        goalDuration: Duration = Duration.ofMillis(1),
        tickPeriod: Long = 1,
        maxFailTime: Long = 1,
        zoneDurationAdjuster: ZoneDurationAdjuster? = this.zoneDurationAdjuster
    ) {
        whenever(appContextProvider.get()).thenReturn(appContext)
        controller = spy(
            CoachPlusKmlControllerImplStub(
                goalDuration,
                tickPeriod,
                maxFailTime,
                checkupCalculator,
                appContextProvider,
                feedbackMapper,
                zoneDurationAdjuster
            )
        )
    }

    /*
      Init
     */
    @Test
    fun `create invokes start on appContext`() {
        // verify(appContext).start(controller.callback)
    }

    /*
      onRawData
     */

    @Test
    fun `onRawData invokes addRawData`() {
        val rawDataSensorState = mock<RawSensorState>()
        val rawData = mock<RawData>()
        whenever(rawDataSensorState.convertToKmlRawData()).thenReturn(rawData)
        doNothing().whenever(controller).onRawData(any(), any<RawData>())
        controller.onRawData(true, rawDataSensorState)
        verify(rawDataSensorState).convertToKmlRawData()
        verify(controller).onRawData(true, rawData)
    }

    /*
      onPlaqlessRawData
     */

    @Test
    fun `onPlaqlessRawData invokes onRawData`() {
        val mockSensorState = mock<PlaqlessRawSensorState>()
        val mockRawData = mock<RawData>()
        whenever(mockSensorState.convertToKmlRawData()).thenReturn(mockRawData)
        doNothing().whenever(controller).onRawData(true, mockRawData)
        controller.onPlaqlessRawData(true, mockSensorState)

        verify(controller).onRawData(true, mockRawData)
        verify(appContext, never()).addRawData(any(), any(), any())
    }

    @Test
    fun `onPlaqlessRawData invokes appContext addRawData when shouldContinueProcessingData true with Running if play true`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessRawSensorState = mock<PlaqlessRawSensorState>()
        val rawData = mock<RawData>()
        whenever(plaqlessRawSensorState.convertToKmlRawData()).thenReturn(rawData)
        controller.onPlaqlessRawData(true, plaqlessRawSensorState)

        verify(appContext).addRawData(eq(rawData), eq(PauseStatus.Running), any())
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onPlaqlessRawData invokes appContext addRawData when shouldContinueProcessingData true with InPause if play false`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessRawSensorState = mock<PlaqlessRawSensorState>()
        val rawData = mock<RawData>()
        whenever(plaqlessRawSensorState.convertToKmlRawData()).thenReturn(rawData)

        controller.onPlaqlessRawData(false, plaqlessRawSensorState)

        verify(appContext).addRawData(eq(rawData), eq(PauseStatus.InPause), any())
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onPlaqlessRawData invokes appContext addRawData when shouldContinueProcessingData true with zone equals to SEQUENCE index`() {
        controller.shouldContinueProcessingData.set(true)
        controller.currentZoneIndex = 2
        val expectedZone = BaseCoachPlusControllerImpl.SEQUENCE[2]
        val plaqlessRawSensorState = mock<PlaqlessRawSensorState>()
        val rawData = mock<RawData>()
        whenever(plaqlessRawSensorState.convertToKmlRawData()).thenReturn(rawData)

        controller.onPlaqlessRawData(false, plaqlessRawSensorState)

        verify(appContext).addRawData(
            eq(rawData),
            eq(PauseStatus.InPause),
            eq(expectedZone)
        )
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onPlaqlessRawData set lastSupervisedResult16 when a result is available`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessRawSensorState = mock<PlaqlessRawSensorState>()
        val rawData = mock<RawData>()
        whenever(plaqlessRawSensorState.convertToKmlRawData()).thenReturn(rawData)
        val result = mock<SupervisedResult16>()
        whenever(appContext.addRawData(any(), any(), any())).then {
            controller.callback.onSupervisedResult(result)
        }
        controller.onPlaqlessRawData(false, plaqlessRawSensorState)

        assertEquals(result, controller.lastSupervisedResult16.get())
    }

    /*
      onRawData
     */
    @Test
    fun `onRawData do nothing if shouldContinueProcessingData false`() {
        controller.shouldContinueProcessingData.set(false)

        controller.onRawData(true, mock<RawData>())

        verify(appContext, never()).addRawData(any(), any(), any())
    }

    @Test
    fun `onRawData invokes appContext addRawData when shouldContinueProcessingData true with Running if play true`() {
        controller.shouldContinueProcessingData.set(true)
        val rawData = mock<RawData>()

        controller.onRawData(true, rawData)

        verify(appContext).addRawData(eq(rawData), eq(PauseStatus.Running), any())
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onRawData invokes appContext addRawData when shouldContinueProcessingData true with InPause if play false`() {
        controller.shouldContinueProcessingData.set(true)
        val rawData = mock<RawData>()

        controller.onRawData(false, rawData)

        verify(appContext).addRawData(eq(rawData), eq(PauseStatus.InPause), any())
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onRawData invokes appContext addRawData when shouldContinueProcessingData true with zone equals to SEQUENCE index`() {
        controller.shouldContinueProcessingData.set(true)
        controller.currentZoneIndex = 2
        val expectedZone = BaseCoachPlusControllerImpl.SEQUENCE[2]
        val rawData = mock<RawData>()

        controller.onRawData(false, rawData)

        verify(appContext).addRawData(
            eq(rawData),
            eq(PauseStatus.InPause),
            eq(expectedZone)
        )
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onRawData set lastSupervisedResult16 when a result is available`() {
        controller.shouldContinueProcessingData.set(true)
        val rawData = mock<RawData>()
        val result = mock<SupervisedResult16>()
        whenever(appContext.addRawData(any(), any(), any())).then {
            controller.callback.onSupervisedResult(result)
        }
        controller.onRawData(false, rawData)

        assertEquals(result, controller.lastSupervisedResult16.get())
    }

    /*
      onPlaqlessData
     */

    @Test
    fun `onPlaqlessData invokes appContext addPlaqlessData when shouldContinueProcessingData true with Running if play true and not out of mouth`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessSensorState = mock<PlaqlessSensorState>()
        val plaqlessData = mock<PlaqlessData>()
        whenever(plaqlessSensorState.convertToKmlPlaqlessData()).thenReturn(plaqlessData)
        whenever(plaqlessSensorState.plaqlessError).thenReturn(PlaqlessError.NONE)

        controller.onPlaqlessData(true, plaqlessSensorState)

        verify(appContext).addPlaqlessData(eq(plaqlessData), eq(PauseStatus.Running))
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onPlaqlessData invokes appContext addPlaqlessData when shouldContinueProcessingData true with InPause if play false`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessSensorState = mock<PlaqlessSensorState>()
        val plaqlessData = mock<PlaqlessData>()
        whenever(plaqlessSensorState.convertToKmlPlaqlessData()).thenReturn(plaqlessData)
        whenever(plaqlessSensorState.plaqlessError).thenReturn(PlaqlessError.OUT_OF_MOUTH)

        controller.onPlaqlessData(false, plaqlessSensorState)

        verify(appContext).addPlaqlessData(eq(plaqlessData), eq(PauseStatus.InPause))
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onPlaqlessData invokes appContext addPlaqlessData when shouldContinueProcessingData true with InPause if play true but out of mouth`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessSensorState = mock<PlaqlessSensorState>()
        val plaqlessData = mock<PlaqlessData>()
        whenever(plaqlessSensorState.convertToKmlPlaqlessData()).thenReturn(plaqlessData)
        whenever(plaqlessSensorState.plaqlessError).thenReturn(PlaqlessError.OUT_OF_MOUTH)

        controller.onPlaqlessData(true, plaqlessSensorState)

        verify(appContext).addPlaqlessData(eq(plaqlessData), eq(PauseStatus.InPause))
        assertNull(controller.lastSupervisedResult16.get())
    }

    @Test
    fun `onPlaqlessData set plaqlessError when shouldContinueProcessingData true`() {
        controller.shouldContinueProcessingData.set(true)
        val plaqlessSensorState = mock<PlaqlessSensorState>()
        val plaqlessData = mock<PlaqlessData>()
        val expectedPlaqlessError = PlaqlessError.WRONG_HANDLE
        whenever(plaqlessSensorState.convertToKmlPlaqlessData()).thenReturn(plaqlessData)
        whenever(plaqlessSensorState.plaqlessError).thenReturn(expectedPlaqlessError)
        controller.onPlaqlessData(true, plaqlessSensorState)

        assertEquals(expectedPlaqlessError, controller.plaqlessError.get())
    }

    @Test
    fun `onPlaqlessData does not set plaqlessError  when shouldContinueProcessingData false`() {
        controller.shouldContinueProcessingData.set(false)
        val plaqlessSensorState = mock<PlaqlessSensorState>()
        val plaqlessData = mock<PlaqlessData>()
        whenever(plaqlessSensorState.convertToKmlPlaqlessData()).thenReturn(plaqlessData)
        whenever(plaqlessSensorState.plaqlessError).thenReturn(PlaqlessError.OUT_OF_MOUTH)
        controller.onPlaqlessData(true, plaqlessSensorState)

        assertEquals(PlaqlessError.NONE, controller.plaqlessError.get())
    }

    /*
      createBrushingData
     */
    @Test
    fun `createBrushingData when isFullBrushingProcessingPossible false dont invokes checkupCalculator but set other fields`() {
        whenever(appContext.isFullBrushingProcessingPossible).thenReturn(false)
        val expectedGoalDuration = Duration.ofSeconds(101)

        doReturn(expectedGoalDuration).whenever(controller).getGoalBrushingDuration()

        val result = controller.createBrushingData()

        verify(checkupCalculator, never()).calculateCheckup(any<ProcessedBrushing>())

        assertEquals(GameApiConstants.GAME_COACH_PLUS, result.game)
        assertEquals(0L, result.duration)
        assertEquals(expectedGoalDuration.seconds.toInt(), result.goalDuration)
        assertNull(result.coverage)
        assertNull(result.getProcessedData())
    }

    @Test
    fun `createBrushingData when isFullBrushingProcessingPossible true invokes checkupCalculator`() {
        whenever(appContext.isFullBrushingProcessingPossible).thenReturn(true)
        val processedBrushing16 = mock<ProcessedBrushing16>()
        val processedBrushing = mock<ProcessedBrushing>()
        val checkupData = mock<CheckupData>()
        val expectedCoverage = 65
        val expectedJson = "{}"

        whenever(appContext.processFullBrushing()).thenReturn(processedBrushing16)
        doReturn(processedBrushing).whenever(controller)
            .convertProcessBrushing16ToProcessBrushing(processedBrushing16)
        whenever(checkupCalculator.calculateCheckup(processedBrushing)).thenReturn(checkupData)
        whenever(checkupData.surfacePercentage).thenReturn(expectedCoverage)
        whenever(processedBrushing.toJSON()).thenReturn(expectedJson)
        val result = controller.createBrushingData()

        verify(checkupCalculator).calculateCheckup(processedBrushing)

        assertEquals(expectedCoverage, result.coverage)
        assertEquals(expectedJson, result.getProcessedData())
    }

    /*
      getDurationPerZone
     */
    @Test
    fun `getDurationPerZone invokes getAdjustedDuration with currentZone`() {
        val expectedDuration = 65L
        whenever(zoneDurationAdjuster.getAdjustedDuration(controller.getCurrentZone())).thenReturn(
            expectedDuration
        )
        assertEquals(expectedDuration, controller.getDurationPerZone())
    }

    /*
      onReset
     */
    @Test
    fun `onReset invoke start on appContext`() {
        controller.reset()
        verify(appContext, times(2)).start(controller.callback)
    }

    /*
      hasCriticalPlaqlessError
     */
    @Test
    fun `hasCriticalPlaqlessError returns true when plaqlessError is OUT_OF_MOUTH`() {
        controller.plaqlessError.set(PlaqlessError.OUT_OF_MOUTH)
        TestCase.assertTrue(controller.hasCriticalPlaqlessError)
    }

    @Test
    fun `hasCriticalPlaqlessError returns false when plaqlessError is not OUT_OF_MOUTH`() {
        PlaqlessError.values().filterNot { it == PlaqlessError.OUT_OF_MOUTH }.forEach { value ->
            controller.plaqlessError.set(value)
            TestCase.assertFalse(controller.hasCriticalPlaqlessError)
        }
    }

    /*
    plaqlessError
     */
    @Test
    fun `plaqlessError default value is NONE`() {
        assertEquals(PlaqlessError.NONE, controller.plaqlessError.get())
    }

    @Test
    fun `zonePasses size equals To SEQUENCE size`() {
        assertEquals(BaseCoachPlusControllerImpl.SEQUENCE.size, controller.zonePasses.size)
    }

    @Test
    fun `failTimes size equals to SEQUENCE size`() {
        assertEquals(BaseCoachPlusControllerImpl.SEQUENCE.size, controller.failTimes.size)
    }

    /*
    onTick
     */
    @Test
    fun `onTick invokes onWrongZoneBrushed when not good zones brushed with mapper onWrongZoneBrushing when shouldPreventFrustration false`() {
        whenever(
            feedbackMapper.onWrongZoneBrushing(
                anyOrNull(),
                any(),
                any()
            )
        ).thenReturn(FeedBackMessage.EmptyFeedback)
        doReturn(false).whenever(controller).shouldPreventFrustration(any())
        doReturn(mock<CoachPlusControllerResult>()).whenever(controller).onWrongZoneBrushed(any())
        controller.onTick()
        verify(controller).onWrongZoneBrushed(any())
        verify(feedbackMapper).onWrongZoneBrushing(anyOrNull(), any(), any())
        verify(feedbackMapper, never()).onGoodZoneBrushing(anyOrNull(), any(), any())
    }

    @Test
    fun `onTick invokes onWrongZoneBrushed when not good zones brushed with mapper onWrongZoneBrushing when shouldPreventFrustration true`() {
        doReturn(true).whenever(controller).shouldPreventFrustration(any())
        whenever(feedbackMapper.onGoodZoneBrushing(anyOrNull(), any(), any())).thenReturn(
            FeedBackMessage.EmptyFeedback
        )
        doReturn(mock<CoachPlusControllerResult>()).whenever(controller).onWrongZoneBrushed(any())
        controller.onTick()
        verify(controller).onWrongZoneBrushed(any())
        verify(feedbackMapper, never()).onWrongZoneBrushing(anyOrNull(), any(), any())
        verify(feedbackMapper).onGoodZoneBrushing(anyOrNull(), any(), any())
    }

    @Test
    fun `onTick invokes onGoodZoneBrushed when good zones brushed`() {
        whenever(feedbackMapper.onGoodZoneBrushing(anyOrNull(), any(), any())).thenReturn(
            FeedBackMessage.EmptyFeedback
        )
        doReturn(true).whenever(controller).isBrushingGoodZone(any())
        doReturn(mock<CoachPlusControllerResult>()).whenever(controller).onGoodZoneBrushed(any())
        controller.onTick()
        verify(controller).onGoodZoneBrushed(any())
    }

    /*
     COMPUTE BRUHSING DURATION
   */
    @Test
    fun `computeBrushingDuration works as expected`() {
        controller.zonePasses[3] = 4500L
        controller.zonePasses[5] = 4000L
        controller.zonePasses[9] = 4000L
        controller.failTimes[5] = 5000L
        controller.failTimes[10] = 5500L
        controller.failTimes[13] = 10000L
        assertEquals(33, controller.computeBrushingDuration())
    }

    /*
    onGoodZoneBrushed
     */

    @Test
    fun `onGoodZoneBrushed increases zonePass by tick period`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(120),
            tickPeriod = 50L
        )
        controller.onGoodZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertEquals(50L, controller.zonePasses[0])
    }

    @Test
    fun `onGoodZoneBrushed does not increases zoneFail time`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(120),
            tickPeriod = 50L
        )
        controller.onGoodZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertEquals(0L, controller.failTimes[0])
    }

    @Test
    fun `onGoodZoneBrushed currentZone not finished returns good result`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(16),
            tickPeriod = 500L,
            zoneDurationAdjuster = null
        )
        val (zoneToBrush, completionPercent, brushingGoodZone, sequenceFinished) = controller.onGoodZoneBrushed(
            FeedBackMessage.EmptyFeedback
        )
        assertTrue(brushingGoodZone)
        assertFalse(sequenceFinished)
        assertEquals(50, completionPercent)
        assertEquals(BaseCoachPlusControllerImpl.SEQUENCE[0], zoneToBrush)
    }

    @Test
    fun `onGoodZoneBrushed current zone finished sequence finished returns good result`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(16),
            tickPeriod = 1000L,
            zoneDurationAdjuster = null
        )
        controller.currentZoneIndex = 15

        val result = controller.onGoodZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertTrue(result.brushingGoodZone)
        assertTrue(result.sequenceFinished)
        assertEquals(100, result.completionPercent)
        assertEquals(BaseCoachPlusControllerImpl.SEQUENCE[15], result.zoneToBrush)
    }

    @Test
    fun `onGoodZoneBrushed current zone finished sequence not finished returns good result`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(16),
            tickPeriod = 1000L,
            zoneDurationAdjuster = null
        )
        controller.currentZoneIndex = 0

        val result = controller.onGoodZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertTrue(result.brushingGoodZone)
        assertFalse(result.sequenceFinished)
        assertEquals(100, result.completionPercent)
        assertEquals(BaseCoachPlusControllerImpl.SEQUENCE[0], result.zoneToBrush)
        assertEquals(1, controller.currentZoneIndex)
    }

    /*
    onWrongZoneBrushed
     */
    @Test
    fun `onWrongZoneBrushed increases zoneFail Time`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(120),
            tickPeriod = 50L,
            zoneDurationAdjuster = null

        )
        controller.onWrongZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertEquals(50L, controller.failTimes[0])
    }

    @Test
    fun `onWrongZoneBrushed gumNotDamaged does not skip current zone`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(120),
            tickPeriod = 50L,
            zoneDurationAdjuster = null

        )
        controller.failTimes[9] = BaseCoachPlusControllerImpl.TOLERANCE_TIME_MS + 50L
        controller.currentZoneIndex = 9
        val result = controller.onWrongZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertEquals(9, controller.currentZoneIndex)
        assertFalse(result.brushingGoodZone)
        assertFalse(result.sequenceFinished)
    }

    @Test
    fun `onWrongZoneBrushed gumDamaged skips current zone`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(120),
            tickPeriod = 50L,
            zoneDurationAdjuster = null
        )
        controller.currentZoneIndex = 9
        controller.failTimes[9] = BaseCoachPlusControllerImpl.DEFAULT_MAX_FAIL_TIME_MS + 7500L + 1L
        val result = controller.onWrongZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertEquals(10, controller.currentZoneIndex)
        assertFalse(result.sequenceFinished)
        assertFalse(result.brushingGoodZone)
    }

    @Test
    fun `onWrongZoneBrushed gumDamaged last zone finishes`() {
        setupMocks(
            goalDuration = Duration.ofSeconds(120),
            tickPeriod = 50L,
            zoneDurationAdjuster = null
        )
        controller.currentZoneIndex = 15
        controller.failTimes[15] = BaseCoachPlusControllerImpl.DEFAULT_MAX_FAIL_TIME_MS + 7500L + 1L
        val result = controller.onWrongZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertEquals(15, controller.currentZoneIndex)
        assertTrue(result.sequenceFinished)
        assertFalse(result.brushingGoodZone)
    }

    @Test
    fun `onWrongZoneBrushed less than 1000ms failing prevents frustration`() {
        setupMocks(zoneDurationAdjuster = null, goalDuration = Duration.ofSeconds(120))
        val (_, _, brushingGoodZone) = controller.onWrongZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertTrue(brushingGoodZone)
    }

    @Test
    fun `onWrongZoneBrushed more than 1000ms failing does not prevent frustration`() {
        setupMocks(zoneDurationAdjuster = null, goalDuration = Duration.ofSeconds(120))
        controller.failTimes[0] = 1010L
        val (_, _, brushingGoodZone) = controller.onWrongZoneBrushed(FeedBackMessage.EmptyFeedback)
        assertFalse(brushingGoodZone)
    }

    /*
   increaseTimes
    */
    @Test
    fun `increaseTimes only increase time when plaqlessError is not OUT_OF_MOUTH`() {
        setupMocks(tickPeriod = 1L)
        controller.plaqlessError.set(PlaqlessError.OUT_OF_MOUTH)
        controller.increaseTimes(controller.failTimes)
        assertEquals(0, controller.failTimes[controller.currentZoneIndex])
        controller.plaqlessError.set(PlaqlessError.NONE)
        controller.increaseTimes(controller.failTimes)
        assertEquals(1L, controller.failTimes[controller.currentZoneIndex])
    }

    /*
    shouldPreventFrustration
     */
    @Test
    fun `shouldPreventFrustration true`() {
        controller.failTimes[0] = 100L
        assertTrue(controller.shouldPreventFrustration(0))
    }

    @Test
    fun `shouldPreventFrustration false because failing too long`() {
        controller.failTimes[0] = BaseCoachPlusControllerImpl.DEFAULT_MAX_FAIL_TIME_MS + 100L
        assertFalse(controller.shouldPreventFrustration(0))
    }

    @Test
    fun `shouldPreventFrustration false because has been brushing well before`() {
        controller.failTimes[0] = 100L
        controller.zonePasses[0] = 1000L
        assertFalse(controller.shouldPreventFrustration(0))
    }

    /*
    shouldPreventGumDamage
     */
    @Test
    fun `shouldPreventGumDamage false`() {
        assertFalse(controller.shouldPreventGumDamage(0))
    }

    @Test
    fun `shouldPreventGumDamage true`() {
        controller.failTimes[0] = BaseCoachPlusControllerImpl.DEFAULT_MAX_FAIL_TIME_MS + 2000L
        assertTrue(controller.shouldPreventGumDamage(0))
    }

    /*
   isBrushingGoodZone(MouthZone16)
   */
    @Test
    fun `when lastSupervisedResult16 empty isBrushingGoodZone returns false`() {
        assertFalse(controller.isBrushingGoodZone(MouthZone16.LoIncExt))
    }

    @Test
    fun `when lastSupervisedResult16 wrong zone isBrushingGoodZone returns false`() {
        val lastResult = mock<SupervisedResult16>()
        whenever(lastResult.isZoneCorrect).thenReturn(false)
        controller.lastSupervisedResult16.set(lastResult)
        assertFalse(controller.isBrushingGoodZone(MouthZone16.LoIncExt))
    }

    @Test
    fun `when the expected zone differ from the one from sequence isBrushingGoodZone returns false`() {
        val lastResult = mock<SupervisedResult16>()
        whenever(lastResult.isZoneCorrect).thenReturn(true)
        whenever(lastResult.zone).thenReturn(MouthZone16.LoMolLeInt)
        controller.lastSupervisedResult16.set(lastResult)
        assertFalse(controller.isBrushingGoodZone(MouthZone16.LoIncExt))
    }

    @Test
    fun `when the expected zone is the same as the one from sequence isBrushingGoodZone returns true`() {
        val lastResult = mock<SupervisedResult16>()
        whenever(lastResult.isZoneCorrect).thenReturn(true)
        whenever(lastResult.zone).thenReturn(MouthZone16.LoIncExt)

        controller.lastSupervisedResult16.set(lastResult)
        assertTrue(controller.isBrushingGoodZone(MouthZone16.LoIncExt))
    }

    /*
    getAvroTransitionsTable()
    */
    @Test
    fun `getAvroTransitionsTable correctly maps times in ms`() {
        val failTimeZone1 = 1300L
        val goodTimeZone1 = 4900L
        val failTimeZone9 = 5000L
        val goodTimeZone9 = 0L
        val failTimeZone14 = 2019L
        val goodTimeZone14 = 2018L

        controller.failTimes[1] = failTimeZone1
        controller.zonePasses[1] = goodTimeZone1
        controller.failTimes[9] = failTimeZone9
        controller.zonePasses[9] = goodTimeZone9
        controller.failTimes[14] = failTimeZone14
        controller.zonePasses[14] = goodTimeZone14

        val table = controller.getAvroTransitionsTable()
            .map { it.toLong() } // Converted to long for comparing with long controller's times
        assertEquals(0L, table[0])
        assertEquals(failTimeZone1 + goodTimeZone1, table[1])
        assertEquals(0L, table[2])
        assertEquals(0L, table[3])
        assertEquals(0L, table[4])
        assertEquals(0L, table[5])
        assertEquals(0L, table[6])
        assertEquals(0L, table[7])
        assertEquals(0L, table[8])
        assertEquals(failTimeZone9 + goodTimeZone9, table[9])
        assertEquals(0L, table[10])
        assertEquals(0L, table[11])
        assertEquals(0L, table[12])
        assertEquals(0L, table[13])
        assertEquals(failTimeZone14 + goodTimeZone14, table[14])
        assertEquals(0L, table[15])
    }

    /*
    onPause
    */
    @Test
    fun `onPause reset feedback mapper`() {
        controller.onPause()

        verify(feedbackMapper).reset()
    }

    /*
    CLEANNESS_FULL
     */
    @Test
    fun `is const CLEANNESS_FULL equals 100`() {
        assertEquals(100, CLEANNESS_FULL)
    }

    /*
    isDurationPerZoneExceeded
    */
    @Test
    fun `isDurationPerZoneExceeded returns true if zone time gt or equals brushingGoal`() {
        val brushingGoalSeconds = 160L
        setupMocks(goalDuration = Duration.ofSeconds(brushingGoalSeconds))

        val zoneTimeMillis =
            (brushingGoalSeconds * 1000) / BaseCoachPlusControllerImpl.SEQUENCE.size

        controller.currentZoneIndex = 0
        controller.zonePasses[0] = zoneTimeMillis.toLong()
        assertTrue(controller.isDurationPerZoneExceeded())

        controller.currentZoneIndex = 0
        controller.zonePasses[0] = zoneTimeMillis.toLong() + 1L
        assertTrue(controller.isDurationPerZoneExceeded())
    }

    @Test
    fun `isDurationPerZoneExceeded returns false if zone time lt brushingGoal`() {
        val brushingGoalSeconds = 160L
        setupMocks(
            goalDuration = Duration.ofSeconds(brushingGoalSeconds),
            zoneDurationAdjuster = null
        )
        val zoneTimeMillis =
            (brushingGoalSeconds * 1000) / BaseCoachPlusControllerImpl.SEQUENCE.size

        controller.currentZoneIndex = 0
        controller.zonePasses[0] = 0
        assertFalse(controller.isDurationPerZoneExceeded())

        controller.currentZoneIndex = 0
        controller.zonePasses[0] = zoneTimeMillis.toLong() - 1L
        assertFalse(controller.isDurationPerZoneExceeded())
    }

    /*
    kmlAvroData
     */

    @Test
    fun `kmlAvroData returns supervised context's result`() {
        val expectedResult = mock<CharVector>()
        whenever(appContext.getAvro(any())).thenReturn(expectedResult)

        val result = controller.kmlAvroData(mock())

        assertEquals(expectedResult, result)
        verify(appContext).getAvro(any())
    }

    /*
    notifyReconnection
     */

    @Test
    fun `notifyReconnection invokes appContext notifyReconnection`() {
        controller.notifyReconnection()

        verify(appContext).notifyReconnection()
    }
}
