/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.mapper

import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeRequest
import com.kolibree.android.rewards.personalchallenge.data.api.model.toApiRequest
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringify
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyDuration
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyUnit
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem

internal fun ProfilePersonalChallengeSynchronizableItem.toApiRequest(): PersonalChallengeRequest =
    challenge.toApiRequest()

internal fun ProfilePersonalChallengeSynchronizableItem.toPersistentEntity(
    id: Long = 0
): PersonalChallengeEntity = PersonalChallengeEntity(
    id = id,
    backendId = backendId,
    profileId = profileId,
    progress = challenge.progress,
    completionDate = challenge.completionDate,
    creationDate = challenge.creationDate,
    updateDate = updatedAt,
    duration = challenge.period.stringifyDuration(),
    durationUnit = challenge.period.stringifyUnit(),
    difficultyLevel = challenge.difficultyLevel.stringify(),
    objectiveType = challenge.objectiveType.stringify(),
    uuid = uuid
)
