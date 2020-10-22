/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.repo

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.internal.AccountInternal
import io.reactivex.Flowable
import io.reactivex.Maybe

@SuppressWarnings("TooManyFunctions")
@Keep
interface AccountDatastore {
    /**
     * Remove Account and its associated Profiles
     */
    fun truncate()

    /**
     * Stores the unique account in the system
     *
     * Assumes internalProfiles, if any, have been set
     *
     * If a previous account existed, it'll be removed
     */
    fun setAccount(newAccount: AccountInternal)

    fun getAccountMaybe(): Maybe<AccountInternal>

    fun updateCurrentProfileId(account: AccountInternal)

    fun updateTokens(account: AccountInternal)

    fun updateEmail(account: AccountInternal)

    fun updateFacebookId(account: AccountInternal)

    fun setUpdateAllowDataCollecting(account: AccountInternal)

    fun updateAllowDigest(account: AccountInternal)

    fun accountFlowable(): Flowable<AccountInternal>

    fun updatePhoneNumber(account: AccountInternal)

    fun updateWeChatData(account: AccountInternal)

    fun updateNewsletterSubscription(isNewsletterSubscriptionOn: Boolean)
}
