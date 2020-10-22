package com.kolibree.account

import androidx.annotation.Keep
import com.kolibree.account.logout.AccountDoesNotExist
import com.kolibree.account.logout.ForceLogoutReason
import com.kolibree.account.logout.RefreshTokenFailed
import com.kolibree.account.phone.PhoneNumberLink
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.sdkws.account.AccountManager
import com.kolibree.sdkws.account.models.PrivateAccessToken
import com.kolibree.sdkws.core.AccountOperations
import com.kolibree.sdkws.exception.WeChatAccountNotRecognizedException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Expose all operations related to Account
 */
@Keep
interface AccountFacade : AccountManager, AccountOperations {

    /**
     * Fetches from the backend the Url to get an access token for account linking with
     * external backend.
     *
     * Syntactic sugar for [AccountManager.getPrivateAccessToken]
     *
     * @return [Single] that will emit [PrivateAccessToken] response object
     *
     *  Emits [NoAccountException] if there's no [Account] logged in
     */
    fun getPrivateAccessToken(): Single<PrivateAccessToken>

    /**
     * Login with wechat
     * Code given by wechat
     * @return non null [List] [IProfile] [Single] profiles fetched
     */
    @Deprecated(
        "You should use attemptLoginWithWechat instead to deal with cache token",
        ReplaceWith("attemptLoginWithWechat")
    )
    fun loginWithWechat(code: String): Single<List<IProfile>>

    /**
     * Login with we chat
     *
     * The Single will emit a [WeChatAccountNotRecognizedException] if the account is not registered. If it's the case,
     * call registerWithWechatWithToken with the token in the error
     *
     * @return Single that will emit a [Account]
     */
    fun attemptLoginWithWechat(code: String): Single<Account>

    /**
     * register with wechat
     * Code given by wechat
     *
     * @return Single that will emit an [Account]
     */
    @Deprecated(
        "You should use the token given back by the back end instead of a code",
        ReplaceWith("registerWithWechatWithToken")
    )
    fun registerWithWechat(code: String, profile: IProfile): Single<Account>

    /**
     * register with wechat
     * Code given by wechat
     * Token given by backend when call attemptLoginWithWechat
     *
     * @return Single that will emit an [Account]
     */
    fun registerWithWechatWithToken(code: String, token: String, profile: IProfile): Single<Account>

    /**
     * @return Single that will emit the logged in [Account], or [NoAccountException]
     */
    fun getAccountSingle(): Single<Account>

    /**
     * @return Flowable that will emit the logged in [Account], or [NoAccountException]
     */
    fun getAccountStream(): Flowable<Account>

    /**
     * Return Account
     */
    fun getAccount(): Account?

    /**
     * Verify a phone number by sending a sms to the given number
     *
     * The returned [Single] will emit a [PhoneNumberLink] that should be passed as argument to the
     * linkPhoneNumber() method.
     *
     * @param phoneNumber [String] phone number
     * @return [PhoneNumberLink] [Single]
     */
    fun verifyPhoneNumber(phoneNumber: String): Single<PhoneNumberLink>

    /**
     * Link a verified phone number to the current account
     *
     * @param link [PhoneNumberLink] you got with verifyPhoneNumber()
     * @param verificationCode [Int] the code the user received by sms
     * @return [Completable]
     */
    fun linkPhoneNumber(link: PhoneNumberLink, verificationCode: Int): Completable

    /**
     * Use this method to remove the phone number associated with this account
     *
     * Note that you can't remove a phone number if this phone number is your only way to log into
     * the application. In order to remove a phone number you must be at least connected with
     * email, or WeChat.
     */
    fun unlinkPhoneNumber(): Completable

    /**
     * Link a WeChat account to the current account
     *
     * @param code [String] WeChat code (available in the WeChat app's account info)
     * @return [Completable]
     */
    fun linkWeChat(code: String): Completable

    /**
     * Unlink the WeChat account that is linked to the current account
     *
     * @return [Completable]
     */
    fun unlinkWeChat(): Completable

    /**
     * Stream that will emit a [ForceLogoutReason] when an unrecoverable error was detected
     *
     * After a value has been emitted, the SDK will wipe all local user storage.
     *
     * Failing to force the user to log in will result in unexpected behaviour
     *
     * Currently, there are two unrecoverable scenarios
     * - [AccountDoesNotExist]: backend reports that the account no longer exists
     * - [RefreshTokenFailed]: we can't refresh the access token. User should log in again
     *
     * @return Single<[ForceLogoutReason]> that will emit a [ForceLogoutReason] whenever an
     * unrecoverable error is detected
     */
    fun shouldLogout(): Single<ForceLogoutReason>
}
