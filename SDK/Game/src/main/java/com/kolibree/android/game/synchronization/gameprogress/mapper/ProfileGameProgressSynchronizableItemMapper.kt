/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress.mapper

import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem

internal fun ProfileGameProgressSynchronizableItem.toPersistentEntities(): List<GameProgressEntity> =
    gameProgress.map {
        GameProgressEntity(
            profileId = profileId,
            gameId = it.gameId,
            progress = it.progress,
            updateDate = it.updatedAt,
            uuid = uuid
        )
    }
