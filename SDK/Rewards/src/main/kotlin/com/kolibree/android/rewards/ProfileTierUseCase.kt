/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository
import com.kolibree.android.rewards.persistence.ProfileTierOptional
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
class ProfileTierUseCase @Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    @ProfileProgress private val profileSmilesRepository: ProfileSmilesRepository
) {

    /**
     * Returns a Flowable that will emit the Tier optional for the active profile
     *
     * If the current profile changes or current profile's Tier changes, it'll emit a new value
     *
     * If we can't find a Tier for the active profile, we emit an empty Optional object.
     */
    fun currentProfileTier(): Flowable<ProfileTierOptional> {
        return currentProfileProvider.currentProfileFlowable()
            .switchMap { profile -> profileSmilesRepository.profileTier(profile.id) }
            .map { profileTierList ->
                ProfileTierOptional(if (profileTierList.isEmpty()) null else profileTierList.first())
            }
            .defaultIfEmpty(ProfileTierOptional())
    }
}
