/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import javax.inject.Inject

internal class ProfileSmilesHistorySynchronizableReadOnlyApi
@Inject constructor(private val rewardsApi: RewardsApi) : SynchronizableReadOnlyApi {
    override fun get(id: Long): SynchronizableReadOnly {
        val response = rewardsApi.getSmilesHistory(id).execute()

        if (response.isSuccessful) {
            val profileSmilesHistoryApi = response.body() ?: throw EmptyBodyException(response)

            return ProfileSmilesHistoryApiWithProfileId(
                profileId = id,
                profileSmilesHistoryApi = profileSmilesHistoryApi
            )
        } else {
            throw errorResponseToApiError(response)
        }
    }
}
