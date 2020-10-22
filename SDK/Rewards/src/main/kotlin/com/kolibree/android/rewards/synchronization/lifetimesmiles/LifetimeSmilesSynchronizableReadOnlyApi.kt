/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.lifetimesmiles

import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import retrofit2.Response
import timber.log.Timber

internal class LifetimeSmilesSynchronizableReadOnlyApi
@Inject constructor(
    private val accountDataStore: AccountDatastore,
    private val rewardsApi: RewardsApi
) : SynchronizableReadOnlyApi {
    override fun get(id: Long): SynchronizableReadOnly {
        account()?.id?.let { accountId ->
            val response: Response<LifetimeSmilesResponse> =
                rewardsApi.getLifetimePoints(accountId = accountId, profileId = id).execute()

            if (response.isSuccessful) {
                val lifetimeStats = response.body() ?: throw EmptyBodyException(response)

                return LifetimeSmilesEntity(
                    profileId = id,
                    lifetimePoints = lifetimeStats.lifetimePoints
                )
            } else {
                throw errorResponseToApiError(response)
            }
        } ?: throw NoAccountException
    }

    private fun account(): AccountInternal? {
        return try {
            accountDataStore.getAccountMaybe().subscribeOn(Schedulers.io()).blockingGet()
        } catch (e: RuntimeException) {
            Timber.e(e)

            null
        }
    }
}
