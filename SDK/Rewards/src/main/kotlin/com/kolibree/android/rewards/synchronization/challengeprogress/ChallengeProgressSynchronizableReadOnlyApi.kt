package com.kolibree.android.rewards.synchronization.challengeprogress

import androidx.annotation.VisibleForTesting
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.kolibree.android.rewards.persistence.ChallengeProgressProfileCatalogInternal
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject

internal class ChallengeProgressSynchronizableReadOnlyApi
@Inject constructor(private val rewardsApi: RewardsApi) : SynchronizableReadOnlyApi {
    override fun get(id: Long): SynchronizableReadOnly {
        val response = rewardsApi.getChallengeProgress(id).execute()

        if (response.isSuccessful) {
            val challengeProgressProfileCatalogApi: ChallengeProgressApi = response.body()!!

            return ChallengeProgressProfileInternalMapper.toChallengeProgressProfileInternal(
                challengeProgressProfileCatalogApi,
                id
            )
        } else {
            throw errorResponseToApiError(response)
        }
    }
}

@VisibleForTesting
internal object ChallengeProgressProfileInternalMapper {
    fun toChallengeProgressProfileInternal(
        challengesFromApi: ChallengeProgressApi,
        profileId: Long
    ): ChallengeProgressProfileCatalogInternal {
        return ChallengeProgressProfileCatalogInternal().apply {
            addAll(challengesFromApi.allChallenges().map { challengeProgressApi ->
                ChallengeProgressEntity(
                    challengeProgressApi.challengeId,
                    profileId,
                    challengeProgressApi.completionTime,
                    challengeProgressApi.completionDetails,
                    challengeProgressApi.percentage
                )
            })
        }
    }
}
