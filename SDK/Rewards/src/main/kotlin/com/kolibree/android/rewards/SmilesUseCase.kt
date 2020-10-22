/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import androidx.annotation.Keep
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository
import io.reactivex.Flowable
import javax.inject.Inject

@Keep
class SmilesUseCase @Inject constructor(
    @ProfileProgress private val rewardsRepository: ProfileSmilesRepository
) {

    /**
     * Stream the current amount of smiles available
     */
    fun smilesAmountStream(): Flowable<Int> =
        rewardsRepository.profileProgress().map { it.sumBy { profileSmiles -> profileSmiles.smiles } }
}
