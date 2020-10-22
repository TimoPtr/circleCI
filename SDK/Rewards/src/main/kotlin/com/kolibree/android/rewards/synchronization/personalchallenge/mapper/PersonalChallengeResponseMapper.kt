/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.mapper

import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItem

internal fun PersonalChallengeResponse.toSynchronizableItem(
    kolibreeId: Long
): SynchronizableItem = ProfilePersonalChallengeSynchronizableItem(
    backendId = id,
    kolibreeId = kolibreeId,
    challenge = toV1Challenge()
)
