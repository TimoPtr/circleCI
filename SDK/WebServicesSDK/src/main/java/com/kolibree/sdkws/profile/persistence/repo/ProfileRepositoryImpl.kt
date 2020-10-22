package com.kolibree.sdkws.profile.persistence.repo

import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.sdkws.api.ConnectivityApiManager
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.models.PictureResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class ProfileRepositoryImpl @Inject
constructor(
    private val profileDatastore: ProfileDatastore,
    private val connectivityApiManager: ConnectivityApiManager,
    private val profileApi: ProfileApi
) : ProfileRepository {
    override fun getProfilesLocally() = profileDatastore.getProfiles()

    override fun deleteProfile(accountId: Long, profileId: Long): Single<Boolean> {
        return when {
            !connectivityApiManager.hasConnectivity() -> connectivityApiManager.syncWhenConnectivityAvailable()
            else -> profileApi.deleteProfile(accountId, profileId)
                .toParsedResponseSingle()
                .subscribeOn(Schedulers.io())
                .flatMap {
                    profileDatastore.deleteProfile(profileId) // don't use it.id
                        .andThen(Single.just(true))
                }
        }
    }

    /**
     * Updates profile remotely. If it succeeds, it updates it locally
     *
     * @return Single emitting true on success, false otherwise
     */
    override fun updateProfile(
        accountId: Long,
        profileInternal: ProfileInternal
    ): Single<ProfileInternal> {
        validateBrushingTime(profileInternal)
        return when {
            !connectivityApiManager.hasConnectivity() -> connectivityApiManager.syncWhenConnectivityAvailable()
            else -> profileApi.updateProfile(accountId, profileInternal.id, profileInternal)
                .toParsedResponseSingle()
                .subscribeOn(Schedulers.io())
                .flatMap(this::updateProfileLocally)
        }
    }

    /**
     * Updates profile remotely. Does not update local persistence
     *
     * @return Single emitting true on success, false otherwise
     */
    override fun updateProfile(
        accountId: Long,
        profileId: Long,
        editProfileData: EditProfileData
    ): Single<Boolean> {
        validateBrushingTime(editProfileData)
        return when {
            !connectivityApiManager.hasConnectivity() -> connectivityApiManager.syncWhenConnectivityAvailable()
            else -> profileApi.updateProfile(accountId, profileId, editProfileData)
                .toParsedResponseSingle()
                .subscribeOn(Schedulers.io())
                .map { true }
        }
    }

    override fun getProfilePicture(accountId: Long, profileId: Long): Single<PictureResponse> {
        return profileApi.getProfilePicture(accountId, profileId)
            .toParsedResponseSingle()
            .subscribeOn(Schedulers.io())
    }

    override fun getPictureUploadUrl(accountId: Int, profileId: Long): Single<ProfileInternal> =
        profileApi
            .getPictureUploadUrl(accountId, profileId)
            .toParsedResponseSingle()
            .subscribeOn(Schedulers.io())

    override fun confirmPictureUrl(
        accountId: Int,
        profileId: Long,
        pictureUrl: String
    ): Single<PictureResponse> {
        return profileApi.confirmPictureUrl(accountId, profileId, pictureUrl)
            .toParsedResponseSingle()
            .subscribeOn(Schedulers.io())
    }

    /**
     * Create new profile for current account
     *
     * @param data Profile data
     */
    override fun createProfile(data: CreateProfileData, accountId: Long): Single<ProfileInternal> {
        return when {
            !connectivityApiManager.hasConnectivity() -> connectivityApiManager.syncWhenConnectivityAvailable()
            else -> profileApi.createProfile(accountId, data)
                .toParsedResponseSingle()
                .subscribeOn(Schedulers.io())
                .map { profile -> validateBrushingTime(profile) }
                .map { profile ->
                    val newProfile = profile.copy(transitionSounds = true)
                    profileDatastore.addProfile(newProfile)
                    newProfile
                }
        }
    }

    override fun getProfileLocally(profileId: Long): Single<ProfileInternal> {
        return profileDatastore.getProfile(profileId)
            .subscribeOn(Schedulers.io())
    }

    override fun deleteProfileLocally(profileId: Long) = profileDatastore.deleteProfile(profileId)

    override fun updateProfileLocally(profileInternal: ProfileInternal): Single<ProfileInternal> {
        return profileDatastore.updateProfile(profileInternal)
            .toSingleDefault(profileInternal)
    }

    override fun insertProfileLocally(profileInternal: ProfileInternal): Single<ProfileInternal> {
        return profileDatastore.getProfile(profileDatastore.addProfile(profileInternal))
    }

    private fun validateBrushingTime(profile: ProfileInternal): ProfileInternal {
        FailEarly.failInConditionMet(
            profile.brushingTime == -1,
            message = "Profile has brushingTime! == -1"
        ) {
            profile.brushingTime = DEFAULT_BRUSHING_GOAL
        }
        return profile
    }

    private fun validateBrushingTime(editProfileData: EditProfileData): EditProfileData {
        FailEarly.failInConditionMet(
            editProfileData.brushingTime == -1,
            message = "Profile has brushingTime! == -1"
        ) {
            editProfileData.brushingTime = DEFAULT_BRUSHING_GOAL
        }
        return editProfileData
    }
}
