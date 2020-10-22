/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask

import com.kolibree.android.game.shorttask.data.api.ShortTaskApi
import com.kolibree.android.game.shorttask.data.api.model.ShortTaskRequest
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.synchronizator.SynchronizableItemApi
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

internal class ShortTaskSynchronizableApi @Inject constructor(
    private val shortTaskApi: ShortTaskApi,
    private val kolibreeConnector: IKolibreeConnector
) : SynchronizableItemApi {

    override fun get(kolibreeId: Long): SynchronizableItem {
        throw IllegalAccessException("Short task does not support retrieving of an item")
    }

    // This will always create a new item
    override fun createOrEdit(synchronizable: SynchronizableItem): SynchronizableItem =
        when (synchronizable) {
            is ShortTaskSynchronizableItem -> createInternal(synchronizable)
            else -> throw IllegalArgumentException("Cannot create/edit $synchronizable")
        }

    private fun createInternal(item: ShortTaskSynchronizableItem): SynchronizableItem {
        val accountId = kolibreeConnector.accountId

        val response = shortTaskApi.createShortTask(
            accountId,
            item.profileId,
            ShortTaskRequest(item.shortTask.internalValue, item.createdAt.toOffsetDateTime())
        ).execute()

        if (response.isSuccessful) {
            return item
        } else {
            throw errorResponseToApiError(response)
        }
    }
}
