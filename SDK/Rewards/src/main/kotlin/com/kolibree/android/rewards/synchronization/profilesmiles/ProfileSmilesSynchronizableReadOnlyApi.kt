/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmiles

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.models.ProfileSmilesEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject

internal class ProfileSmilesSynchronizableReadOnlyApi
@Inject constructor(private val rewardsApi: RewardsApi) : SynchronizableReadOnlyApi {
    override fun get(id: Long): SynchronizableReadOnly {
        val response = rewardsApi.getProfileSmiles(id).execute()

        if (response.isSuccessful) {
            val profileSmiles = response.body()!!

            return ProfileSmilesEntity(id, profileSmiles.smiles)
        } else {
            throw errorResponseToApiError(response)
        }
    }
}
