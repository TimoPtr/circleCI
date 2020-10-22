/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.mapper

import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem

internal fun PersonalChallengeEntity.toSynchronizableItem(): ProfilePersonalChallengeSynchronizableItem =
    ProfilePersonalChallengeSynchronizableItem(
        backendId = backendId,
        kolibreeId = profileId,
        challenge = toV1Challenge(),
        updatedAt = updateDate,
        uuid = uuid
    )
