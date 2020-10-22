/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.sdkws.data.model.FacebookLoginData
import com.kolibree.sdkws.data.model.LoginData
import com.kolibree.sdkws.data.request.BetaData
import com.kolibree.sdkws.data.request.CreateAccountData
import com.kolibree.sdkws.data.request.UpdateAccountV3Data
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.LocalDate

/**
 * To be used internally by SDK developers. Not intended for clients
 */
@Keep
interface AccountOperations {

    fun currentAccount(): AccountInternal?

    /**
     * Create an account on the backend
     *
     * @param data non null [CreateAccountData]
     * @return non null [Completable]
     */
    fun createAnonymousAccount(data: CreateAccountData): Completable

    fun updateAccount(accountId: Long, data: UpdateAccountV3Data): Completable

    fun updateBetaAccount(accountId: Long, data: BetaData): Completable

    val myData: Completable
    val accountId: Long
    val email: String?
    val beta: Boolean
    val pubId: String?

    fun parentalConsentStatus(): ParentalConsent?

    fun doesCurrentAccountKnow(profileId: Long): Boolean

    /**
     * Check if the account has been created with email password or with a magic link
     *
     * @param email non null valid account email [String]
     * @return non null [Single] that will emit true if the account has been created with a
     * password, or false otherwise
     */
    fun accountHasPassword(email: String): Single<Boolean>

    /**
     * Login with email and password (old accounts)
     *
     * @param data non null [LoginData] credentials
     * @return non null [Completable]
     */
    fun login(data: LoginData): Completable

    /**
     * The single will emit true if the user is logged in, false if a new account has been created and
     * needs more data to complete the registration
     *
     * @param data non null [FacebookLoginData]
     * @return Boolean [Single]
     */
    fun login(data: FacebookLoginData): Single<Boolean>

    /**
     * Login with a validated magic link code
     *
     * @param validatedCode non null magic link code [String]
     * @return non null [Completable]
     */
    fun login(validatedCode: String): Completable

    /**
     * Check magic link validity
     *
     * @param code non null magic link extracted code
     * @return non null [Single] that will emit true if the code is still valid or false
     * otherwise
     */
    fun validateMagicLinkCode(code: String): Single<String>

    /**
     * Request the backend to send a magic link to the provided email
     *
     * @param email non null email [String]
     * @return non null [Completable]
     */
    fun requestMagicLink(email: String): Completable

    fun needsParentalConsent(birthdate: LocalDate): Single<Boolean>

    /**
     * Log out user
     *
     * This will clear all user data
     */
    fun logout(): Completable

    /**
     * Deletes the account remotely and, if it succeeds, removes the account and the profiles from the database
     */
    fun deleteAccount(): Completable

    fun hasConnectedAccount(): Boolean

    fun allowDataCollecting(allow: Boolean): Completable

    val isDataCollectingAllowed: Boolean

    val isAmazonDrsEnabled: Boolean

    fun resetPassword(email: String, l: KolibreeConnectorListener<Boolean>)

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        l: KolibreeConnectorListener<Boolean>
    )

    fun enableWeeklyDigest(enable: Boolean): Completable

    fun isWeeklyDigestEnabled(): Boolean

    fun createAccountByEmail(email: String): Completable

    fun createAccountByFacebook(
        email: String,
        facebookId: String?,
        facebookAuthToken: String?
    ): Single<Boolean>

    fun createEmailAccount(data: CreateAccountData): Single<AccountInternal>

    fun loginByGoogle(data: CreateAccountData): Completable

    fun createAccountByGoogle(data: CreateAccountData): Completable
}
