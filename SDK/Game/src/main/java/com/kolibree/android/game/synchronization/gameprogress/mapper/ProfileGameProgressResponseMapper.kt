/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress.mapper

import com.kolibree.android.game.gameprogress.data.api.model.ProfileGameProgressResponse
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItem

internal fun ProfileGameProgressResponse.toSynchronizableItem(): SynchronizableItem =
    ProfileGameProgressSynchronizableItem(
        kolibreeId = profileId,
        gameProgress = gamesProgress.map { it.toDomainGameProgress() }
    )
