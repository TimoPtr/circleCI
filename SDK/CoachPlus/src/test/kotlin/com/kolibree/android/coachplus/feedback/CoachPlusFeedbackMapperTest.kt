/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.kml.KPIAggregate
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.OptionalKPIAggregate
import com.kolibree.kml.SpeedKPI
import com.kolibree.kml.SupervisedResult16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration

internal class CoachPlusFeedbackMapperTest : BaseUnitTest() {

    lateinit var tick: Duration
    lateinit var mapper: CoachPlusFeedbackMapper

    @Before
    fun setUp() {
        tick = mock()
        mapper = spy(CoachPlusFeedbackMapper(tick))
    }

    @Test
    fun `minDurationBeforeDisplay is 1,5 second`() {
        assertEquals(Duration.ofSeconds(1, 500000000), CoachPlusFeedbackMapper.minDurationBeforeDisplay)
    }

    @Test
    fun `maxDurationDisplayed is 2 seconds`() {
        assertEquals(Duration.ofSeconds(2), CoachPlusFeedbackMapper.maxDurationDisplayed)
    }

    @Test
    fun `extendDurationDisplay is 1 seconds`() {
        assertEquals(Duration.ofSeconds(1), CoachPlusFeedbackMapper.extendDurationDisplay)
    }

    @Test
    fun `onGoodZoneBrushing invokes onTick getFeedback and ToDisplay`() {
        whenever(tick.nano).thenReturn(0)
        doReturn(listOf(FeedBackMessage.EmptyFeedback)).whenever(mapper).getFeedbackMessages(
            any(),
            eq(MouthZone16.LoIncExt),
            any()
        )
        doReturn(FeedBackMessage.EmptyFeedback).whenever(mapper)
            .getFeedbackToDisplay(eq(listOf(FeedBackMessage.EmptyFeedback)))
        mapper.onGoodZoneBrushing(mock(), MouthZone16.LoIncExt, PlaqlessError.WRONG_HANDLE)

        verify(mapper).onTick()
        verify(mapper).getFeedbackMessages(any(), any(), any())
        verify(mapper).getFeedbackToDisplay(any())
    }

    @Test
    fun `onGoodZoneBrushing returns OutOfMouthFeedback when plaqlessError is OUT_OF_MOUTH and never invokes getFeedbackMessages`() {
        whenever(tick.nano).thenReturn(0)

        assertEquals(
            FeedBackMessage.OutOfMouthFeedback,
            mapper.onGoodZoneBrushing(null, MouthZone16.LoIncExt, PlaqlessError.OUT_OF_MOUTH)
        )
        verify(mapper, never()).getFeedbackMessages(any(), any(), any())
    }

    @Test
    fun `onWrongZoneBrushing invokes onTick getFeedbackMessages and getFeedbackToDisplay with WrongZoneFeedback`() {
        whenever(tick.nano).thenReturn(0)
        doReturn(listOf(FeedBackMessage.EmptyFeedback)).whenever(mapper).getFeedbackMessages(
            any(),
            eq(MouthZone16.LoIncExt),
            any()
        )
        doReturn(FeedBackMessage.EmptyFeedback).whenever(mapper).getFeedbackToDisplay(
            eq(
                listOf(
                    FeedBackMessage.EmptyFeedback,
                    FeedBackMessage.WrongZoneFeedback
                )
            )
        )
        mapper.onWrongZoneBrushing(mock(), MouthZone16.LoIncExt, PlaqlessError.NONE)

        verify(mapper).onTick()
        verify(mapper).getFeedbackMessages(any(), any(), any())
        verify(mapper).getFeedbackToDisplay(
            eq(
                listOf(
                    FeedBackMessage.EmptyFeedback,
                    FeedBackMessage.WrongZoneFeedback
                )
            )
        )
    }

    @Test
    fun `onWrongZoneBrushing returns OutOfMouthFeedback when plaqlessError is OUT_OF_MOUTH and never invokes getFeedbackMessages`() {
        whenever(tick.nano).thenReturn(0)

        assertEquals(
            FeedBackMessage.OutOfMouthFeedback,
            mapper.onWrongZoneBrushing(null, MouthZone16.LoIncExt, PlaqlessError.OUT_OF_MOUTH)
        )
        verify(mapper, never()).getFeedbackMessages(any(), any(), any())
    }

    @Test
    fun `onTick increase currentTime of tickPeriod`() {
        val tickPeriodNano = 100
        whenever(tick.nano).thenReturn(tickPeriodNano)

        assertEquals(Duration.ZERO, mapper.currentTime)

        (1 until 10).forEach {
            mapper.onTick()
            assertEquals(Duration.ofNanos((it * tickPeriodNano).toLong()), mapper.currentTime)
        }
    }

    @Test
    fun `getFeedbackMessages returns empty list if no kpi and plaqlessError is NONE`() {
        val result = mapper.getFeedbackMessages(null, MouthZone16.LoIncExt, PlaqlessError.NONE)
        assertEquals(emptyList<FeedBackMessage>(), result)
    }

    @Test
    fun `getFeedbackMessages returns only WrongHandleFeedback if no kpi and plaqlessError is WRONG_HANDLE`() {
        val result = mapper.getFeedbackMessages(null, MouthZone16.LoIncExt, PlaqlessError.WRONG_HANDLE)
        assertEquals(listOf(FeedBackMessage.WrongHandleFeedback), result)
    }

    @Test
    fun `getFeedbackMessages returns only OutOfMouthFeedback if plaqlessError is OUT_OF_MOUTH`() {
        val result = mapper.getFeedbackMessages(null, MouthZone16.LoIncExt, PlaqlessError.OUT_OF_MOUTH)
        assertEquals(listOf(FeedBackMessage.OutOfMouthFeedback), result)
    }

    @Test
    fun `getFeedbackMessages returns only if isPressureCorrect is false`() {
        val supervisedResult16 = mock<SupervisedResult16>()
        val optionalKPIAggregate = mock<OptionalKPIAggregate>()
        val kpiAggregate = mock<KPIAggregate>()
        whenever(supervisedResult16.optionalKpi).thenReturn(optionalKPIAggregate)
        whenever(optionalKPIAggregate.first).thenReturn(true)
        whenever(optionalKPIAggregate.second).thenReturn(kpiAggregate)
        whenever(kpiAggregate.isOrientationCorrect).thenReturn(true)
        whenever(kpiAggregate.speedCorrectness).thenReturn(SpeedKPI.Correct)
        whenever(kpiAggregate.isPressureCorrect).thenReturn(false)
        val result = mapper.getFeedbackMessages(supervisedResult16, MouthZone16.LoIncExt, PlaqlessError.NONE)
        assertEquals(listOf(FeedBackMessage.OverpressureFeedback), result)
    }

    @Test
    fun `getFeedbackMessages returns WrongHandleFeedback when kpi is fine and plaqlessError is WRONG_HANDLE`() {
        val supervisedResult16 = mock<SupervisedResult16>()
        val optionalKPIAggregate = mock<OptionalKPIAggregate>()
        val kpiAggregate = mock<KPIAggregate>()
        whenever(supervisedResult16.optionalKpi).thenReturn(optionalKPIAggregate)
        whenever(optionalKPIAggregate.first).thenReturn(true)
        whenever(optionalKPIAggregate.second).thenReturn(kpiAggregate)
        whenever(kpiAggregate.isOrientationCorrect).thenReturn(true)
        whenever(kpiAggregate.speedCorrectness).thenReturn(SpeedKPI.Correct)
        whenever(kpiAggregate.isPressureCorrect).thenReturn(true)
        val result = mapper.getFeedbackMessages(supervisedResult16, MouthZone16.LoIncExt, PlaqlessError.WRONG_HANDLE)
        assertEquals(listOf(FeedBackMessage.WrongHandleFeedback), result)
    }

    @Test
    fun `getFeedbackMessage returns WrongAngle and WrongSpeed and WrongHandle`() {
        val supervisedResult16 = mock<SupervisedResult16>()
        val optionalKPIAggregate = mock<OptionalKPIAggregate>()
        val kpiAggregate = mock<KPIAggregate>()
        whenever(supervisedResult16.optionalKpi).thenReturn(optionalKPIAggregate)
        whenever(optionalKPIAggregate.first).thenReturn(true)
        whenever(optionalKPIAggregate.second).thenReturn(kpiAggregate)
        whenever(kpiAggregate.isOrientationCorrect).thenReturn(false)
        whenever(kpiAggregate.speedCorrectness).thenReturn(SpeedKPI.Underspeed)
        whenever(kpiAggregate.isPressureCorrect).thenReturn(true)
        val result = mapper.getFeedbackMessages(supervisedResult16, MouthZone16.LoIncExt, PlaqlessError.WRONG_HANDLE)
        assertEquals(
            listOf(
                FeedBackMessage.Wrong45AngleFeedback,
                FeedBackMessage.UnderSpeedFeedback,
                FeedBackMessage.WrongHandleFeedback
            ), result
        )
    }

    @Test
    fun `getFeedbackToDisplay returns empty when new feedback comes`() {
        assertEquals(
            FeedBackMessage.EmptyFeedback,
            mapper.getFeedbackToDisplay(listOf(FeedBackMessage.Wrong45AngleFeedback))
        )
    }

    @Test
    fun `getFeedbackToDisplay returns most important feedback after minDurationBeforeDisplay`() {
        whenever(tick.nano).thenReturn(CoachPlusFeedbackMapper.minDurationBeforeDisplay.dividedBy(2).nano)

        assertEquals(
            FeedBackMessage.EmptyFeedback,
            mapper.getFeedbackToDisplay(
                listOf(
                    FeedBackMessage.OverSpeedFeedback,
                    FeedBackMessage.WrongZoneFeedback,
                    FeedBackMessage.WrongHandleFeedback
                )
            )
        )
        val receivedAt = mapper.currentTime
        assertEquals(FeedBackMessage.WrongZoneFeedback, mapper.cachedFeedbackMessage)
        assertEquals(receivedAt, mapper.receivedAt)
        assertNull(mapper.startDisplayAt)
        mapper.onTick()
        mapper.onTick()
        mapper.onTick()

        assertEquals(
            FeedBackMessage.WrongZoneFeedback,
            mapper.getFeedbackToDisplay(
                listOf(
                    FeedBackMessage.WrongZoneFeedback,
                    FeedBackMessage.UnderSpeedFeedback,
                    FeedBackMessage.WrongHandleFeedback
                )
            )
        )

        assertEquals(FeedBackMessage.WrongZoneFeedback, mapper.cachedFeedbackMessage)
        assertEquals(receivedAt, mapper.receivedAt)
        assertEquals(mapper.currentTime, mapper.startDisplayAt)
    }

    @Test
    fun `getFeedbackToDisplay returns most important feedback + extend to extendDurationDisplay`() {
        whenever(tick.nano).thenReturn(CoachPlusFeedbackMapper.minDurationBeforeDisplay.dividedBy(2).nano)

        assertEquals(
            FeedBackMessage.EmptyFeedback,
            mapper.getFeedbackToDisplay(
                listOf(
                    FeedBackMessage.OverSpeedFeedback,
                    FeedBackMessage.WrongZoneFeedback,
                    FeedBackMessage.WrongHandleFeedback
                )
            )
        )
        val receivedAt = mapper.currentTime
        assertEquals(FeedBackMessage.WrongZoneFeedback, mapper.cachedFeedbackMessage)
        assertEquals(receivedAt, mapper.receivedAt)
        assertNull(mapper.startDisplayAt)
        mapper.onTick()
        mapper.onTick()
        mapper.onTick()

        assertEquals(
            FeedBackMessage.WrongZoneFeedback,
            mapper.getFeedbackToDisplay(
                listOf(
                    FeedBackMessage.WrongZoneFeedback,
                    FeedBackMessage.UnderSpeedFeedback,
                    FeedBackMessage.WrongHandleFeedback
                )
            )
        )

        assertEquals(FeedBackMessage.WrongZoneFeedback, mapper.cachedFeedbackMessage)
        assertEquals(receivedAt, mapper.receivedAt)
        assertEquals(mapper.currentTime, mapper.startDisplayAt)

        mapper.onTick()
        mapper.onTick()

        assertEquals(
            FeedBackMessage.WrongZoneFeedback,
            mapper.getFeedbackToDisplay(
                listOf(
                    FeedBackMessage.WrongZoneFeedback,
                    FeedBackMessage.UnderSpeedFeedback,
                    FeedBackMessage.WrongHandleFeedback
                )
            )
        )

        assertEquals(FeedBackMessage.WrongZoneFeedback, mapper.cachedFeedbackMessage)
        assertEquals(receivedAt, mapper.receivedAt)
        assertEquals(mapper.currentTime.minus(CoachPlusFeedbackMapper.extendDurationDisplay), mapper.startDisplayAt)
    }

    @Test
    fun `reset all the timer`() {
        mapper.currentTime = Duration.ofSeconds(1)
        mapper.cachedFeedbackMessage = FeedBackMessage.UnderSpeedFeedback
        mapper.receivedAt = Duration.ofSeconds(2)
        mapper.startDisplayAt = Duration.ofSeconds(3)

        mapper.reset()

        assertEquals(Duration.ZERO, mapper.currentTime)
        assertEquals(FeedBackMessage.EmptyFeedback, mapper.cachedFeedbackMessage)
        assertNull(mapper.receivedAt)
        assertNull(mapper.startDisplayAt)
    }

    @Test
    fun `maybeUpdateStartDisplayAt does nothing when startDisplay is null`() {
        mapper.currentTime = Duration.ofSeconds(100)
        mapper.maybeUpdateStartDisplayAt()
        assertNull(mapper.startDisplayAt)
    }

    @Test
    fun `maybeUpdateStartDisplayAt does not update startDisplay when normalEnd is after the potential extendEnd`() {
        mapper.currentTime = Duration.ZERO
        mapper.startDisplayAt = Duration.ofSeconds(CoachPlusFeedbackMapper.extendDurationDisplay.seconds + 1)
        mapper.maybeUpdateStartDisplayAt()
        assertEquals(
            Duration.ofSeconds(CoachPlusFeedbackMapper.extendDurationDisplay.seconds + 1),
            mapper.startDisplayAt
        )
    }

    @Test
    fun `maybeUpdateStartDisplayAt update startDisplay when normalEnd is before the potential extendEnd`() {
        mapper.currentTime = CoachPlusFeedbackMapper.maxDurationDisplayed
        mapper.startDisplayAt = Duration.ZERO
        mapper.maybeUpdateStartDisplayAt()
        assertEquals(mapper.currentTime.minus(CoachPlusFeedbackMapper.extendDurationDisplay), mapper.startDisplayAt)
    }

    @Test
    fun `shouldContinueDisplayFeedback returns false when durationSinceDisplay equals 0 or greater than maxDurationDisplayed`() {
        assertFalse(mapper.shouldContinueDisplayFeedback(Duration.ZERO))
        assertFalse(
            mapper.shouldContinueDisplayFeedback(
                Duration.ofSeconds(1)
                    .plus(CoachPlusFeedbackMapper.maxDurationDisplayed)
            )
        )
    }

    @Test
    fun `shouldContinueDisplayFeedback returns true when durationSinceDisplay greater than 0 and smaller than maxDurationDisplayed or equals`() {
        assertTrue(mapper.shouldContinueDisplayFeedback(CoachPlusFeedbackMapper.maxDurationDisplayed))
        assertTrue(mapper.shouldContinueDisplayFeedback(CoachPlusFeedbackMapper.maxDurationDisplayed.minusSeconds(1)))
    }

    @Test
    fun `shouldCacheFeedback returns false when same as previous`() {
        assertFalse(mapper.shouldCacheFeedback(true, null))
    }

    @Test
    fun `shouldCacheFeedback returns false when not same as previous and most important is null`() {
        assertFalse(mapper.shouldCacheFeedback(false, null))
    }

    @Test
    fun `shouldCacheFeedback returns false when not same as previous and most important is emptyFeedback`() {
        assertFalse(mapper.shouldCacheFeedback(false, FeedBackMessage.EmptyFeedback))
    }

    @Test
    fun `shouldCacheFeedback returns true when not the same as previous and mostImportantFeedback is not null and not empty`() {
        assertTrue(mapper.shouldCacheFeedback(false, FeedBackMessage.WrongZoneFeedback))
    }

    @Test
    fun `shouldStartDisplay returns false when not the same as previous and durationSinceReceived is greater than minDurationBeforeDisplay`() {
        assertFalse(mapper.shouldStartDisplay(false, CoachPlusFeedbackMapper.minDurationBeforeDisplay.plusSeconds(1)))
    }

    @Test
    fun `shouldStartDisplay returns false when not the same as previous and durationSinceReceived is smaller than minDurationBeforeDisplay`() {
        assertFalse(mapper.shouldStartDisplay(false, CoachPlusFeedbackMapper.minDurationBeforeDisplay.minusSeconds(1)))
    }

    @Test
    fun `shouldStartDisplay returns false when same as previous and durationSinceReceived is greater than minDurationBeforeDisplay`() {
        assertFalse(mapper.shouldStartDisplay(false, CoachPlusFeedbackMapper.minDurationBeforeDisplay.plusSeconds(1)))
    }

    @Test
    fun `shouldStartDisplay returns true when same as previous and durationSinceReceived is greater than minDurationBeforeDisplay`() {
        assertTrue(mapper.shouldStartDisplay(true, CoachPlusFeedbackMapper.minDurationBeforeDisplay.plusSeconds(1)))
    }
}
