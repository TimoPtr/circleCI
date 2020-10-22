package com.kolibree.sdkws.profile

import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.sdkws.Constants.SERVICE_BASE_ACCOUNT_URL
import com.kolibree.sdkws.Constants.SERVICE_BASE_ACCOUNT_URL_V4
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.models.PictureResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface ProfileApi {

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/")
    fun updateProfile(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body profileInternal: ProfileInternal
    ): Single<Response<ProfileInternal>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/")
    fun updateProfile(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body editProfileData: EditProfileData
    ): Single<Response<ResponseBody>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/")
    fun getProfile(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Single<Response<ResponseBody>>

    // Update endpoint when the dedicated one is ready
    // https://kolibree.atlassian.net/browse/KLTB002-11553
    @GET("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/")
    fun getProfilePicture(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Single<Response<PictureResponse>>

    @DELETE("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/")
    fun deleteProfile(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Single<Response<ResponseBody>>

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/{accountId}/profiles/")
    fun createProfile(
        @Path("accountId") accountId: Long,
        @Body profileToCreate: @JvmSuppressWildcards CreateProfileData
    ): Single<Response<ProfileInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/picture/")
    fun getPictureUploadUrl(
        @Path("accountId") accountId: Int,
        @Path("profileId") profileId: Long
    ): Single<Response<ProfileInternal>>

    @FormUrlEncoded
    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/profiles/{profileId}/picture/")
    fun confirmPictureUrl(
        @Path("accountId") accountId: Int,
        @Path("profileId") profileId: Long,
        @Field("picture_get_url") pictureUrl: String
    ): Single<Response<PictureResponse>>
}
