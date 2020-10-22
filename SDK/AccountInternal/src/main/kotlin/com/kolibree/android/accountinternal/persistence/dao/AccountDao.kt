/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.accountinternal.internal.AccountInternal
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
internal interface AccountDao {
    @Query("DELETE FROM account")
    fun truncate()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: AccountInternal)

    @Query("SELECT * FROM account LIMIT 1")
    fun getAccount(): AccountInternal?

    @Query("SELECT * FROM account LIMIT 1")
    fun getAccountMaybe(): Maybe<AccountInternal>

    @Query("SELECT * FROM account LIMIT 1")
    fun getAccountFlowable(): Flowable<AccountInternal>

    @Query("UPDATE account SET current_profile_id=:currentProfileId")
    fun updateCurrentProfileId(currentProfileId: Long?)

    @Query("UPDATE account SET access_token=:accessToken, refresh_token=:refreshToken")
    fun updateTokens(accessToken: String, refreshToken: String)

    @Query("UPDATE account SET email=:email")
    fun updateEmail(email: String?)

    @Query("UPDATE account SET facebook_id=:facebookId")
    fun updateFacebookId(facebookId: String?)

    @Query("UPDATE account SET allow_data_collecting=:allowDataCollecting")
    fun updateAllowDataCollecting(allowDataCollecting: Boolean)

    @Query("UPDATE account SET weekly_digest_subscription=:allowDigest")
    fun updateAllowDigest(allowDigest: Boolean)

    @Query("UPDATE account SET phone_number=:phoneNumber")
    fun updatePhoneNumber(phoneNumber: String?)

    @Query("UPDATE account SET news_email_subscription=:isNewsletterSubscriptionOn")
    fun updateNewsletterSubscription(isNewsletterSubscriptionOn: Boolean)

    @SuppressWarnings("LongParameterList")
    @Query("UPDATE account SET wc_openid=:openId, wc_unionid=:unionId, " +
        "wc_access_token=:accessToken, wc_refresh_token=:refreshToken, wc_expires_in=:expiresIn," +
        "wc_scope=:scope")
    fun updateWeChatData(
        openId: String?,
        unionId: String?,
        accessToken: String?,
        refreshToken: String?,
        expiresIn: Int?,
        scope: String?
    )
}
