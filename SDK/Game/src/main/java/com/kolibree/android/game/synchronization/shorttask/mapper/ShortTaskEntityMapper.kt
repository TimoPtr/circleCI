/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask.mapper

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.shorttask.data.persistence.model.ShortTaskEntity
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem

internal fun ShortTaskEntity.toSynchronizableItem(): ShortTaskSynchronizableItem =
    ShortTaskSynchronizableItem(
        shortTask,
        profileId,
        creationDateTime.toZonedDateTime(),
        TrustedClock.getNowZonedDateTime(),
        uuid
    )
