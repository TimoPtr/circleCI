package com.kolibree.account

import androidx.annotation.VisibleForTesting
import com.google.gson.JsonObject
import com.kolibree.account.eraser.UserSessionManager
import com.kolibree.account.logout.ForceLogoutReason
import com.kolibree.account.logout.ShouldLogoutUseCase
import com.kolibree.account.phone.PhoneNumberLink
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.profile.HANDEDNESS_LEFT
import com.kolibree.android.commons.profile.HANDEDNESS_RIGHT
import com.kolibree.android.utils.PhoneNumberChecker
import com.kolibree.sdkws.account.AccountManager
import com.kolibree.sdkws.account.models.PrivateAccessToken
import com.kolibree.sdkws.account.models.PutPhoneNumberRequestBody
import com.kolibree.sdkws.core.AccountOperations
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.core.OnUserLoggedInCallback
import com.kolibree.sdkws.core.SynchronizationScheduler
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Convert the private Profile object into the Public Profile object
 */
@AppScope
internal class AccountFacadeImpl
@Inject constructor(
    private val connector: InternalKolibreeConnector,
    private val accountDatastore: AccountDatastore,
    private val accountManager: AccountManager,
    private val phoneNumberVerifier: PhoneNumberChecker,
    private val onUserLoggedInCallback: OnUserLoggedInCallback,
    private val shouldLogoutUseCase: ShouldLogoutUseCase,
    private val synchronizationScheduler: SynchronizationScheduler,
    private val userSessionManager: UserSessionManager
) : AccountFacade,
    AccountManager by accountManager,
    AccountOperations by connector {

    override fun logout(): Completable =
        connector.logout().doOnTerminate { userSessionManager.reset() }

    override fun deleteAccount(): Completable {
        return connector.deleteAccount().doOnTerminate { userSessionManager.reset() }
    }

    override fun shouldLogout(): Single<ForceLogoutReason> =
        shouldLogoutUseCase
            .shouldLogoutStream
            .take(1)
            .singleOrError()

    /**
     * Login with we chat
     * @return non null [List] [IProfile] [Single] profiles fetched
     */
    override fun loginWithWechat(code: String): Single<List<IProfile>> =
        accountManager.legacyLoginWithWechat(code)
            .doOnSuccess { onSuccessfulWechatLogin() }
            .map { account ->
                account.internalProfiles.map { it.exportProfile() }
            }

    /**
     * Login with we chat
     * @return non null [Account] [Single] that will emit an Account if login was successful.
     *
     * it can return an error WeChatAccountNotRecognizedException if the account is not register
     * if it's the case call registerWithWechatWithToken with the token in the error
     */
    override fun attemptLoginWithWechat(code: String): Single<Account> =
        accountManager.internalAttemptLoginWithWechat(code)
            .storeAccount()
            .doOnSuccess { onSuccessfulWechatLogin() }
            .map { accountInternal -> accountInternal.toAccount() }

    @VisibleForTesting
    fun onSuccessfulWechatLogin() {
        onUserLoggedInCallback.onUserLoggedIn()

        synchronizationScheduler.syncNow()
    }

    @VisibleForTesting
    fun getAccountInternalSingle(): Single<AccountInternal> = accountDatastore.getAccountMaybe()
        .toSingle()
        .onErrorResumeNext {
            if (it is NoSuchElementException) {
                Single.error(NoAccountException)
            } else {
                Single.error(it)
            }
        }

    override fun getAccountSingle(): Single<Account> = getAccountInternalSingle()
        .map { it.toAccount() }

    override fun getAccountStream(): Flowable<Account> =
        accountDatastore.accountFlowable().map { it.toAccount() }

    /**
     * Get Account
     * @return null [Account] current Account
     */
    override fun getAccount(): Account? = currentAccount()?.toAccount()

    /**
     * register with we chat
     * @return non null [Account] [Single] profiles created
     */
    override fun registerWithWechat(code: String, profile: IProfile): Single<Account> =
        JsonObject().run {
            internalRegisterWechat(code, Profile.of(profile), this)
        }

    /**
     * register with we chat
     * @return non null [Account] [Single] profiles created
     */
    override fun registerWithWechatWithToken(
        code: String,
        token: String,
        profile: IProfile
    ): Single<Account> =
        JsonObject().run {
            addProperty("token", token)
            internalRegisterWechat(code, Profile.of(profile), this)
        }

    @VisibleForTesting
    fun internalRegisterWechat(
        code: String,
        profile: Profile,
        body: JsonObject
    ): Single<Account> {
        val handedness = if (profile.isRightHanded()) HANDEDNESS_RIGHT else HANDEDNESS_LEFT

        body.addProperty("code", code)
        body.addProperty("first_name", profile.firstName)
        body.addProperty("gender", profile.gender.serializedName)
        body.addProperty("birthday", DATE_FORMATTER.format(profile.birthday))
        body.addProperty("country", profile.country)
        body.addProperty("survey_handedness", handedness)
        body.addProperty("brushing_goal_time", profile.brushingGoalTime)

        profile.pictureUrl?.let {
            body.addProperty("picture", it)
        }

        return accountManager.registerWithWechat(body).storeAccount().map { it.toAccount() }
    }

    /**
     * Url to get an access token for account linking with external backend.
     * @return non null [PrivateAccessToken] [Single] Response object
     */
    override fun getPrivateAccessToken(): Single<PrivateAccessToken> = getAccountInternalSingle()
        .flatMap { account -> accountManager.getPrivateAccessToken(account.id) }

    override fun unlinkPhoneNumber(): Completable =
        getAccountInternalSingle()
            .flatMapCompletable { currentAccount ->
                accountManager.removePhoneNumber(currentAccount.id)
                    .andThen(persistPhoneNumber(currentAccount, null))
            }

    override fun verifyPhoneNumber(phoneNumber: String): Single<PhoneNumberLink> =
        checkPhoneNumber(phoneNumber)
            .andThen(accountManager.sendSmsCode(phoneNumber)
                .map { PhoneNumberLink(it.verificationToken, phoneNumber) })

    override fun linkPhoneNumber(link: PhoneNumberLink, verificationCode: Int): Completable =
        getAccountInternalSingle()
            .flatMapCompletable { currentAccount ->
                accountManager.setPhoneNumber(
                    currentAccount.id,
                    PutPhoneNumberRequestBody(
                        link.verificationToken, verificationCode, link.phoneNumber
                    )
                )
                    .andThen(persistPhoneNumber(currentAccount, link.phoneNumber))
            }

    @VisibleForTesting
    fun checkPhoneNumber(phoneNumber: String): Completable =
        if (phoneNumberVerifier.isValid(phoneNumber)) {
            Completable.complete()
        } else {
            Completable.error(
                IllegalArgumentException("Not a valid international phone number: $phoneNumber")
            )
        }

    @VisibleForTesting
    fun persistPhoneNumber(account: AccountInternal, phoneNumber: String?): Completable =
        Completable.create {
            account.phoneNumber = phoneNumber
            accountDatastore.updatePhoneNumber(account)
            it.onComplete()
        }

    override fun linkWeChat(code: String): Completable =
        getAccountInternalSingle()
            .flatMapCompletable { currentAccount ->
                accountManager.setWeChat(currentAccount.id, code)
                    .flatMapCompletable { persistWeChatData(it) }
            }

    override fun unlinkWeChat(): Completable =
        getAccountInternalSingle()
            .flatMapCompletable { currentAccount ->
                accountManager.removeWeChat(currentAccount.id)
                    .andThen(nullifyWeChatData(currentAccount))
                    .andThen(persistWeChatData(currentAccount))
            }

    @VisibleForTesting
    fun nullifyWeChatData(account: AccountInternal) =
        Completable.create {
            account.wcOpenId = null
            account.wcUnionId = null
            account.wcAccessToken = null
            account.wcRefreshToken = null
            account.wcExpiresIn = null
            account.wcScope = null
            it.onComplete()
        }

    @VisibleForTesting
    fun persistWeChatData(account: AccountInternal) =
        Completable.create {
            accountDatastore.updateWeChatData(account)
            it.onComplete()
        }

    private fun Single<AccountInternal>.storeAccount(): Single<AccountInternal> =
        map { account -> accountDatastore.setAccount(account); account }
}

@VisibleForTesting
internal fun AccountInternal.toAccount(): Account = Account(
    ownerProfileId = ownerProfileId,
    phoneNumber = phoneNumber,
    backendId = id,
    email = email,
    pubId = pubId,
    weChatData = extractWeChatData(this),
    profiles = internalProfiles.map { it.exportProfile() }
)

private fun extractWeChatData(account: AccountInternal): WeChatData? =
    if (account.wcOpenId == null) null else WeChatData(
        checkNotNull(account.wcOpenId) { "openID shouldn't be null" },
        checkNotNull(account.wcUnionId) { "unionID shouldn't be null" },
        checkNotNull(account.wcAccessToken) { "accessToken shouldn't be null" },
        checkNotNull(account.wcRefreshToken) { "refreshToken shouldn't be null" },
        checkNotNull(account.wcExpiresIn) { "expiresIn shouldn't be null" },
        checkNotNull(account.wcScope) { "scope shouldn't be null" }
    )
