/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.accountinternal.internal.AccountInternal

/*
This migration fix issues of nullability that was present in java impl. (owner_profile_id and pub_id should
not be null)
It also add a new field news_email_subscription which is false by default.
 */
@Suppress("MagicNumber")
internal object MigrationFrom4To5 : Migration(4, 5) {

    override fun migrate(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("ALTER TABLE ${AccountInternal.TABLE_NAME} RENAME TO $TABLE_NAME_OLD")

            createAccountTableV5()
            fillAccountTableV5FromV4()

            execSQL("DROP TABLE $TABLE_NAME_OLD")
        }
    }

    private fun SupportSQLiteDatabase.createAccountTableV5() {
        execSQL(
            """CREATE TABLE IF NOT EXISTS `${AccountInternal.TABLE_NAME}` 
                (`id` INTEGER NOT NULL, `owner_profile_id` INTEGER NOT NULL, 
                    `current_profile_id` INTEGER, `facebook_id` TEXT, `refresh_token` TEXT NOT NULL, 
                    `access_token` TEXT NOT NULL, `token_expires` TEXT NOT NULL, `email` TEXT, 
                    `email_verified` INTEGER NOT NULL, `data_version` INTEGER, 
                    `allow_data_collecting` INTEGER NOT NULL, `weekly_digest_subscription` INTEGER NOT NULL,
                    `news_email_subscription` INTEGER NOT NULL, `pub_id` TEXT NOT NULL, `app_id` TEXT,
                    `parental_consent` INTEGER, `beta` INTEGER NOT NULL, `phone_number` TEXT,
                    `wc_openid` TEXT, `wc_unionid` TEXT, `wc_access_token` TEXT, `wc_refresh_token` TEXT,
                    `wc_expires_in` INTEGER, `wc_scope` TEXT, PRIMARY KEY(`id`))""".trimMargin()
        )
    }

    private fun SupportSQLiteDatabase.fillAccountTableV5FromV4() {
        execSQL(
            """INSERT INTO `${AccountInternal.TABLE_NAME}` 
                (`id`, `owner_profile_id`, `current_profile_id`, `facebook_id`, `refresh_token`, 
                    `access_token`, `token_expires`, `email`, `email_verified`, `data_version`, 
                    `allow_data_collecting`, `weekly_digest_subscription`,`news_email_subscription`,
                    `pub_id`, `app_id`, `parental_consent`, `beta`, `phone_number`, `wc_openid`, 
                    `wc_unionid`, `wc_access_token`, `wc_refresh_token`, `wc_expires_in`, `wc_scope`) 
                    
                    SELECT `id`, IFNULL(owner_profile_id, 0), `current_profile_id`, `facebook_id`, `refresh_token`, 
                    `access_token`, `token_expires`, `email`, `email_verified`, `data_version`, 
                    `allow_data_collecting`, `weekly_digest_subscription`, 0, IFNULL(pub_id, ""), `app_id`,
                    `parental_consent`, `beta`, `phone_number`,
                    `wc_openid`, `wc_unionid`, `wc_access_token`, `wc_refresh_token`,
                    `wc_expires_in`, `wc_scope` FROM $TABLE_NAME_OLD
            """.trimMargin()
        )
    }
}

private const val TABLE_NAME_OLD = "tmp_account"
