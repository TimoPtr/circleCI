package com.kolibree.sdkws.sms

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.sdkws.sms.data.AccountData
import io.reactivex.Single

@Keep
interface SmsAccountManager {

    /**
     * This method informs backend to send SMS with the code to given phone number
     * @param phoneNumber the number where SMS with the code will be send
     * @return [Single] with object [SmsToken]
     */
    fun sendSmsCodeTo(phoneNumber: String): Single<SmsToken>

    /**
     * This method will create new account with [AccountData] data
     * @param smsToken, object returned by method [sendSmsCodeTo]
     * @param smsCode code which has been sent to your phone by SMS
     * @param data detailed data about account
     * @return [Single] with list of profiles [IProfile]
     */
    fun createAccount(
        smsToken: SmsToken,
        smsCode: String,
        data: AccountData
    ): Single<List<IProfile>>

    /**
     * Login to already existing account
     *
     * This method will emit a E04 error if the phone number is not associated to any account
     *
     * @param smsToken, object returned by method [sendSmsCodeTo]
     * @param code code which has been sent to your phone by SMS
     * @return [Single] with list of profiles [IProfile]
     */
    fun loginToAccount(smsToken: SmsToken, code: String): Single<List<IProfile>>
}
