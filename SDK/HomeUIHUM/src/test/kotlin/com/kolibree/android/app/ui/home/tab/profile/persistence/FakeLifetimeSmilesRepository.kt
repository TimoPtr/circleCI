/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.persistence

import com.google.common.base.Optional
import com.kolibree.android.rewards.models.LifetimeSmiles
import com.kolibree.android.rewards.persistence.LifetimeStatsRepository
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

internal class FakeLifetimeSmilesRepository : LifetimeStatsRepository {
    private val lifetimeSmilesEntities = mutableSetOf<LifetimeSmiles>()

    private val listUpdatedProcessor = PublishProcessor.create<Unit>()

    fun insertOrReplace(lifetimeSmilesEntity: LifetimeSmiles) {
        lifetimeSmilesEntities.removeIf { it.profileId == lifetimeSmilesEntity.profileId }
        lifetimeSmilesEntities.add(lifetimeSmilesEntity)

        listUpdatedProcessor.onNext(Unit)
    }

    override fun lifetimePoints(profileId: Long): Flowable<LifetimeSmiles> {
        return listUpdatedProcessor
            .startWith(Unit)
            .map { Optional.fromNullable(lifetimeSmilesEntities.firstOrNull { it.profileId == profileId }) }
            .filter { it.isPresent }
            .map { it.get() }
    }
}
