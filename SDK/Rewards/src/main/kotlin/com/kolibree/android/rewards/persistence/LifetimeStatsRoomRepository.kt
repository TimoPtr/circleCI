/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.persistence

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.models.LifetimeSmiles
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
interface LifetimeStatsRepository {
    fun lifetimePoints(profileId: Long): Flowable<LifetimeSmiles>
}

internal class LifetimeStatsRoomRepository @Inject constructor(
    private val lifetimeSmilesDao: LifetimeSmilesDao
) : LifetimeStatsRepository {
    override fun lifetimePoints(profileId: Long): Flowable<LifetimeSmiles> =
        lifetimeSmilesDao.readByProfileStream(profileId).map { it }
}
