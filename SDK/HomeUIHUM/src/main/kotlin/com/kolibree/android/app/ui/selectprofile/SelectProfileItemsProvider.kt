/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import io.reactivex.Single
import javax.inject.Inject

internal class SelectProfileItemsProvider @Inject constructor(
    private val profileFacade: ProfileFacade
) {
    fun selectProfileItems(): Single<List<SelectProfileItem>> {
        return profileFacade.getProfilesList()
            .mapToSelectProfileBindingItems()
            .sortProfiles()
            .attachAddProfile()
    }

    private fun Single<List<IProfile>>.mapToSelectProfileBindingItems(): Single<List<ProfileItem>> {
        return map { profiles -> profiles.map(::toSelectProfileItem) }
    }

    private fun toSelectProfileItem(profile: IProfile): ProfileItem {
        return ProfileItem(
            profileId = profile.id,
            profileName = profile.firstName,
            profileAvatarUrl = profile.pictureUrl,
            creationDate = Profile.of(profile).getCreationDate(),
            isSelected = profileFacade.currentProfile == profile
        )
    }

    private fun Single<List<ProfileItem>>.sortProfiles(): Single<List<ProfileItem>> {
        return map { it.sortedWith(profileComparator) }
    }

    private fun Single<List<ProfileItem>>.attachAddProfile(): Single<List<SelectProfileItem>> {
        return map { profiles ->
            when {
                profiles.size < MAX_PROFILES -> profiles + AddProfileItem()
                else -> profiles
            }
        }
    }

    private val profileComparator = Comparator<ProfileItem> { first, second ->
        when {
            first.isSelected -> -1
            second.isSelected -> 1
            else -> first.creationDate.compareTo(second.creationDate)
        }
    }
}

private const val MAX_PROFILES = 4
