package com.kolibree.sdkws.profile

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.models.PictureResponse
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Intended for internal use
 *
 * SDK clients should use ProfileFacade
 */
@VisibleForApp
interface ProfileManager {

    fun updateProfile(accountId: Long, profile: Profile): Single<Profile>
    fun createProfile(data: CreateProfileData, accountId: Long): Single<ProfileInternal>
    fun deleteProfile(profile: ProfileInternal): Single<Boolean>
    fun deleteProfileLocally(profileId: Long): Completable
    fun editProfile(data: EditProfileData, profile: ProfileInternal): Single<ProfileInternal>
    fun changeProfilePicture(
        profile: ProfileInternal,
        picturePath: String?
    ): Single<ProfileInternal>

    fun updateOrInsertProfileLocally(profileInternal: ProfileInternal): Single<ProfileInternal>
    fun downloadExternalPicture(
        profile: ProfileInternal,
        picturePath: String
    ): Single<ProfileInternal>

    fun avatarCache(profile: ProfileInternal)
    fun deleteProfile(accountId: Long, profileId: Long): Single<Boolean>

    /**
     * @return [Single]<[ProfileInternal]> emitting [ProfileInternal] or [NoSuchElementException]
     */
    fun getProfileInternalLocally(profileId: Long): Single<ProfileInternal>
    fun getProfileLocally(profileId: Long): Single<Profile>
    fun getProfilesLocally(): Single<List<Profile>>

    fun getProfilePicture(accountId: Long, profileId: Long): Single<PictureResponse>
}

internal interface ProfileSyncableFields {
    var firstName: String?

    var gender: String?

    var age: Int

    var brushingTime: Int

    var brushingNumber: Int

    var handedness: String?

    var countryCode: String?
}
