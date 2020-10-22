/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.internal

import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.accountinternal.AccountRoomDatabase
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom4To5
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom5To6
import com.kolibree.android.room.booleanValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.android.test.sqlBool
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test

internal class AccountInternalMigrationTest : BaseRoomMigrationTest<AccountRoomDatabase>(
    AccountRoomDatabase::class,
    AccountRoomDatabase.DATABASE_NAME
) {

    @Test
    fun migrationFrom4To5_containsCorrectData() {
        initializeDatabaseWith(4) {
            insertAccountV4()
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom4To5) {
            val cursor = fetchAccounts()
            cursor.moveToFirst()

            assertEquals(DEFAULT_ID, cursor.longValueForColumn(AccountInternal.COLUMN_ACCOUNT_ID))
            assertEquals(
                0L,
                cursor.longValueForColumn(AccountInternal.COLUMN_ACCOUNT_OWNER_PROFILE_ID)!!
            )
            assertFalse(cursor.booleanValueForColumn(AccountInternal.COLUMN_ACCOUNT_EMAIL_NEWSLETTER_SUBSCRIPTION)!!)
            assertEquals("", cursor.stringValueForColumn(AccountInternal.COLUMN_ACCOUNT_PUB_ID)!!)
        }
    }

    @Test
    fun migrationFrom5To6_containsCorrectData() {
        initializeDatabaseWith(5) {
            insertAccountV5()
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom5To6) {
            val cursor = fetchAccounts()
            cursor.moveToFirst()

            assertEquals(
                DEFAULT_AMAZON_DRS_ENABLED,
                cursor.booleanValueForColumn(AccountInternal.COLUMN_ACCOUNT_AMAZON_DRS_ENABLED)
            )
        }
    }

    private fun SupportSQLiteDatabase.insertAccountV4(
        id: Long = DEFAULT_ID,
        facebookId: Long = DEFAULT_FACEBOOK_ID,
        refreshToken: String = DEFAULT_REFRESH_TOKEN,
        accessToken: String = DEFAULT_ACCESS_TOKEN,
        tokenExpiresIn: String = DEFAULT_TOKEN_EXPIRES,
        email: String = DEFAULT_EMAIL,
        ownerProfileId: Long? = null,
        currentProfileId: Long = DEFAULT_CURRENT_PROFILE_ID,
        isEmailVerified: Boolean = DEFAULT_IS_EMAIL_VERIFIED,
        dataVersion: Int = DEFAULT_DATA_VERSION,
        allowDataCollecting: Boolean = DEFAULT_ALLOW_DATA_COLLECTING,
        isDigestEnabled: Boolean = DEFAULT_IS_DIGEST_ENABLED,
        pubId: String? = null,
        appId: String = DEFAULT_APP_ID,
        parentConsent: Boolean = DEFAULT_PARENT_CONSENT,
        beta: Boolean = DEFAULT_BETA,
        phoneNumber: String = DEFAULT_PHONE_NUMBER,
        wcOpenId: String = DEFAULT_WC_OPEN_ID,
        wcUnionId: String = DEFAULT_WC_UNION_ID,
        wcAccessToken: String = DEFAULT_WC_ACCESS_TOKEN,
        wcRefreshToken: String = DEFAULT_WC_REFRESH_TOKEN,
        wcExpiresIn: Int = DEFAULT_WC_EXPIRES_IN,
        wcScope: String = DEFAULT_WC_SCOPE
    ) {
        execSQL(
            """INSERT INTO ${AccountInternal.TABLE_NAME} 
                (id, facebook_id, refresh_token, access_token, token_expires, email, owner_profile_id,
                    email_verified, data_version, current_profile_id, allow_data_collecting, 
                    weekly_digest_subscription, pub_id, app_id, parental_consent, beta, phone_number, 
                    wc_openid, wc_unionid, wc_access_token, wc_refresh_token, wc_expires_in, wc_scope) 
                VALUES 
                ($id, $facebookId, '$refreshToken', '$accessToken', '$tokenExpiresIn', '$email',
                    $ownerProfileId, $currentProfileId, 
                    ${isEmailVerified.sqlBool()}, $dataVersion, ${allowDataCollecting.sqlBool()}, 
                    ${isDigestEnabled.sqlBool()}, $pubId, '$appId', ${parentConsent.sqlBool()},
                    ${beta.sqlBool()}, '$phoneNumber', '$wcOpenId', '$wcUnionId', '$wcAccessToken',
                    '$wcRefreshToken',  $wcExpiresIn, '$wcScope')""".trimMargin()
        )
    }

    private fun SupportSQLiteDatabase.insertAccountV5(
        id: Long = DEFAULT_ID,
        facebookId: Long = DEFAULT_FACEBOOK_ID,
        refreshToken: String = DEFAULT_REFRESH_TOKEN,
        accessToken: String = DEFAULT_ACCESS_TOKEN,
        tokenExpiresIn: String = DEFAULT_TOKEN_EXPIRES,
        email: String = DEFAULT_EMAIL,
        ownerProfileId: Long = DEFAULT_OWNER_ID,
        currentProfileId: Long = DEFAULT_CURRENT_PROFILE_ID,
        isEmailVerified: Boolean = DEFAULT_IS_EMAIL_VERIFIED,
        dataVersion: Int = DEFAULT_DATA_VERSION,
        allowDataCollecting: Boolean = DEFAULT_ALLOW_DATA_COLLECTING,
        isDigestEnabled: Boolean = DEFAULT_IS_DIGEST_ENABLED,
        emailNewsletterSubscription: Boolean = DEFAULT_NEWS_EMAIL_SUBSCRIPTION,
        pubId: String = DEFAULT_PUB_ID,
        appId: String = DEFAULT_APP_ID,
        parentConsent: Boolean = DEFAULT_PARENT_CONSENT,
        beta: Boolean = DEFAULT_BETA,
        phoneNumber: String = DEFAULT_PHONE_NUMBER,
        wcOpenId: String = DEFAULT_WC_OPEN_ID,
        wcUnionId: String = DEFAULT_WC_UNION_ID,
        wcAccessToken: String = DEFAULT_WC_ACCESS_TOKEN,
        wcRefreshToken: String = DEFAULT_WC_REFRESH_TOKEN,
        wcExpiresIn: Int = DEFAULT_WC_EXPIRES_IN,
        wcScope: String = DEFAULT_WC_SCOPE
    ) {
        execSQL(
            """INSERT INTO ${AccountInternal.TABLE_NAME} 
                (`id`, `owner_profile_id`, `current_profile_id`, `facebook_id`, `refresh_token`, 
                    `access_token`, `token_expires`, `email`, `email_verified`, `data_version`, 
                    `allow_data_collecting`, `weekly_digest_subscription`,`news_email_subscription`,
                    `pub_id`, `app_id`, `parental_consent`, `beta`, `phone_number`, `wc_openid`, 
                    `wc_unionid`, `wc_access_token`, `wc_refresh_token`, `wc_expires_in`, `wc_scope`)
                VALUES 
                ($id, $facebookId, '$refreshToken', '$accessToken', '$tokenExpiresIn', '$email',
                    $ownerProfileId, $currentProfileId, 
                    ${isEmailVerified.sqlBool()}, $dataVersion, ${allowDataCollecting.sqlBool()}, 
                    ${isDigestEnabled.sqlBool()}, ${emailNewsletterSubscription.sqlBool()}, '$pubId', 
                    '$appId', ${parentConsent.sqlBool()},${beta.sqlBool()}, '$phoneNumber', '$wcOpenId',
                    '$wcUnionId', '$wcAccessToken','$wcRefreshToken',  $wcExpiresIn, '$wcScope')""".trimMargin()
        )
    }

    private fun SupportSQLiteDatabase.fetchAccounts() =
        query("SELECT DISTINCT * FROM ${AccountInternal.TABLE_NAME}", emptyArray())

    companion object {
        const val DEFAULT_ID = 1L
        const val DEFAULT_CURRENT_PROFILE_ID = 2L
        const val DEFAULT_FACEBOOK_ID = 5L
        const val DEFAULT_OWNER_ID = 6L
        const val DEFAULT_REFRESH_TOKEN = "refresh"
        const val DEFAULT_ACCESS_TOKEN = "access"
        const val DEFAULT_TOKEN_EXPIRES = "expires"
        const val DEFAULT_EMAIL = "refresh"
        const val DEFAULT_IS_EMAIL_VERIFIED = false
        const val DEFAULT_DATA_VERSION = 0
        const val DEFAULT_ALLOW_DATA_COLLECTING = false
        const val DEFAULT_IS_DIGEST_ENABLED = false
        const val DEFAULT_NEWS_EMAIL_SUBSCRIPTION = false
        const val DEFAULT_AMAZON_DRS_ENABLED = false
        const val DEFAULT_APP_ID = "appid"
        const val DEFAULT_PUB_ID = "pubid"
        const val DEFAULT_PARENT_CONSENT = false
        const val DEFAULT_BETA = false
        const val DEFAULT_PHONE_NUMBER = "phone number"
        const val DEFAULT_WC_OPEN_ID = "open id"
        const val DEFAULT_WC_UNION_ID = "union id"
        const val DEFAULT_WC_ACCESS_TOKEN = "access token"
        const val DEFAULT_WC_REFRESH_TOKEN = "refresh token"
        const val DEFAULT_WC_EXPIRES_IN = 0
        const val DEFAULT_WC_SCOPE = "scope"
    }
}
