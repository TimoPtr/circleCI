package com.kolibree.sdkws.profile.utils

import com.google.gson.Gson
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.models.PictureResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

internal class MockProfileApi(
    private val delegate: BehaviorDelegate<ProfileApi>,
    private val profiles: ArrayList<ProfileInternal>
) : ProfileApi {

    override fun getPictureUploadUrl(accountId: Int, profileId: Long): Single<Response<ProfileInternal>> {
        return delegate.returningResponse(ResponseBody.create(null, "new_image_link"))
            .getPictureUploadUrl(accountId, profileId)
    }

    override fun confirmPictureUrl(
        accountId: Int,
        profileId: Long,
        pictureUrl: String
    ): Single<Response<PictureResponse>> {
        return delegate.returningResponse(Single.just(pictureUrl))
            .confirmPictureUrl(accountId, profileId, pictureUrl)
    }

    override fun updateProfile(
        accountId: Long,
        profileId: Long,
        profileInternal: ProfileInternal
    ): Single<Response<ProfileInternal>> {

        val profileToUpdate =
            profiles.firstOrNull { it.accountId == accountId.toInt() && it.id == profileId }
        profiles.remove(profileToUpdate)
        return delegate.returningResponse(profileInternal)
            .updateProfile(accountId, profileId, profileInternal)
    }

    override fun updateProfile(
        accountId: Long,
        profileId: Long,
        editProfileData: EditProfileData
    ): Single<Response<ResponseBody>> {

        val profileToUpdate =
            profiles.firstOrNull { it.accountId == accountId.toInt() && it.id == profileId }
        profiles.remove(profileToUpdate)

        return delegate.returningResponse(ResponseBody.create(null, Gson().toJson(editProfileData)))
            .updateProfile(accountId, profileId, editProfileData)
    }

    override fun getProfile(accountId: Long, profileId: Long): Single<Response<ResponseBody>> {
        val profile = profiles.firstOrNull { it.accountId == accountId.toInt() && it.id == profileId }
        return delegate.returningResponse(profile)
            .getProfile(accountId, profileId)
    }

    override fun getProfilePicture(
        accountId: Long,
        profileId: Long
    ): Single<Response<PictureResponse>> {
        val profile = profiles.firstOrNull { profileInternal -> profileInternal.id == profileId }
        return delegate.returningResponse(
            PictureResponse(
                profile?.pictureUrl ?: "",
                profile?.pictureLastModifier ?: ""
            )
        )
            .getProfilePicture(accountId, profileId)
    }

    override fun deleteProfile(accountId: Long, profileId: Long): Single<Response<ResponseBody>> {

        val toDelete =
            profiles.firstOrNull { it.accountId == accountId.toInt() && it.id == profileId }
        profiles.remove(toDelete)

        return delegate.returningResponse(ResponseBody.create(null, Gson().toJson(toDelete)))
            .deleteProfile(accountId, profileId)
    }

    override fun createProfile(
        accountId: Long,
        profileToCreate: CreateProfileData
    ): Single<Response<ProfileInternal>> {

        val profileToAdd = ProfileInternal(
            pictureUrl = profileToCreate.picturePath,
            firstName = profileToCreate.firstName,
            addressCountry = profileToCreate.country,
            gender = profileToCreate.gender,
            handedness = profileToCreate.handedness,
            birthday = profileToCreate.birthday,
            exactBirthday = true,
            accountId = accountId.toInt(),
            age = profileToCreate.age,
            brushingTime = 120,
            creationDate = "2018-02-02T10:00:00+0000",
            points = 0,
            id = accountId
        )

        profiles.add(profileToAdd)
        return delegate.returningResponse(profileToAdd)
            .createProfile(accountId, profileToCreate)
    }
}
