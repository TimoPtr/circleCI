package com.kolibree.sdkws.account

import com.google.gson.JsonObject
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.network.models.LogoutBody
import com.kolibree.sdkws.Constants.SERVICE_BASE_ACCOUNT_URL
import com.kolibree.sdkws.Constants.SERVICE_BASE_ACCOUNT_URL_V4
import com.kolibree.sdkws.account.models.BrushSyncReminderRequest
import com.kolibree.sdkws.account.models.BrushSyncReminderResponse
import com.kolibree.sdkws.account.models.EmailNewsletterSubscriptionData
import com.kolibree.sdkws.account.models.GoogleLoginRequestBody
import com.kolibree.sdkws.account.models.GoogleSignUpRequestBody
import com.kolibree.sdkws.account.models.PrivateAccessToken
import com.kolibree.sdkws.account.models.PutPhoneNumberRequestBody
import com.kolibree.sdkws.account.models.VerifyUniqueNumberRequest
import com.kolibree.sdkws.account.models.VerifyUniqueNumberResponse
import com.kolibree.sdkws.account.models.WeChatCode
import com.kolibree.sdkws.account.models.WeeklyDigestData
import com.kolibree.sdkws.api.response.VerificationTokenResponse
import com.kolibree.sdkws.data.request.BetaData
import com.kolibree.sdkws.data.request.CreateAccountData
import com.kolibree.sdkws.data.request.CreateEmailAccountData
import com.kolibree.sdkws.data.request.UpdateAccountV3Data
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface AccountApi {

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/")
    fun createAccount(@Body createAccountBody: CreateAccountData): Single<Response<AccountInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/")
    fun createAccountWithEmail(@Body body: CreateEmailAccountData): Single<Response<AccountInternal>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/")
    fun weeklyDigest(
        @Path("accountId") accountId: Long,
        @Body body: WeeklyDigestData
    ): Single<Response<ResponseBody>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/")
    fun emailNewsletterSubscription(
        @Path("accountId") accountId: Long,
        @Body body: EmailNewsletterSubscriptionData
    ): Single<Response<ResponseBody>>

    @DELETE("$SERVICE_BASE_ACCOUNT_URL/{accountId}/")
    fun deleteAccount(@Path("accountId") accountId: Long): Single<Response<Void>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/")
    fun updateAccount(
        @Path("accountId") accountId: Long,
        @Body updateAccountV3Data: UpdateAccountV3Data
    ): Single<Response<AccountInternal>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/{accountId}/privateAccessToken/")
    fun getPrivateAccessToken(
        @Path("accountId") accountId: Long
    ): Single<Response<PrivateAccessToken>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/")
    fun updateBetaAccount(
        @Path("accountId") accountId: Long,
        @Body updateAccountV3Data: BetaData
    ): Single<Response<AccountInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL/{accountId}/logout/")
    fun logout(
        @Path("accountId") accountId: Long,
        @Body refreshTokenBody: LogoutBody
    ): Single<Response<String>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/{accountId}/")
    fun getAccount(@Path("accountId") accountId: Long): Single<Response<AccountInternal>>

    @GET("/v2/accounts/get_my_data/")
    fun getMyData(): Single<Response<String>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/smscode/")
    fun sendSmsCode(@Query("phone_number") phoneNumber: String):
        Single<Response<VerificationTokenResponse>>

    @POST("$SERVICE_BASE_ACCOUNT_URL/account_with_phone_number/")
    fun createAccountBySms(@Body createAccountBody: CreateAccountData): Single<Response<AccountInternal>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/account_with_phone_number/")
    fun loginBySms(
        @Query("phone_number") phoneNumber: String,
        @Query("verification_code") code: String,
        @Query("verification_token") token: String,
        @Query("test") test: Boolean? = null, // only supported in staging
        @Query("duration") tokenExpirationInSeconds: Int? = null
    ): Single<Response<AccountInternal>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/ConnectWithWeChat/")
    fun loginWithWechat(
        @Query("code") code: String,
        @Query("duration") duration: Int? = null,
        @Query("test") test: Boolean? = null
    ): Single<Response<AccountInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/ConnectWithWeChat/")
    fun attemptLoginWithWechat(@Body code: WeChatCode): Single<Response<ResponseBody>>

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/ConnectWithWeChat/")
    fun registerWithWechat(@Body body: JsonObject): Single<Response<AccountInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/signUpWithGoogle/")
    fun registerWithGoogle(@Body body: GoogleSignUpRequestBody): Single<Response<AccountInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL_V4/loginWithGoogle/")
    fun loginByGoogle(@Body body: GoogleLoginRequestBody): Single<Response<AccountInternal>>

    @POST("$SERVICE_BASE_ACCOUNT_URL/account_no_password/")
    fun createEmailAccount(@Body body: CreateAccountData): Single<Response<AccountInternal>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/{accountId}/poSignedUrl/android/")
    fun get6POUrl(@Path("accountId") accountId: Long): Single<Response<SignedUrl6POResponse>>

    @GET("$SERVICE_BASE_ACCOUNT_URL/anonymous/poSignedUrl/android/")
    fun get6POUrl(): Single<Response<SignedUrl6POResponse>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/account_with_phone_number/")
    fun putPhoneNumber(
        @Path("accountId") accountId: Long,
        @Body body: PutPhoneNumberRequestBody
    ): Single<Response<AccountInternal>>

    @DELETE("$SERVICE_BASE_ACCOUNT_URL/{accountId}/account_with_phone_number/")
    fun removePhoneNumber(@Path("accountId") accountId: Long): Single<Response<Void>>

    @DELETE("$SERVICE_BASE_ACCOUNT_URL/{accountId}/ConnectWithWeChat/")
    fun removeWeChat(@Path("accountId") accountId: Long): Single<Response<Void>>

    @PUT("$SERVICE_BASE_ACCOUNT_URL/{accountId}/ConnectWithWeChat/")
    fun updateWeChat(
        @Path("accountId") accountId: Long,
        @Body body: WeChatCode
    ): Single<Response<AccountInternal>>

    @POST("/v4/accounts/phone-number-association/")
    fun checkPhoneNumberAssociation(@Body body: VerifyUniqueNumberRequest): Single<Response<VerifyUniqueNumberResponse>>

    @POST("/v4/accounts/{accountId}/profiles/{profileId}/brush_sync_reminder/")
    fun createBrushSyncReminder(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body body: BrushSyncReminderRequest
    ): Single<Response<ResponseBody>>

    @PUT("/v4/accounts/{accountId}/profiles/{profileId}/brush_sync_reminder/")
    fun setBrushSyncReminderEnabled(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body body: BrushSyncReminderRequest
    ): Single<Response<ResponseBody>>

    @GET("/v4/accounts/{accountId}/profiles/{profileId}/brush_sync_reminder/")
    fun getBrushSyncReminderEnabled(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Single<Response<BrushSyncReminderResponse>>
}
