/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository
import com.kolibree.sdkws.profile.ProfileManager
import io.reactivex.Flowable
import javax.inject.Inject
import kotlinx.android.parcel.Parcelize

@Keep
class ProfileWithSmilesUseCase
@Inject constructor(
    private val profileManager: ProfileManager,
    private val currentProfileProvider: CurrentProfileProvider,
    @ProfileProgress private val rewardsRepository: ProfileSmilesRepository
) {
    /**
     * Returns a Flowable that will emit the ProfileWithSmiles for the active profile
     *
     * If for whatever reason we don't have smile info about the active profile, we will emit a ProfileWithSmiles with
     * value 0
     */
    fun currentProfileWithSmilesStream(): Flowable<ProfileWithSmiles> = currentProfileProvider.currentProfileFlowable()
        .distinctUntilChanged()
        .switchMap { profile ->
            mapProfileAndProgress(profile)
        }

    /**
     * Return a stream of a given profile of ProfileWithSmiles if no smile info retrieve it will be replace by 0
     */
    fun getProfileWithSmilesStream(profileId: Long): Flowable<ProfileWithSmiles> =
        profileManager.getProfileLocally(profileId).flatMapPublisher { profile ->
            mapProfileAndProgress(profile)
        }

    /**
     * Return a list of the other profile (different than currentProfile) with smiles. It will emit the whole
     * list each time a profile is updated
     */
    fun retrieveOtherProfilesSmilesStream(currentProfile: Profile): Flowable<List<ProfileWithSmiles>> =
        profileManager.getProfilesLocally()
            .flatMapPublisher { profiles ->
                val otherProfiles = profiles.filter { profile -> profile.id != currentProfile.id }
                    .map { profile -> mapProfileAndProgress(profile) }.toTypedArray()

                Flowable.combineLatest<ProfileWithSmiles, List<ProfileWithSmiles>>(otherProfiles) { array ->
                    array.map { it as ProfileWithSmiles }
                }
            }

    private fun mapProfileAndProgress(profile: Profile): Flowable<ProfileWithSmiles> =
        rewardsRepository.profileProgress(profile.id)
            .map { profileSmilesList ->
                val smiles = profileSmilesList.firstOrNull()?.smiles ?: 0

                ProfileWithSmiles(profile, smiles)
            }
}

@Parcelize
@Keep
data class ProfileWithSmiles(private val profile: Profile, val smiles: Int) : IProfile by profile, Parcelable
