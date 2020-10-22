package com.kolibree.sdkws.profile.persistence.repo

import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.models.PictureResponse
import io.reactivex.Completable
import io.reactivex.Single

internal interface ProfileRepository {

    fun createProfile(data: CreateProfileData, accountId: Long): Single<ProfileInternal>
    fun deleteProfile(accountId: Long, profileId: Long): Single<Boolean>
    fun deleteProfileLocally(profileId: Long): Completable

    fun updateProfile(accountId: Long, profileInternal: ProfileInternal): Single<ProfileInternal>
    fun updateProfileLocally(profileInternal: ProfileInternal): Single<ProfileInternal>
    fun insertProfileLocally(profileInternal: ProfileInternal): Single<ProfileInternal>
    fun updateProfile(
        accountId: Long,
        profileId: Long,
        editProfileData: EditProfileData
    ): Single<Boolean>

    fun getProfilePicture(accountId: Long, profileId: Long): Single<PictureResponse>

    fun getPictureUploadUrl(accountId: Int, profileId: Long): Single<ProfileInternal>
    fun confirmPictureUrl(accountId: Int, profileId: Long, pictureUrl: String): Single<PictureResponse>

    /**
     * @return [Single]<[ProfileInternal]> emitting [ProfileInternal] or [NoSuchElementException]
     */
    fun getProfileLocally(profileId: Long): Single<ProfileInternal>
    fun getProfilesLocally(): Single<List<ProfileInternal>>
}
