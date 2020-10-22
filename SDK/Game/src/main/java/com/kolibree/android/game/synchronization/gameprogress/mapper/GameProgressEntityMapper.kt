/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress.mapper

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem

internal fun List<GameProgressEntity>.toSynchronizableItem(): ProfileGameProgressSynchronizableItem? =
    if (isNotEmpty()) {
        ProfileGameProgressSynchronizableItem(
            kolibreeId = first().profileId,
            gameProgress = map { it.toGameProgress() },
            updatedAt = TrustedClock.getNowZonedDateTimeUTC(),
            uuid = first().uuid
        )
    } else {
        null
    }
