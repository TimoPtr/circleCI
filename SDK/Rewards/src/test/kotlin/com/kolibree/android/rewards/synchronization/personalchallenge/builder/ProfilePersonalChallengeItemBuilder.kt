/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.builder

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import java.util.UUID
import org.threeten.bp.ZonedDateTime

internal fun synchroItem(
    backendId: Long? = null,
    kolibreeId: Long = 1000,
    objectiveType: PersonalChallengeType = PersonalChallengeType.STREAK,
    difficultyLevel: PersonalChallengeLevel = PersonalChallengeLevel.EASY,
    period: PersonalChallengePeriod = PersonalChallengePeriod.SEVEN_DAYS,
    creationDate: ZonedDateTime = TrustedClock.getNowZonedDateTime().minusDays(8),
    completionDate: ZonedDateTime? = null,
    progress: Int = 0,
    updatedAt: ZonedDateTime = TrustedClock.getNowZonedDateTime(),
    uuid: UUID? = null
) = ProfilePersonalChallengeSynchronizableItem(
    backendId = backendId,
    kolibreeId = kolibreeId,
    challenge = v1PersonalChallenge(
        objectiveType = objectiveType,
        difficultyLevel = difficultyLevel,
        period = period,
        creationDate = creationDate,
        completionDate = completionDate,
        progress = progress
    ),
    updatedAt = updatedAt,
    uuid = uuid
)

internal fun v1PersonalChallenge(
    objectiveType: PersonalChallengeType = PersonalChallengeType.STREAK,
    difficultyLevel: PersonalChallengeLevel = PersonalChallengeLevel.EASY,
    period: PersonalChallengePeriod = PersonalChallengePeriod.SEVEN_DAYS,
    creationDate: ZonedDateTime = TrustedClock.getNowZonedDateTime().minusDays(8),
    completionDate: ZonedDateTime? = null,
    progress: Int = 0
): V1PersonalChallenge {
    return V1PersonalChallenge(
        objectiveType = objectiveType,
        difficultyLevel = difficultyLevel,
        period = period,
        creationDate = creationDate,
        completionDate = completionDate,
        progress = progress
    )
}
