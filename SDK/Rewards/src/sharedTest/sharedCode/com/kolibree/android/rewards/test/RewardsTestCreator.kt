/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.test

import com.kolibree.android.rewards.feedback.FeedbackEntity
import com.kolibree.android.rewards.models.BrushingSessionHistoryEventStatus
import com.kolibree.android.rewards.models.EVENT_TYPE_BRUSHING_SESSION
import com.kolibree.android.rewards.models.OFFLINE_BRUSHING_TYPE_SUCCESS
import com.kolibree.android.rewards.models.OfflineBrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import com.kolibree.android.rewards.models.StreakCompletedHistoryEvent
import com.kolibree.android.rewards.synchronization.SMILES_HISTORY_DATETIME_FORMATTER
import com.kolibree.android.rewards.synchronization.profilesmileshistory.SmilesHistoryEventApi
import com.kolibree.android.test.mocks.ProfileBuilder
import org.threeten.bp.ZonedDateTime

const val DEFAULT_CREATION_TIME = "2018-12-20T12:50:24.361950+00:00"
const val DEFAULT_MESSAGE = "Congratulations!"
const val DEFAULT_SMILES_REWARDS = 3
const val DEFAULT_TIER_LEVEL = 4
const val DEFAULT_CHALLENGE_ID = 123L
const val DEFAULT_BRUSHING_ID = 22L
const val DEFAULT_BRUSHING_TYPE = "coach"
const val DEFAULT_EVENT_TYPE = EVENT_TYPE_BRUSHING_SESSION
internal val DEFAULT_BRUSHING_SESSION_STATUS = BrushingSessionHistoryEventStatus.COMPLETED

internal fun createSmilesHistoryEventApi(
    @com.kolibree.android.rewards.models.SmilesHistoryEventType eventType: String,
    message: String = DEFAULT_MESSAGE,
    smilesRewards: Int = DEFAULT_SMILES_REWARDS,
    creationTime: String = DEFAULT_CREATION_TIME,
    challengeId: Long? = null,
    brushingId: Long? = null,
    brushingType: String? = null,
    tierLevel: Int? = null,
    rewardsId: Long? = null,
    relatedProfileId: Long? = null
): SmilesHistoryEventApi {
    return createSmilesHistoryEventApiWithDateTime(
        eventType = eventType,
        message = message,
        smilesRewards = smilesRewards,
        challengeId = challengeId,
        brushingId = brushingId,
        brushingType = brushingType,
        tierLevel = tierLevel,
        rewardsId = rewardsId,
        relatedProfileId = relatedProfileId,
        creationTime = ZonedDateTime.parse(
            creationTime,
            SMILES_HISTORY_DATETIME_FORMATTER
        )
    )
}

internal fun createSmilesHistoryEventApiWithDateTime(
    @com.kolibree.android.rewards.models.SmilesHistoryEventType eventType: String,
    message: String = DEFAULT_MESSAGE,
    smilesRewards: Int = DEFAULT_SMILES_REWARDS,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    challengeId: Long? = null,
    brushingId: Long? = null,
    brushingType: String? = null,
    tierLevel: Int? = null,
    rewardsId: Long? = null,
    relatedProfileId: Long? = null
): SmilesHistoryEventApi {
    return SmilesHistoryEventApi(
        eventType = eventType,
        creationTime = creationTime,
        message = message,
        smilesRewards = smilesRewards,
        challengeId = challengeId,
        brushingId = brushingId,
        brushingType = brushingType,
        tierLevel = tierLevel,
        rewardsId = rewardsId,
        relatedProfileId = relatedProfileId
    )
}

internal fun createFeedbackEntity(
    profileId: Long,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    smilesEarned: Int = 0,
    challengesCompleted: List<Long> = listOf(),
    tierReached: Int = 0
): FeedbackEntity {
    return FeedbackEntity(
        profileId = profileId,
        historyEventDateTime = creationTime,
        smilesEarned = smilesEarned,
        challengesCompleted = challengesCompleted,
        tierReached = tierReached
    )
}

internal fun createChallengeCompletedHistoryEvent(
    smiles: Int = DEFAULT_SMILES_REWARDS,
    message: String = DEFAULT_MESSAGE,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    challengeId: Long = DEFAULT_CHALLENGE_ID
) =
    com.kolibree.android.rewards.models.ChallengeCompletedHistoryEvent(
        profileId = profileId,
        message = message,
        smiles = smiles,
        creationTime = creationTime,
        challengeId = challengeId
    )

internal fun createTierReachedHistoryEvent(
    smiles: Int = DEFAULT_SMILES_REWARDS,
    message: String = DEFAULT_MESSAGE,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    tierLevel: Int = DEFAULT_TIER_LEVEL
) = com.kolibree.android.rewards.models.TierReachedHistoryEvent(
    profileId = profileId,
    message = message,
    smiles = smiles,
    creationTime = creationTime,
    tierLevel = tierLevel
)

internal fun createBrushingSessionHistoryEvent(
    smiles: Int = DEFAULT_SMILES_REWARDS,
    message: String = DEFAULT_MESSAGE,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    brushingId: Long = DEFAULT_BRUSHING_ID,
    brushingType: String = DEFAULT_BRUSHING_TYPE,
    status: BrushingSessionHistoryEventStatus = DEFAULT_BRUSHING_SESSION_STATUS
) = com.kolibree.android.rewards.models.BrushingSessionHistoryEvent(
    profileId = profileId,
    message = message,
    smiles = smiles,
    creationTime = creationTime,
    brushingId = brushingId,
    brushingType = brushingType,
    status = status
)

internal fun createStreakCompletedHistoryEvent(
    smiles: Int = DEFAULT_SMILES_REWARDS,
    message: String = DEFAULT_MESSAGE,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    relatedProfileId: Long = ProfileBuilder.DEFAULT_ID
) = StreakCompletedHistoryEvent(
    profileId = profileId,
    message = message,
    smiles = smiles,
    creationTime = creationTime,
    relatedProfileId = relatedProfileId
)

internal fun createOfflineBrushingSessionHistoryEvent(
    smiles: Int = DEFAULT_SMILES_REWARDS,
    message: String = DEFAULT_MESSAGE,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    brushingId: Long = DEFAULT_BRUSHING_ID,
    brushingType: String = OFFLINE_BRUSHING_TYPE_SUCCESS,
    status: BrushingSessionHistoryEventStatus = DEFAULT_BRUSHING_SESSION_STATUS
) = OfflineBrushingSessionHistoryEvent(
    profileId = profileId,
    message = message,
    smiles = smiles,
    creationTime = creationTime,
    brushingId = brushingId,
    brushingType = brushingType,
    status = status
)

internal fun createSmilesHistoryEventEntity(
    id: Long = 0L,
    smiles: Int = DEFAULT_SMILES_REWARDS,
    message: String = DEFAULT_MESSAGE,
    creationTime: ZonedDateTime = ZonedDateTime.parse(DEFAULT_CREATION_TIME, SMILES_HISTORY_DATETIME_FORMATTER),
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    eventType: String = DEFAULT_EVENT_TYPE,
    challengeId: Long? = null,
    brushingId: Long? = null,
    brushingType: String? = null,
    tierLevel: Int? = null,
    rewardsId: Long? = null,
    relatedProfileId: Long? = null
): SmilesHistoryEventEntity {
    return SmilesHistoryEventEntity(
        id = id,
        profileId = profileId,
        message = message,
        eventType = eventType,
        smiles = smiles,
        creationTime = creationTime,
        challengeId = challengeId,
        brushingId = brushingId,
        brushingType = brushingType,
        tierLevel = tierLevel,
        rewardsId = rewardsId,
        relatedProfileId = relatedProfileId
    )
}

internal fun SmilesHistoryEventApi.toEntity(profileId: Long): SmilesHistoryEventEntity {
    return SmilesHistoryEventEntity(
        profileId = profileId,
        message = message,
        eventType = eventType,
        smiles = smilesRewards,
        creationTime = creationTime,
        challengeId = challengeId,
        brushingId = brushingId,
        brushingType = brushingType,
        tierLevel = tierLevel,
        rewardsId = rewardsId,
        relatedProfileId = relatedProfileId
    )
}
