package com.kolibree.android.rewards.synchronization.challenges

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableCatalogApi
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import javax.inject.Inject

internal class ChallengesSynchronizableCatalogApi
@Inject constructor(private val rewardsApi: RewardsApi) : SynchronizableCatalogApi {
    override fun get(): SynchronizableCatalog {
        val response = rewardsApi.getChallengesCatalog().execute()

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw errorResponseToApiError(response)
        }
    }
}
