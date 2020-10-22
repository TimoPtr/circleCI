/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import androidx.annotation.VisibleForTesting
import com.kolibree.android.rewards.models.BrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.ChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.models.SmilesHistoryEvent
import com.kolibree.android.rewards.models.StreakCompletedHistoryEvent
import com.kolibree.android.rewards.models.TierReachedHistoryEvent
import javax.inject.Inject

internal class HistoryToFeedbackMapper @Inject constructor(private val feedbackStateMachine: FeedbackStateMachine) {
    /**
     * Given a List of N smiles history events, returns a list of FeedbackEntity items of at most N items
     */
    fun map(itemsToProcess: List<SmilesHistoryEvent>): List<FeedbackEntity> {
        return itemsToProcess.fold(mutableListOf<FeedbackEntity?>()) { entityList, historyEvent ->
            entityList.add(feedbackStateMachine.consume(historyEvent))

            entityList
        }
            .apply {
                add(feedbackStateMachine.terminusEvent())
            }
            .filterNotNull()
    }
}

internal class FeedbackStateMachine @Inject constructor() {
    @VisibleForTesting
    var state: FeedbackState =
        NoState

    fun consume(input: SmilesHistoryEvent): FeedbackEntity? {
        val oldState = state

        val historyInput = input.toHistoryInput()
        state = oldState.nextState(historyInput)

        return oldState.feedbackEntityFromInput(historyInput)
    }

    fun terminusEvent(): FeedbackEntity? {
        val feedback = state.feedbackEntityFromInput(TerminusInput)
        reset()
        return feedback
    }

    @VisibleForTesting
    fun reset() {
        state = NoState
    }
}

private fun SmilesHistoryEvent.toHistoryInput(): HistoryInput {
    return when (val specificEvent = toSpecificEvent()) {
        is ChallengeCompletedHistoryEvent -> ChallengeCompletedInput(
            specificEvent
        )
        is BrushingSessionHistoryEvent -> BrushingInput(
            specificEvent
        )
        is TierReachedHistoryEvent -> TierReachedInput(
            specificEvent
        )
        is StreakCompletedHistoryEvent -> StreakCompletedInput(
            specificEvent
        )
        else -> OtherInput(specificEvent)
    }
}

/*
State machine

History events inputs: Brushing, StreakCompletedInput, ChallengeCompleted, TierReached, Other, Terminus

Given a State, when an input is received, it produces a FeedbackAction and the state machine transitions to a new state

Example:
CurrentState + HistoryInput = FeedbackAction + NewState

State machine description
-------------------------

NoState + BrushingInput = NoAction + BrushingState
NoState + ChallengeCompletedInput = NoAction + ChallengeCompletedState
NoState + TierReachedInput = NoAction + NoState //Input not supported, transitioning to NoState
NoState + StreakCompletedInput = NoAction + StreakCompletedState
NoState + OtherInput = NoAction + NoState
NoState + TerminusInput = NoAction

BrushingState + BrushingInput = SmilesEarned + BrushingState
BrushingState + ChallengeCompletedInput = NoAction + ChallengeCompletedState
BrushingState + TierReachedInput = SmilesEarned + NoState //Input not supported, transitioning to NoState
BrushingState + StreakCompletedInput = SmilesEarned + StreakCompletedState
BrushingState + OtherInput = SmilesEarned + NoState
BrushingState + TerminusInput = SmilesEarned

ChallengeCompletedState + BrushingInput = ChallengeCompleted + BrushingState
ChallengeCompletedState + ChallengeCompletedInput = NoAction + ChallengeCompletedState
ChallengeCompletedState + TierReachedInput = NoAction + TierReachedState
ChallengeCompletedState + StreakCompletedInput = ChallengeCompleted + StreakCompletedState
ChallengeCompletedState + OtherInput = ChallengeCompleted + NoState
ChallengeCompletedState + TerminusInput = ChallengeCompleted

TierReachedState + BrushingInput = TierReached + BrushingState
TierReachedState + ChallengeCompletedInput = TierReached + ChallengeCompletedState
TierReachedState + TierReachedInput = TierReached + NoState //Input not supported, transitioning to NoState
TierReachedState + StreakCompletedInput = TierReached + StreakCompletedState
TierReachedState + OtherInput = TierReached + NoState
TierReachedState + TerminusInput = TierReached

StreakCompletedState + BrushingInput = StreakCompleted + BrushingState
StreakCompletedState + ChallengeCompletedInput = StreakCompleted + ChallengeCompletedState
StreakCompletedState + TierReachedInput = StreakCompleted  + NoState //Input not supported, transitioning to NoState
StreakCompletedState + StreakCompletedInput = NoAction + StreakCompletedState
StreakCompletedState + OtherInput = StreakCompleted + NoState
StreakCompletedState + TerminusInput = StreakCompleted
 */

/**
 * Inputs
 */
@VisibleForTesting
internal sealed class HistoryInput

@VisibleForTesting
internal data class BrushingInput(val event: BrushingSessionHistoryEvent) : HistoryInput()

@VisibleForTesting
internal data class StreakCompletedInput(val event: StreakCompletedHistoryEvent) : HistoryInput()

@VisibleForTesting
internal data class ChallengeCompletedInput(val event: ChallengeCompletedHistoryEvent) : HistoryInput()

@VisibleForTesting
internal data class TierReachedInput(val event: TierReachedHistoryEvent) : HistoryInput()

@VisibleForTesting
internal data class OtherInput(val event: SmilesHistoryEvent) : HistoryInput()

@VisibleForTesting
internal object TerminusInput : HistoryInput()

/**
 * State
 */
@VisibleForTesting
internal sealed class FeedbackState {
    abstract fun feedbackEntityFromInput(inputEvent: HistoryInput): FeedbackEntity?

    abstract fun nextState(inputEvent: HistoryInput): FeedbackState
}

@VisibleForTesting
internal object NoState : FeedbackState() {
    override fun nextState(inputEvent: HistoryInput): FeedbackState {
        return when (inputEvent) {
            is BrushingInput -> BrushingState(
                inputEvent
            )
            is ChallengeCompletedInput -> ChallengeCompletedState(
                inputEvent
            )
            is TierReachedInput -> NoState
            is StreakCompletedInput -> StreakCompletedState(
                inputEvent
            )
            is OtherInput -> NoState
            is TerminusInput -> NoState
        }
    }

    override fun feedbackEntityFromInput(inputEvent: HistoryInput): FeedbackEntity? = null
}

@VisibleForTesting
internal data class BrushingState(val brushingInput: BrushingInput) : FeedbackState() {
    override fun nextState(inputEvent: HistoryInput): FeedbackState {
        return when (inputEvent) {
            is BrushingInput -> BrushingState(
                inputEvent
            )
            is ChallengeCompletedInput -> ChallengeCompletedState(
                inputEvent
            )
            is TierReachedInput -> NoState
            is StreakCompletedInput -> StreakCompletedState(
                inputEvent
            )
            is OtherInput -> NoState
            is TerminusInput -> NoState
        }
    }

    override fun feedbackEntityFromInput(inputEvent: HistoryInput): FeedbackEntity? {
        return when (inputEvent) {
            is BrushingInput -> createSmilesEarnedEntity()
            is ChallengeCompletedInput -> null
            is TierReachedInput -> createSmilesEarnedEntity()
            is StreakCompletedInput -> createSmilesEarnedEntity()
            is OtherInput -> createSmilesEarnedEntity()
            is TerminusInput -> createSmilesEarnedEntity()
        }
    }

    private fun createSmilesEarnedEntity(): FeedbackEntity =
        FeedbackEntity.createSmilesEarnedEntity(
            profileId = brushingInput.event.profileId,
            historyEventDateTime = brushingInput.event.creationTime,
            smilesEarned = brushingInput.event.smiles
        )
}

@VisibleForTesting
internal data class ChallengeCompletedState(
    val mostRecentInputEvent: ChallengeCompletedInput,
    val challengesCompleted: MutableSet<Long> = mutableSetOf()
) : FeedbackState() {

    init {
        if (challengesCompleted.isEmpty()) {
            challengesCompleted.add(mostRecentInputEvent.event.challengeId)
        }
    }

    override fun nextState(inputEvent: HistoryInput): FeedbackState {
        return when (inputEvent) {
            is BrushingInput -> BrushingState(
                inputEvent
            )
            is ChallengeCompletedInput -> {
                challengesCompleted.add(inputEvent.event.challengeId)

                copy(mostRecentInputEvent = mostRecentEvent(inputEvent))
            }
            is TierReachedInput -> TierReachedState(
                inputEvent,
                challengesCompleted
            )
            is StreakCompletedInput -> StreakCompletedState(
                inputEvent
            )
            is OtherInput -> NoState
            is TerminusInput -> NoState
        }
    }

    private fun mostRecentEvent(inputEvent: ChallengeCompletedInput): ChallengeCompletedInput {
        val newEventDateTime = inputEvent.event.creationTime
        val previousDateTime = mostRecentInputEvent.event.creationTime

        return if (newEventDateTime.isAfter(previousDateTime)) inputEvent else mostRecentInputEvent
    }

    override fun feedbackEntityFromInput(inputEvent: HistoryInput): FeedbackEntity? {
        return when (inputEvent) {
            is BrushingInput -> createChallengeCompletedEntity()
            is ChallengeCompletedInput -> null
            is TierReachedInput -> null
            is StreakCompletedInput -> createChallengeCompletedEntity()
            is OtherInput -> createChallengeCompletedEntity()
            is TerminusInput -> createChallengeCompletedEntity()
        }
    }

    private fun createChallengeCompletedEntity(): FeedbackEntity {
        return FeedbackEntity.createChallengeCompletedEntity(
            profileId = mostRecentInputEvent.event.profileId,
            historyEventDateTime = mostRecentInputEvent.event.creationTime,
            challengesCompleted = challengesCompleted.toList()
        )
    }
}

@VisibleForTesting
internal data class TierReachedState(val tierReachedInput: TierReachedInput, val challengesCompleted: Set<Long>) :
    FeedbackState() {

    override fun nextState(inputEvent: HistoryInput): FeedbackState {
        return when (inputEvent) {
            is BrushingInput -> BrushingState(
                inputEvent
            )
            is ChallengeCompletedInput -> ChallengeCompletedState(
                inputEvent
            )
            is TierReachedInput -> NoState
            is StreakCompletedInput -> StreakCompletedState(
                inputEvent
            )
            is OtherInput -> NoState
            is TerminusInput -> NoState
        }
    }

    override fun feedbackEntityFromInput(inputEvent: HistoryInput): FeedbackEntity? {
        return createTierReachedEntity()
    }

    private fun createTierReachedEntity(): FeedbackEntity {
        return FeedbackEntity.createTierReachedEntity(
            profileId = tierReachedInput.event.profileId,
            historyEventDateTime = tierReachedInput.event.creationTime,
            tierReached = tierReachedInput.event.tierLevel,
            challengesCompleted = challengesCompleted.toList()
        )
    }
}

@VisibleForTesting
internal data class StreakCompletedState(
    val input: StreakCompletedInput
) : FeedbackState() {

    override fun nextState(inputEvent: HistoryInput): FeedbackState {
        return when (inputEvent) {
            is BrushingInput -> BrushingState(
                inputEvent
            )
            is ChallengeCompletedInput -> ChallengeCompletedState(
                inputEvent
            )
            is TierReachedInput -> NoState
            is StreakCompletedInput -> StreakCompletedState(
                inputEvent
            )
            is OtherInput -> NoState
            is TerminusInput -> NoState
        }
    }

    override fun feedbackEntityFromInput(inputEvent: HistoryInput): FeedbackEntity? {
        return when (inputEvent) {
            is StreakCompletedInput -> null
            else -> createStreakCompletedEntity()
        }
    }

    private fun createStreakCompletedEntity(): FeedbackEntity {
        return FeedbackEntity.createStreakCompletedEntity(
            relatedProfileId = input.event.relatedProfileId,
            historyEventDateTime = input.event.creationTime,
            streakSmilesEarned = input.event.smiles
        )
    }
}
