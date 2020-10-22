/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profiletier

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.models.ProfileTierEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject

internal class ProfileTierSynchronizableReadOnlyApi
@Inject constructor(private val rewardsApi: RewardsApi) : SynchronizableReadOnlyApi {
    override fun get(id: Long): SynchronizableReadOnly {
        val response = rewardsApi.getProfileTier(id).execute()

        if (response.isSuccessful) {
            val profileTierApi = response.body()!!

            return ProfileTierEntity(id, profileTierApi.tierId)
        } else {
            throw errorResponseToApiError(response)
        }
    }
}
