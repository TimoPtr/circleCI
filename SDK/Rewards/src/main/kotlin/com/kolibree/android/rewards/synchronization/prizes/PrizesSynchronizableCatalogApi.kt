/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.prizes

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableCatalogApi
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import javax.inject.Inject

internal class PrizesSynchronizableCatalogApi
@Inject constructor(private val rewardsApi: RewardsApi) : SynchronizableCatalogApi {
    override fun get(): SynchronizableCatalog {
        val response = rewardsApi.getAllPrizes().execute()

        if (response.isSuccessful) {
            return response.body() ?: throw EmptyBodyException(response)
        } else {
            throw errorResponseToApiError(response)
        }
    }
}
