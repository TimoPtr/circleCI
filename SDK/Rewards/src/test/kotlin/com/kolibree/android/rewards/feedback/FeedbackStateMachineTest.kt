/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.models.BrushingSessionHistoryEvent
import com.kolibree.android.rewards.test.DEFAULT_CHALLENGE_ID
import com.kolibree.android.rewards.test.createBrushingSessionHistoryEvent
import com.kolibree.android.rewards.test.createChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.test.createStreakCompletedHistoryEvent
import com.kolibree.android.rewards.test.createTierReachedHistoryEvent
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class FeedbackStateMachineTest : BaseUnitTest() {
    /*
    Tier Reached State

    TierReachedState + BrushingInput = TierReached + BrushingState
    TierReachedState + ChallengeCompletedInput = TierReached + ChallengeCompletedState
    TierReachedState + TierReachedInput = TierReached + NoState
    TierReachedState + OtherInput = TierReached + NoState
    TierReachedState + TerminusInput = TierReached
    TierReachedState + StreakCompletedInput = TierReached + StreakCompletedState
     */
    @Test
    fun `TierReachedState + BrushingInput = TierReached entity`() {
        val tierReachedState = createTierReachedState()

        assertTrue(tierReachedState.feedbackEntityFromInput(createBrushingInput())!!.isTierReached())
    }

    @Test
    fun `TierReachedState + BrushingInput = BrushingState`() {
        val tierReachedState = createTierReachedState()

        val input = createBrushingInput()
        val expectedState = BrushingState(input)

        assertEquals(expectedState, tierReachedState.nextState(input))
    }

    @Test
    fun `TierReachedState + ChallengeCompletedInput = TierReached entity`() {
        val tierReachedState = createTierReachedState()

        assertTrue(tierReachedState.feedbackEntityFromInput(createChallengeCompletedInput())!!.isTierReached())
    }

    @Test
    fun `TierReachedState + ChallengeCompletedInput = ChallengeCompletedState`() {
        val tierReachedState = createTierReachedState()

        val input = createChallengeCompletedInput()
        val expectedState = ChallengeCompletedState(input)

        assertEquals(expectedState, tierReachedState.nextState(input))
    }

    @Test
    fun `TierReachedState + TierReachedInput = TierReached entity`() {
        val tierReachedState = createTierReachedState()

        assertTrue(tierReachedState.feedbackEntityFromInput(createTierReachedInput())!!.isTierReached())
    }

    @Test
    fun `TierReachedState + TierReachedInput = NoState`() {
        val tierReachedState = createTierReachedState()

        val input = createTierReachedInput()

        assertEquals(NoState, tierReachedState.nextState(input))
    }

    @Test
    fun `TierReachedState + OtherInput = TierReached entity`() {
        val tierReachedState = createTierReachedState()

        assertTrue(tierReachedState.feedbackEntityFromInput(createOtherInput())!!.isTierReached())
    }

    @Test
    fun `TierReachedState + OtherInput = NoState`() {
        val tierReachedState = createTierReachedState()

        val input = createOtherInput()

        assertEquals(NoState, tierReachedState.nextState(input))
    }

    @Test
    fun `TierReachedState + TerminusInput = TierReached entity`() {
        val tierReachedState = createTierReachedState()

        assertTrue(tierReachedState.feedbackEntityFromInput(TerminusInput)!!.isTierReached())
    }

    @Test
    fun `TierReachedState + TerminusInput = NoState`() {
        val tierReachedState = createTierReachedState()

        assertEquals(NoState, tierReachedState.nextState(TerminusInput))
    }

    @Test
    fun `TierReachedState + StreakCompletedInput = TierReached entity`() {
        val tierReachedState = createTierReachedState()

        val input = createStreakCompletedInput()

        assertTrue(tierReachedState.feedbackEntityFromInput(input)!!.isTierReached())
    }

    @Test
    fun `TierReachedState + StreakCompletedInput = StreakCompletedState`() {
        val tierReachedState = createTierReachedState()

        val input = createStreakCompletedInput()

        val expectedState = StreakCompletedState(input)

        assertEquals(expectedState, tierReachedState.nextState(input))
    }

    private fun createTierReachedState(challengesCompleted: Set<Long> = setOf(DEFAULT_CHALLENGE_ID)) =
        TierReachedState(createTierReachedInput(), challengesCompleted)

    /*

    ChallengeCompletedState

    ChallengeCompletedState + BrushingInput = ChallengeCompleted + BrushingState
    ChallengeCompletedState + ChallengeCompletedInput = NoAction + ChallengeCompletedState
    ChallengeCompletedState + TierReachedInput = NoAction + TierReachedState
    ChallengeCompletedState + OtherInput = ChallengeCompleted + NoState
    ChallengeCompletedState + TerminusInput = ChallengeCompleted
    ChallengeCompletedState + StreakCompletedInput = ChallengeCompleted + StreakCompletedState
     */
    @Test
    fun `ChallengeCompletedState + BrushingInput = ChallengeCompleted entity`() {
        val challengeCompletedState = createChallengeCompletedState()

        assertTrue(challengeCompletedState.feedbackEntityFromInput(createBrushingInput())!!.isChallengesCompleted())
    }

    @Test
    fun `ChallengeCompletedState + BrushingInput = BrushingState`() {
        val challengeCompletedState = createChallengeCompletedState()

        val input = createBrushingInput()
        val expectedState = BrushingState(input)

        assertEquals(expectedState, challengeCompletedState.nextState(input))
    }

    @Test
    fun `ChallengeCompletedState + ChallengeCompletedInput = null entity`() {
        val challengeCompletedState = createChallengeCompletedState()

        assertNull(challengeCompletedState.feedbackEntityFromInput(createChallengeCompletedInput()))
    }

    @Test
    fun `ChallengeCompletedState + ChallengeCompletedInput with newer datetime = same ChallengeCompletedState with added challengeCompleted and new datetime`() {
        val originalDateTime = TrustedClock.getNowZonedDateTime().minusMinutes(1)
        val challengeCompletedState =
            createChallengeCompletedState(createChallengeCompletedInput(creationDateTime = originalDateTime))

        val newChallengeId = 656L
        val expectedDateTime = originalDateTime.plusSeconds(20)
        val newInput = createChallengeCompletedInput(
            challengeCompletedId = newChallengeId,
            creationDateTime = expectedDateTime
        )
        val expectedState = createChallengeCompletedState(inputEvent = newInput)
        expectedState.challengesCompleted.add(challengeCompletedState.mostRecentInputEvent.event.challengeId)

        assertEquals(expectedState, challengeCompletedState.nextState(newInput))
    }

    @Test
    fun `ChallengeCompletedState + ChallengeCompletedInput with older datetime = same ChallengeCompletedState with added challengeCompleted and original datetime`() {
        val originalDateTime = TrustedClock.getNowZonedDateTime().minusMinutes(1)
        val challengeCompletedState =
            createChallengeCompletedState(createChallengeCompletedInput(creationDateTime = originalDateTime))

        val newChallengeId = 656L
        val newEventInputTime = originalDateTime.minusSeconds(5)
        val input = createChallengeCompletedInput(
            challengeCompletedId = newChallengeId,
            creationDateTime = newEventInputTime
        )

        val expectedState = challengeCompletedState.copy()
        expectedState.challengesCompleted.add(newChallengeId)

        assertEquals(expectedState, challengeCompletedState.nextState(input))
    }

    @Test
    fun `ChallengeCompletedState + TierReachedInput = NoAction entity`() {
        val challengeCompletedState = createChallengeCompletedState()

        assertNull(challengeCompletedState.feedbackEntityFromInput(createTierReachedInput()))
    }

    @Test
    fun `ChallengeCompletedState + TierReachedInput = TierReachedState with challengeId and newer date`() {
        val originalDateTime = TrustedClock.getNowZonedDateTime().minusMinutes(1)
        val challengeCompletedState =
            createChallengeCompletedState(createChallengeCompletedInput(creationDateTime = originalDateTime))

        val newEventInputTime = originalDateTime.minusSeconds(5)
        val input = createTierReachedInput(creationDateTime = newEventInputTime)
        val expectedState = TierReachedState(input, challengeCompletedState.challengesCompleted)

        assertEquals(expectedState, challengeCompletedState.nextState(input))
    }

    @Test
    fun `ChallengeCompletedState + OtherInput = ChallengeCompleted entity`() {
        val challengeCompletedState = createChallengeCompletedState()

        assertTrue(challengeCompletedState.feedbackEntityFromInput(createOtherInput())!!.isChallengesCompleted())
    }

    @Test
    fun `ChallengeCompletedState + OtherInput = NoState`() {
        val challengeCompletedState = createChallengeCompletedState()

        val input = createOtherInput()

        assertEquals(NoState, challengeCompletedState.nextState(input))
    }

    @Test
    fun `ChallengeCompletedState + TerminusInput = ChallengeCompleted entity`() {
        val challengeCompletedState = createChallengeCompletedState()

        assertTrue(challengeCompletedState.feedbackEntityFromInput(TerminusInput)!!.isChallengesCompleted())
    }

    @Test
    fun `ChallengeCompletedState + TerminusInput = NoState`() {
        val challengeCompletedState = createChallengeCompletedState()

        assertEquals(NoState, challengeCompletedState.nextState(TerminusInput))
    }

    @Test
    fun `ChallengeCompletedState + StreakCompletedInput = ChallengeCompleted entity`() {
        val challengeCompletedState = createChallengeCompletedState()

        val input = createStreakCompletedInput()

        assertTrue(challengeCompletedState.feedbackEntityFromInput(input)!!.isChallengesCompleted())
    }

    @Test
    fun `ChallengeCompletedState + StreakCompletedInput = StreakCompletedState`() {
        val challengeCompletedState = createChallengeCompletedState()

        val input = createStreakCompletedInput()

        val expectedState = StreakCompletedState(input)

        assertEquals(expectedState, challengeCompletedState.nextState(input))
    }

    private fun createChallengeCompletedState(inputEvent: ChallengeCompletedInput = createChallengeCompletedInput()) =
        ChallengeCompletedState(inputEvent)

    /*

    BrushingState

    BrushingState + BrushingInput = SmilesEarned + BrushingState
    BrushingState + ChallengeCompletedInput = NoAction + ChallengeCompletedState
    BrushingState + TierReachedInput = SmilesEarned + NoState
    BrushingState + OtherInput = SmilesEarned + NoState
    BrushingState + TerminusInput = SmilesEarned
    BrushingState + StreakCompletedInput = SmilesEarned + StreakCompletedState
     */
    @Test
    fun `BrushingState + BrushingInput = SmilesEarned entity `() {
        val brushingState = createBrushingState()

        assertTrue(brushingState.feedbackEntityFromInput(createBrushingInput())!!.isSmilesEarned())
    }

    @Test
    fun `BrushingState + BrushingInput = BrushingState `() {
        val brushingState = createBrushingState()

        val input = createBrushingInput()
        val expectedState = BrushingState(input)

        assertEquals(expectedState, brushingState.nextState(input))
    }

    @Test
    fun `BrushingState + ChallengeCompletedInput = null entity `() {
        val brushingState = createBrushingState()

        assertNull(brushingState.feedbackEntityFromInput(createChallengeCompletedInput()))
    }

    @Test
    fun `BrushingState + ChallengeCompletedInput = ChallengeCompletedState `() {
        val brushingState = createBrushingState()

        val input = createChallengeCompletedInput()
        val expectedState = ChallengeCompletedState(input)

        assertEquals(expectedState, brushingState.nextState(input))
    }

    @Test
    fun `BrushingState + TierReachedInput = SmilesEarned entity `() {
        val brushingState = createBrushingState()

        assertTrue(brushingState.feedbackEntityFromInput(createTierReachedInput())!!.isSmilesEarned())
    }

    @Test
    fun `BrushingState + TierReachedInput = NoState `() {
        val brushingState = createBrushingState()

        val input = createTierReachedInput()

        assertEquals(NoState, brushingState.nextState(input))
    }

    @Test
    fun `BrushingState + OtherInput = SmilesEarned entity `() {
        val brushingState = createBrushingState()

        assertTrue(brushingState.feedbackEntityFromInput(createOtherInput())!!.isSmilesEarned())
    }

    @Test
    fun `BrushingState + OtherInput = NoState `() {
        val brushingState = createBrushingState()

        val input = createOtherInput()
        val expectedState = NoState

        assertEquals(expectedState, brushingState.nextState(input))
    }

    @Test
    fun `BrushingState + TerminusInput = SmilesEarned entity`() {
        val brushingState = createBrushingState()

        assertTrue(brushingState.feedbackEntityFromInput(TerminusInput)!!.isSmilesEarned())
    }

    @Test
    fun `BrushingState + TerminusInput = NoState`() {
        val brushingState = createBrushingState()

        assertEquals(NoState, brushingState.nextState(TerminusInput))
    }

    @Test
    fun `BrushingState + StreakCompletedInput = SmilesEarned entity`() {
        val brushingState = createBrushingState()

        val input = createStreakCompletedInput()

        assertTrue(brushingState.feedbackEntityFromInput(input)!!.isSmilesEarned())
    }

    @Test
    fun `BrushingState + StreakCompletedInput = StreakCompletedState`() {
        val brushingState = createBrushingState()

        val input = createStreakCompletedInput()

        val expectedState = StreakCompletedState(input)

        assertEquals(expectedState, brushingState.nextState(input))
    }

    private fun createBrushingState() = BrushingState(createBrushingInput())

    /*
    NoState

    NoState + BrushingInput = NoAction + BrushingState
    NoState + ChallengeCompletedInput = NoAction + ChallengeCompletedState
    NoState + TierReachedInput = NoAction + NoState
    NoState + OtherInput = NoAction + NoState
    NoState + TerminusInput = NoAction
    NoState + StreakCompletedInput = NoAction + StreakCompletedState
     */
    @Test
    fun `NoState + BrushingInput = null entity`() {
        assertNull(NoState.feedbackEntityFromInput(createBrushingInput()))
    }

    @Test
    fun `NoState + BrushingInput = BrushingState`() {
        val input = createBrushingInput()
        val expectedState = BrushingState(input)

        assertEquals(expectedState, NoState.nextState(input))
    }

    @Test
    fun `NoState + ChallengeCompletedInput = null entity`() {
        assertNull(NoState.feedbackEntityFromInput(createChallengeCompletedInput()))
    }

    @Test
    fun `NoState + ChallengeCompletedInput = ChallengeCompletedState`() {
        val input = createChallengeCompletedInput()
        val expectedState = ChallengeCompletedState(input)

        assertEquals(expectedState, NoState.nextState(input))
    }

    @Test
    fun `NoState + TierReachedInput = null entity`() {
        assertNull(NoState.feedbackEntityFromInput(createTierReachedInput()))
    }

    @Test
    fun `NoState + TierReachedInput = NoState`() {
        assertEquals(NoState, NoState.nextState(createTierReachedInput()))
    }

    @Test
    fun `NoState + OtherInput = null entity`() {
        assertNull(NoState.feedbackEntityFromInput(createOtherInput()))
    }

    @Test
    fun `NoState + OtherInput = NoState`() {
        assertEquals(NoState, NoState.nextState(createOtherInput()))
    }

    @Test
    fun `NoState + TerminusInput = null entity`() {
        assertNull(NoState.feedbackEntityFromInput(TerminusInput))
    }

    @Test
    fun `NoState + TerminusInput = NoState`() {
        assertEquals(NoState, NoState.nextState(TerminusInput))
    }

    @Test
    fun `reset sets state to NoState`() {
        val stateMachine = FeedbackStateMachine()
        val input = createChallengeCompletedInput()
        val challengeCompletedState = ChallengeCompletedState(input)
        stateMachine.state = challengeCompletedState

        stateMachine.reset()

        assertEquals(NoState, stateMachine.state)
    }

    @Test
    fun `terminusEvent invokes reset`() {
        val stateMachine = spy(FeedbackStateMachine())
        stateMachine.terminusEvent()

        verify(stateMachine).reset()
    }

    @Test
    fun `NoState + StreakCompletedInput = null entity`() {
        val input = createStreakCompletedInput()

        assertNull(NoState.feedbackEntityFromInput(input))
    }

    @Test
    fun `NoState + StreakCompletedInput = StreakCompletedState`() {
        val input = createStreakCompletedInput()

        val expectedState = StreakCompletedState(input)

        assertEquals(expectedState, NoState.nextState(input))
    }

    /*
    StreakCompletedState

    StreakCompletedState + BrushingInput = StreakCompleted + BrushingState
    StreakCompletedState + ChallengeCompletedInput = StreakCompleted + ChallengeCompletedState
    StreakCompletedState + TierReachedInput = StreakCompleted  + NoState
    StreakCompletedState + StreakCompletedInput = NoAction + StreakCompletedState
    StreakCompletedState + OtherInput = StreakCompleted + NoState
    StreakCompletedState + TerminusInput = StreakCompleted
    */

    @Test
    fun `StreakCompletedState + BrushingInput = StreakCompleted entity`() {
        val input = createBrushingInput()

        assertTrue(createStreakCompletedState().feedbackEntityFromInput(input)!!.isStreakCompleted())
    }

    @Test
    fun `StreakCompletedState + BrushingInput = BrushingState`() {
        val input = createBrushingInput()

        val expectedState = BrushingState(input)

        assertEquals(expectedState, createStreakCompletedState().nextState(input))
    }

    @Test
    fun `StreakCompletedState + ChallengeCompletedInput = StreakCompleted entity`() {
        val input = createChallengeCompletedInput()

        assertTrue(createStreakCompletedState().feedbackEntityFromInput(input)!!.isStreakCompleted())
    }

    @Test
    fun `StreakCompletedState + ChallengeCompletedInput = ChallengeCompletedState`() {
        val input = createChallengeCompletedInput()

        val expectedState = ChallengeCompletedState(input)

        assertEquals(expectedState, createStreakCompletedState().nextState(input))
    }

    @Test
    fun `StreakCompletedState + TierReachedInput = StreakCompleted entity`() {
        val input = createTierReachedInput()

        assertTrue(createStreakCompletedState().feedbackEntityFromInput(input)!!.isStreakCompleted())
    }

    @Test
    fun `StreakCompletedState + TierReachedInput = NoState`() {
        val input = createTierReachedInput()

        assertEquals(NoState, createStreakCompletedState().nextState(input))
    }

    @Test
    fun `StreakCompletedState + StreakCompletedInput = null entity`() {
        val input = createStreakCompletedInput()

        assertNull(createStreakCompletedState().feedbackEntityFromInput(input))
    }

    @Test
    fun `StreakCompletedState + StreakCompletedInput = StreakCompletedState`() {
        val input = createStreakCompletedInput()

        val expectedState = StreakCompletedState(input)

        assertEquals(expectedState, createStreakCompletedState().nextState(input))
    }

    @Test
    fun `StreakCompletedState + OtherInput = StreakCompleted entity`() {
        val input = createOtherInput()

        assertTrue(createStreakCompletedState().feedbackEntityFromInput(input)!!.isStreakCompleted())
    }

    @Test
    fun `StreakCompletedState + OtherInput = NoState`() {
        val input = createOtherInput()

        assertEquals(NoState, createStreakCompletedState().nextState(input))
    }

    @Test
    fun `StreakCompletedState + TerminusInput = StreakCompleted entity`() {
        val input = createOtherInput()

        assertTrue(createStreakCompletedState().feedbackEntityFromInput(input)!!.isStreakCompleted())
    }

    @Test
    fun `StreakCompletedState + TerminusInput = NoState`() {
        assertEquals(NoState, createStreakCompletedState().nextState(TerminusInput))
    }

    private fun createStreakCompletedState() = StreakCompletedState(createStreakCompletedInput())

    /*
    UTILS
     */

    private fun createOtherInput(event: BrushingSessionHistoryEvent = createBrushingSessionHistoryEvent()) =
        OtherInput(event)

    private fun createBrushingInput(event: BrushingSessionHistoryEvent = createBrushingSessionHistoryEvent()) =
        BrushingInput(event)

    private fun createTierReachedInput(creationDateTime: ZonedDateTime = TrustedClock.getNowZonedDateTime()) =
        TierReachedInput(createTierReachedHistoryEvent(creationTime = creationDateTime))

    private fun createStreakCompletedInput(creationDateTime: ZonedDateTime = TrustedClock.getNowZonedDateTime()) =
        StreakCompletedInput(createStreakCompletedHistoryEvent(creationTime = creationDateTime))

    private fun createChallengeCompletedInput(
        challengeCompletedId: Long = DEFAULT_CHALLENGE_ID,
        creationDateTime: ZonedDateTime = TrustedClock.getNowZonedDateTime()
    ) =
        ChallengeCompletedInput(
            createChallengeCompletedHistoryEvent(
                challengeId = challengeCompletedId,
                creationTime = creationDateTime
            )
        )
}
