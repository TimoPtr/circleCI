/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class ProfileSmilesHistorySynchronizationKeyBuilder
@Inject constructor(private val rewardsSynchronizedVersions: RewardsSynchronizedVersions) :
    SynchronizeAccountKeyBuilder(SynchronizableKey.PROFILE_SMILES_HISTORY) {
    override fun version(): Int = rewardsSynchronizedVersions.smilesHistoryVersion()
}
