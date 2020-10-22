/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.kolibree.charts.inoff.data.api.InOffBrushingsCountApi
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class InOffBrushingsCountSynchronizableReadOnlyApi @Inject constructor(
    private val api: InOffBrushingsCountApi,
    private val accountDatastore: AccountDatastore
) :
    SynchronizableReadOnlyApi {

    override fun get(id: Long): SynchronizableReadOnly {
        account()?.id?.let { accountId ->

            val response =
                api.getInOffBrushingsCount(accountId = accountId, profileId = id).execute()

            if (response.isSuccessful) {
                val inOffBrushingCount = response.body() ?: throw EmptyBodyException(response)
                return InOffBrushingsCountEntity(
                    id,
                    inOffBrushingCount.offTotal,
                    inOffBrushingCount.inTotal
                )
            } else {
                throw errorResponseToApiError(response)
            }
        } ?: throw NoAccountException
    }

    private fun account(): AccountInternal? = try {
        accountDatastore.getAccountMaybe().subscribeOn(Schedulers.io()).blockingGet()
    } catch (e: RuntimeException) {
        Timber.e(e)

        null
    }
}
