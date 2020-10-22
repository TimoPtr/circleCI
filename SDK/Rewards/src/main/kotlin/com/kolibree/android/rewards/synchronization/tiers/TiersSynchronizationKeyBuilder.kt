/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.tiers

import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class TiersSynchronizationKeyBuilder
@Inject constructor(private val rewardsSynchronizedVersions: RewardsSynchronizedVersions) :
    SynchronizeAccountKeyBuilder(SynchronizableKey.TIERS_CATALOG) {
    override fun version(): Int = rewardsSynchronizedVersions.tiersCatalogVersion()
}
