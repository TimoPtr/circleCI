/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.redeem

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.models.Redeem
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import javax.inject.Inject

internal class RedeemNetworkServiceImpl
@Inject constructor(private val rewardsApi: RewardsApi) : RedeemNetworkService {

    override fun claimRedeem(redeemData: RedeemData): Redeem {
        val response = rewardsApi.claimRedeem(redeemData).execute()

        when {
            response.isSuccessful -> {
                val redeemApi = response.body() ?: throw EmptyBodyException(response)

                return Redeem(redeemApi.redeemUrl, redeemApi.result, redeemApi.rewardsId)
            }
            response.code() == 500 -> {
                throw RedeemHttpException(response)
            }
            else -> throw errorResponseToApiError(response)
        }
    }
}
