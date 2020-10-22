/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import androidx.annotation.VisibleForTesting
import com.kolibree.android.game.gameprogress.data.api.GameProgressApi
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressRequest
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressResponse
import com.kolibree.android.game.synchronization.gameprogress.mapper.toSynchronizableItem
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.synchronizator.SynchronizableItemApi
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

internal class GameProgressSynchronizableApi @Inject constructor(
    private val gameProgressApi: GameProgressApi,
    private val kolibreeConnector: IKolibreeConnector
) : SynchronizableItemApi {

    override fun get(kolibreeId: Long): SynchronizableItem {
        val response = gameProgressApi.getProfileGameProgress(kolibreeConnector.accountId, kolibreeId).blockingGet()

        if (response.isSuccessful) {
            val profileGameProgressResponse = response.body() ?: throw EmptyBodyException(response)
            return profileGameProgressResponse.toSynchronizableItem()
        } else {
            throw errorResponseToApiError(response)
        }
    }

    override fun createOrEdit(synchronizable: SynchronizableItem): SynchronizableItem =
        when (synchronizable) {
            is ProfileGameProgressSynchronizableItem -> createOrEditInternal(synchronizable)
            else -> throw IllegalArgumentException("Cannot create/edit $synchronizable")
        }

    @VisibleForTesting
    fun createOrEditInternal(item: ProfileGameProgressSynchronizableItem): SynchronizableItem {
        val accoutId = kolibreeConnector.accountId
        val gameProgressResponses = item.gameProgress.map {
            gameProgressApi.setGameProgress(accoutId, item.profileId, it.gameId, GameProgressRequest(it.progress))
                .toParsedResponseSingle()
                .map(GameProgressResponse::toDomainGameProgress)
                .blockingGet()
        }

        return ProfileGameProgressSynchronizableItem(item.profileId, gameProgressResponses)
    }
}
