/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.rewards.persistence.LifetimeStatsRepository
import io.reactivex.Flowable
import javax.inject.Inject

/**
 * UseCase that will report lifetime points for active profile
 */
internal class LifetimeSmilesUseCase @Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val lifetimeStatsRepository: LifetimeStatsRepository
) {
    fun lifetimePoints(): Flowable<Int> {
        return currentProfileProvider.currentProfileFlowable()
            .switchMap { profile ->
                lifetimeStatsRepository.lifetimePoints(profile.id).map { it.lifetimePoints }
            }
    }
}
