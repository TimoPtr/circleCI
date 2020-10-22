/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import android.os.Parcelable
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.sdkws.profile.ProfileManager
import io.reactivex.Flowable
import javax.inject.Inject
import kotlinx.android.parcel.Parcelize

@VisibleForApp
class ProfileWithSmilesAndTierRankUseCase
@Inject constructor(
    private val profileManager: ProfileManager,
    private val rewardsRepository: RewardsRepository
) {

    /**
     * Return a list of the other profile (different than currentProfile) with smiles and rank. It will emit the whole
     * list each time a profile is updated (rank or smiles)
     */
    fun retrieveOtherProfilesSmilesStream(currentProfile: IProfile): Flowable<List<ProfileWithSmilesAndTierRank>> =
        profileManager.getProfilesLocally()
            .flatMapPublisher { profiles ->
                val otherProfiles = profiles.filter { profile -> profile.id != currentProfile.id }
                    .map { profile -> mapProfileAndProgress(profile) }.toTypedArray()

                Flowable.combineLatest<
                    ProfileWithSmilesAndTierRank,
                    List<ProfileWithSmilesAndTierRank>>(otherProfiles) { array ->
                    array.map { it as ProfileWithSmilesAndTierRank }
                }
            }

    private fun mapProfileAndProgress(profile: Profile): Flowable<ProfileWithSmilesAndTierRank> =
        rewardsRepository.profileTier(profile.id).switchMap { profileTiers ->
            val rank = profileTiers.firstOrNull()?.rank
            rewardsRepository.profileProgress(profile.id)
                .map { profileSmilesList ->
                    val smiles = profileSmilesList.firstOrNull()?.smiles ?: 0

                    ProfileWithSmilesAndTierRank(profile, smiles, rank)
                }
        }
}

@Parcelize
@VisibleForApp
data class ProfileWithSmilesAndTierRank(
    private val profile: Profile,
    val smiles: Int,
    val tierRank: String?
) : IProfile by profile, Parcelable
