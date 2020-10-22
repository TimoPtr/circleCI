package com.kolibree.sdkws.account

import androidx.annotation.Keep
import com.google.gson.JsonObject
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.sdkws.account.models.PhoneWeChatLinked
import com.kolibree.sdkws.account.models.PrivateAccessToken
import com.kolibree.sdkws.account.models.PutPhoneNumberRequestBody
import com.kolibree.sdkws.api.response.VerificationTokenResponse
import com.kolibree.sdkws.data.model.PhoneNumberData
import com.kolibree.sdkws.data.request.BetaData
import com.kolibree.sdkws.data.request.CreateAccountData
import com.kolibree.sdkws.data.request.UpdateAccountV3Data
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Intended for internal use
 *
 * SDK clients should use AccountFacade
 */
@Keep
@Suppress("TooManyFunctions")
interface AccountManager {
    fun getAccount(accountId: Long): Single<AccountInternal>

    @Deprecated(
        "Use AccountFacade",
        ReplaceWith("attemptLoginWithWechat")
    )
    fun legacyLoginWithWechat(code: String): Single<AccountInternal>

    @Deprecated("Use AccountFacade")
    fun internalAttemptLoginWithWechat(code: String): Single<AccountInternal>

    @Deprecated("Use AccountFacade")
    fun registerWithWechat(profile: JsonObject): Single<AccountInternal>

    @Deprecated(
        "Use AccountFacade",
        ReplaceWith("unlinkPhoneNumber")
    )
    fun removePhoneNumber(accountId: Long): Completable

    fun setPhoneNumber(accountId: Long, body: PutPhoneNumberRequestBody): Completable

    fun sendSmsCode(phoneNumber: String): Single<VerificationTokenResponse>

    fun removeWeChat(accountId: Long): Completable

    fun setWeChat(accountId: Long, code: String): Single<AccountInternal>

    fun getPrivateAccessToken(accountId: Long): Single<PrivateAccessToken>

    fun checkPhoneNumberAssociation(
        phoneNumber: String,
        verificationToken: String,
        verificationCode: String
    ): Single<PhoneWeChatLinked>

    fun emailNewsletterSubscription(accountId: Long, subscribed: Boolean): Completable

    fun isEmailNewsletterSubscriptionEnabled(): Single<Boolean>

    fun setBrushSyncReminderEnabled(
        accountId: Long,
        profileId: Long,
        enabled: Boolean
    ): Completable

    fun getBrushSyncReminderEnabled(
        accountId: Long,
        profileId: Long
    ): Single<Boolean>
}

internal interface InternalAccountManager : AccountManager {
    fun createAnonymousAccount(data: CreateAccountData): Single<AccountInternal>

    fun weeklyDigest(accountId: Long, isEnabled: Boolean): Completable

    fun updateAccount(accountId: Long, data: UpdateAccountV3Data): Single<AccountInternal>

    fun updateBetaAccount(accountId: Long, data: BetaData): Single<AccountInternal>

    fun logout(accountId: Long, refreshToken: String, accessToken: String): Completable

    fun getMyData(): Completable

    fun createAccountBySms(data: CreateAccountData): Single<AccountInternal>

    fun loginBySms(data: PhoneNumberData): Single<AccountInternal>

    fun createAccount(email: String, appId: String): Completable

    fun deleteAccount(accountId: Long): Completable

    fun createEmailAccount(data: CreateAccountData): Single<AccountInternal>

    fun registerWithGoogle(data: CreateAccountData): Single<AccountInternal>

    fun loginByGoogle(data: CreateAccountData): Single<AccountInternal>
}
