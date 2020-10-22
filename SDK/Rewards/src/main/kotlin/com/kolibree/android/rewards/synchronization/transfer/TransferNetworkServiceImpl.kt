/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.transfer

import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.rewards.models.Transfer
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import javax.inject.Inject

internal class TransferNetworkServiceImpl @Inject constructor(private val rewardsApi: RewardsApi) :
    TransferNetworkService {

    override fun transferSmiles(transferData: TransferData): Transfer {
        val response = rewardsApi.transferSmiles(transferData).execute()

        when {
            response.isSuccessful -> {
                val transferApi = response.body() ?: throw EmptyBodyException(response)

                return Transfer(transferApi.smiles, transferApi.fromProfileId, transferApi.toProfileId)
            }
            response.code() == 500 -> {
                throw TransferHttpException(response)
            }
            else -> throw errorResponseToApiError(response)
        }
    }
}
