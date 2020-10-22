/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class ProfilePersonalChallengeSynchronizationKeyBuilder @Inject constructor(
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.PERSONAL_CHALLENGE) {
    override fun version(): Int = rewardsSynchronizedVersions.personalChallengeVersion()
}
