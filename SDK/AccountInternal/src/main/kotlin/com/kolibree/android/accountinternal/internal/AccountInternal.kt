/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.internal

import androidx.annotation.Keep
import androidx.annotation.RestrictTo
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.kolibree.android.accountinternal.account.AccountConverters
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.defensive.Preconditions

/** Created by aurelien on 01/07/15.  */
@Entity(tableName = AccountInternal.TABLE_NAME)
@TypeConverters(
    AccountConverters::class
)
@Keep
data class AccountInternal @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) constructor(
    @SerializedName(COLUMN_ACCOUNT_ID)
    @PrimaryKey
    var id: Long = 0,

    @ColumnInfo(name = COLUMN_ACCOUNT_OWNER_PROFILE_ID)
    @SerializedName(COLUMN_ACCOUNT_OWNER_PROFILE_ID)
    var ownerProfileId: Long = 0,

    @ColumnInfo(name = COLUMN_CURRENT_PROFILE_ID)
    @SerializedName(COLUMN_CURRENT_PROFILE_ID)
    var currentProfileId: Long? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_FACEBOOK_ID)
    @SerializedName(COLUMN_ACCOUNT_FACEBOOK_ID)
    var facebookId: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_REFRESH_TOKEN)
    @SerializedName(COLUMN_ACCOUNT_REFRESH_TOKEN)
    var refreshToken: String = "",

    @ColumnInfo(name = COLUMN_ACCOUNT_ACCESS_TOKEN)
    @SerializedName(COLUMN_ACCOUNT_ACCESS_TOKEN)
    var accessToken: String = "",

    @ColumnInfo(name = COLUMN_ACCOUNT_TOKEN_EXPIRES)
    @SerializedName(COLUMN_ACCOUNT_TOKEN_EXPIRES)
    var tokenExpires: String = "",

    @ColumnInfo(name = COLUMN_ACCOUNT_EMAIL)
    @SerializedName(COLUMN_ACCOUNT_EMAIL)
    var email: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_EMAIL_VERIFIED)
    @SerializedName(COLUMN_ACCOUNT_EMAIL_VERIFIED)
    var isEmailVerified: Boolean = false,

    @ColumnInfo(name = COLUMN_ACCOUNT_DATA_VERSION)
    @SerializedName(COLUMN_ACCOUNT_DATA_VERSION)
    var dataVersion: Int? = null,

    @ColumnInfo(name = COLUMN_ALLOW_DATA_COLLECTING)
    var isAllowDataCollecting: Boolean = true,

    @ColumnInfo(name = COLUMN_ACCOUNT_DIGEST_SUBSCRIPTION)
    @SerializedName(COLUMN_ACCOUNT_DIGEST_SUBSCRIPTION)
    var isDigestEnabled: Boolean = false,

    @ColumnInfo(name = COLUMN_ACCOUNT_EMAIL_NEWSLETTER_SUBSCRIPTION)
    @SerializedName(COLUMN_ACCOUNT_EMAIL_NEWSLETTER_SUBSCRIPTION)
    var emailNewsletterSubscription: Boolean = false,

    @ColumnInfo(name = COLUMN_ACCOUNT_AMAZON_DRS_ENABLED)
    @SerializedName(COLUMN_ACCOUNT_AMAZON_DRS_ENABLED)
    var isAmazonDrsEnabled: Boolean = false,

    @ColumnInfo(name = COLUMN_ACCOUNT_PUB_ID)
    @SerializedName("pubid")
    var pubId: String = "",

    @ColumnInfo(name = COLUMN_ACCOUNT_APP_ID)
    @SerializedName("appid")
    var appId: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_PARENTAL_CONSENT)
    @SerializedName(COLUMN_ACCOUNT_PARENTAL_CONSENT)
    var parentalConsent: ParentalConsent? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_BETA)
    @SerializedName(COLUMN_ACCOUNT_BETA)
    var isBeta: Boolean = false,

    @ColumnInfo(name = COLUMN_ACCOUNT_PHONE_NUMBER)
    @SerializedName(COLUMN_ACCOUNT_PHONE_NUMBER)
    var phoneNumber: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_WC_OPEN_ID)
    var wcOpenId: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_WC_UNION_ID)
    var wcUnionId: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_WC_ACCESS_TOKEN)
    var wcAccessToken: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_WC_REFRESH_TOKEN)
    var wcRefreshToken: String? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_WC_EXPIRES_IN)
    var wcExpiresIn: Int? = null,

    @ColumnInfo(name = COLUMN_ACCOUNT_WC_SCOPE)
    var wcScope: String? = null
) {

    @Ignore
    private val profiles = mutableListOf<ProfileInternal>()

    val ownerProfile: Profile
        get() = ownerProfileInternal.exportProfile()

    init {
        Preconditions.checkArgumentNonNegative(id)
        Preconditions.checkArgumentNonNegative(ownerProfileId)
        if (currentProfileId != null) {
            Preconditions.checkArgumentNonNegative(currentProfileId!!)
        }
    }

    fun setOwnerProfileAsCurrent() {
        currentProfileId = ownerProfileInternal.id
    }

    private val ownerProfileInternal: ProfileInternal
        get() =
            profiles.find { it.isOwnerProfile } ?: throw IllegalStateException("No owner profile")

    fun setWechat(wechat: AccountWechat?) {
        wechat?.let {
            wcAccessToken = wechat.wcAccessToken
            wcExpiresIn = wechat.wcExpiresIn
            wcOpenId = wechat.wcOpenId
            wcUnionId = wechat.wcUnionId
            wcScope = wechat.wcScope
            wcRefreshToken = wechat.wcRefreshToken
        }
    }

    var internalProfiles: List<ProfileInternal>
        get() = profiles
        set(profiles) {
            this.profiles.clear()
            this.profiles.addAll(profiles)
        }

    fun updateTokensWith(newOne: RefreshTokenProvider) {
        accessToken = newOne.getAccessToken()
        val newToken = newOne.getRefreshToken()
        if (newToken.isNotEmpty()) {
            refreshToken = newToken
        }
    }

    fun getProfileInternalWithId(id: Long): ProfileInternal? = profiles.find { it.id == id }

    fun addProfile(profile: ProfileInternal) {
        if (!profiles.contains(profile)) profiles.add(profile)
    }

    fun removeProfile(profileInternalWithId: ProfileInternal?) {
        profiles.remove(profileInternalWithId)
    }

    fun knows(profileId: Long): Boolean = profiles.map(ProfileInternal::id).contains(profileId)

    internal companion object {
        // Name
        const val TABLE_NAME = "account"

        // Fields
        const val COLUMN_ACCOUNT_ID = "id"
        const val COLUMN_ACCOUNT_FACEBOOK_ID = "facebook_id"
        const val COLUMN_ACCOUNT_ACCESS_TOKEN = "access_token"
        const val COLUMN_ACCOUNT_REFRESH_TOKEN = "refresh_token"
        const val COLUMN_ACCOUNT_TOKEN_EXPIRES = "token_expires"
        const val COLUMN_ACCOUNT_EMAIL = "email"
        const val COLUMN_ACCOUNT_OWNER_PROFILE_ID = "owner_profile_id"
        const val COLUMN_ACCOUNT_EMAIL_VERIFIED = "email_verified"
        const val COLUMN_ACCOUNT_DATA_VERSION = "data_version"
        const val COLUMN_CURRENT_PROFILE_ID = "current_profile_id"
        const val COLUMN_ALLOW_DATA_COLLECTING = "allow_data_collecting"
        const val COLUMN_ACCOUNT_DIGEST_SUBSCRIPTION = "weekly_digest_subscription"
        const val COLUMN_ACCOUNT_PUB_ID = "pub_id"
        const val COLUMN_ACCOUNT_APP_ID = "app_id"
        const val COLUMN_ACCOUNT_PARENTAL_CONSENT = "parental_consent"
        const val COLUMN_ACCOUNT_PHONE_NUMBER = "phone_number"
        const val COLUMN_ACCOUNT_BETA = "beta"
        const val COLUMN_ACCOUNT_WC_OPEN_ID = "wc_openid"
        const val COLUMN_ACCOUNT_WC_UNION_ID = "wc_unionid"
        const val COLUMN_ACCOUNT_WC_ACCESS_TOKEN = "wc_access_token"
        const val COLUMN_ACCOUNT_WC_REFRESH_TOKEN = "wc_refresh_token"
        const val COLUMN_ACCOUNT_WC_EXPIRES_IN = "wc_expires_in"
        const val COLUMN_ACCOUNT_WC_SCOPE = "wc_scope"
        const val COLUMN_ACCOUNT_EMAIL_NEWSLETTER_SUBSCRIPTION = "news_email_subscription"
        const val COLUMN_ACCOUNT_AMAZON_DRS_ENABLED = "amazon_drs_enabled"
    }
}
