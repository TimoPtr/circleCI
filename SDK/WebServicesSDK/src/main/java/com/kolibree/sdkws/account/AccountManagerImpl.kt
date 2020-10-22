package com.kolibree.sdkws.account

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode.INVALID_ACCESS_TOKEN
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.network.models.LogoutBody
import com.kolibree.android.network.toParsedResponseCompletable
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.sdkws.account.models.BrushSyncReminderRequest
import com.kolibree.sdkws.account.models.EmailNewsletterSubscriptionData
import com.kolibree.sdkws.account.models.GoogleLoginRequestBody
import com.kolibree.sdkws.account.models.GoogleSignUpRequestBody
import com.kolibree.sdkws.account.models.PhoneWeChatLinked
import com.kolibree.sdkws.account.models.PutPhoneNumberRequestBody
import com.kolibree.sdkws.account.models.VerifyUniqueNumberRequest
import com.kolibree.sdkws.account.models.WeChatCode
import com.kolibree.sdkws.account.models.WeeklyDigestData
import com.kolibree.sdkws.api.response.VerificationTokenResponse
import com.kolibree.sdkws.data.model.PhoneNumberData
import com.kolibree.sdkws.data.request.BetaData
import com.kolibree.sdkws.data.request.CreateAccountData
import com.kolibree.sdkws.data.request.CreateEmailAccountData
import com.kolibree.sdkws.data.request.UpdateAccountV3Data
import com.kolibree.sdkws.exception.WeChatAccountNotRecognizedException
import io.reactivex.Completable
import io.reactivex.Single
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import javax.inject.Inject
import retrofit2.Response

internal class AccountManagerImpl
@Inject constructor(
    private val context: Context,
    private val accountApi: AccountApi,
    private val accountDatastore: AccountDatastore,
    private val gson: Gson
) : InternalAccountManager {

    override fun updateBetaAccount(accountId: Long, data: BetaData) =
        accountApi
            .updateBetaAccount(accountId, data)
            .toParsedResponseSingle()

    override fun createAnonymousAccount(data: CreateAccountData) =
        accountApi
            .createAccount(data)
            .toParsedResponseSingle()

    override fun updateAccount(accountId: Long, data: UpdateAccountV3Data) =
        accountApi
            .updateAccount(accountId, data)
            .toParsedResponseSingle()

    override fun getPrivateAccessToken(accountId: Long) =
        accountApi
            .getPrivateAccessToken(accountId)
            .toParsedResponseSingle()

    override fun checkPhoneNumberAssociation(
        phoneNumber: String,
        verificationToken: String,
        verificationCode: String
    ): Single<PhoneWeChatLinked> =
        accountApi
            .checkPhoneNumberAssociation(
                VerifyUniqueNumberRequest(
                    phoneNumber,
                    verificationToken,
                    verificationCode
                )
            )
            .toParsedResponseSingle()
            .map { PhoneWeChatLinked(it.phoneLinked, it.wechatLinked) }

    override fun createAccount(email: String, appId: String): Completable =
        accountApi
            .createAccountWithEmail(CreateEmailAccountData(email, appId))
            .toParsedResponseCompletable()

    override fun deleteAccount(accountId: Long): Completable =
        accountApi
            .deleteAccount(accountId)
            .toParsedResponseCompletable()

    override fun weeklyDigest(accountId: Long, isEnabled: Boolean): Completable =
        accountApi
            .weeklyDigest(accountId, WeeklyDigestData(isEnabled))
            .toParsedResponseCompletable()

    override fun emailNewsletterSubscription(accountId: Long, subscribed: Boolean): Completable =
        accountApi.emailNewsletterSubscription(
            accountId,
            EmailNewsletterSubscriptionData(subscribed)
        ).toParsedResponseCompletable()
            .andThen {
                accountDatastore.updateNewsletterSubscription(subscribed)
                it.onComplete()
            }

    override fun isEmailNewsletterSubscriptionEnabled(): Single<Boolean> {
        return Single.fromCallable {
            val currentAccount = accountDatastore.getAccountMaybe().blockingGet()
            currentAccount?.emailNewsletterSubscription ?: false
        }
    }

    override fun logout(accountId: Long, refreshToken: String, accessToken: String): Completable =
        accountApi.logout(accountId, LogoutBody(refreshToken, accessToken))
            .flatMapCompletable {
                when (it.code()) {
                    HTTP_NO_CONTENT -> Completable.complete()
                    else -> logoutErrorToCompletable(it)
                }
            }

    override fun getMyData(): Completable = accountApi.getMyData()
        .flatMapCompletable {
            when (it.code()) {
                HTTP_NO_CONTENT -> Completable.complete()
                else -> getDataErrorToCompletable(it)
            }
        }

    private fun getDataErrorToCompletable(responseError: Response<String>): Completable =
        Completable.error(ApiError(responseError.errorBody()?.string()))

    /**
     * Returns a completed Completable if the access token is invalid, otherwise returns a
     * Completable.error
     *
     * We want to avoid blocking the user and never letting him exit the home screen, such as If the
     * access_token was invalidated but the app didn't receive the confirmation for whatever reason
     */
    private fun logoutErrorToCompletable(it: Response<String>): Completable {
        val apiError = ApiError(it.errorBody()?.string())

        if (apiError.internalErrorCode == INVALID_ACCESS_TOKEN)
            return Completable.complete()

        return Completable.error(apiError)
    }

    override fun getAccount(accountId: Long): Single<AccountInternal> =
        accountApi
            .getAccount(accountId)
            .toParsedResponseSingle()

    override fun legacyLoginWithWechat(code: String): Single<AccountInternal> =
        accountApi
            .loginWithWechat(code)
            .toParsedResponseSingle()

    override fun internalAttemptLoginWithWechat(code: String): Single<AccountInternal> =
        accountApi.attemptLoginWithWechat(WeChatCode(code)).map { response ->
            if (response.isSuccessful) {
                if (response.code() != RESPONSE_CODE_UNKNOWN_WECHAT_ACCOUNT) {
                    response.body()?.run {
                        gson.fromJson(string(), AccountInternal::class.java)
                    } ?: throw IllegalStateException("No Internal account in the response")
                } else {
                    val loginAttemptToken = response.body()?.run {
                        JsonParser().parse(string()).asJsonObject.get("token").asString
                    } ?: throw IllegalStateException("No wechat token in the response")
                    throw WeChatAccountNotRecognizedException(loginAttemptToken)
                }
            } else {
                throw errorResponseToApiError(response)
            }
        }

    override fun registerWithWechat(profile: JsonObject): Single<AccountInternal> =
        accountApi
            .registerWithWechat(profile)
            .toParsedResponseSingle()

    override fun removeWeChat(accountId: Long): Completable =
        accountApi.removeWeChat(accountId)
            .toParsedResponseCompletable()

    override fun setWeChat(accountId: Long, code: String): Single<AccountInternal> =
        accountApi.updateWeChat(accountId, WeChatCode(code))
            .toParsedResponseSingle()

    override fun sendSmsCode(phoneNumber: String): Single<VerificationTokenResponse> =
        accountApi.sendSmsCode(phoneNumber)
            .toParsedResponseSingle()

    override fun createAccountBySms(data: CreateAccountData) =
        accountApi
            .createAccountBySms(data)
            .toParsedResponseSingle()

    override fun loginBySms(data: PhoneNumberData) =
        accountApi
            .loginBySms(data.phoneNumber, data.verificationCode, data.verificationToken)
            .toParsedResponseSingle()

    override fun createEmailAccount(data: CreateAccountData): Single<AccountInternal> =
        accountApi.createEmailAccount(data)
            .toParsedResponseSingle()

    override fun registerWithGoogle(data: CreateAccountData): Single<AccountInternal> =
        accountApi.registerWithGoogle(
            GoogleSignUpRequestBody.createFrom(
                packageName = context.applicationContext.packageName,
                data = data
            )
        ).toParsedResponseSingle()

    override fun loginByGoogle(data: CreateAccountData): Single<AccountInternal> =
        accountApi.loginByGoogle(
            GoogleLoginRequestBody.createFrom(
                packageName = context.applicationContext.packageName,
                data = data
            )
        ).toParsedResponseSingle()

    override fun setPhoneNumber(accountId: Long, body: PutPhoneNumberRequestBody): Completable =
        accountApi.putPhoneNumber(accountId, body)
            .toParsedResponseCompletable()

    override fun removePhoneNumber(accountId: Long): Completable =
        accountApi.removePhoneNumber(accountId)
            .toParsedResponseCompletable()

    override fun setBrushSyncReminderEnabled(
        accountId: Long,
        profileId: Long,
        enabled: Boolean
    ): Completable {
        // First request may fail if resource is not available on BE
        // in such case we should try to create it with second request
        return accountApi
            .setBrushSyncReminderEnabled(
                accountId = accountId,
                profileId = profileId,
                body = BrushSyncReminderRequest(isActive = enabled)
            )
            .toParsedResponseCompletable()
            .onErrorResumeNext { createBrushSyncReminder(accountId, profileId, enabled) }
    }

    override fun getBrushSyncReminderEnabled(
        accountId: Long,
        profileId: Long
    ): Single<Boolean> {
        return accountApi
            .getBrushSyncReminderEnabled(
                accountId = accountId,
                profileId = profileId
            )
            .toParsedResponseSingle()
            .map { response -> response.isActive }
    }

    private fun createBrushSyncReminder(
        accountId: Long,
        profileId: Long,
        enabled: Boolean
    ): Completable {
        return accountApi
            .createBrushSyncReminder(
                accountId = accountId,
                profileId = profileId,
                body = BrushSyncReminderRequest(isActive = enabled)
            )
            .toParsedResponseCompletable()
    }

    companion object {

        @VisibleForTesting
        internal const val RESPONSE_CODE_UNKNOWN_WECHAT_ACCOUNT = 202
    }
}
