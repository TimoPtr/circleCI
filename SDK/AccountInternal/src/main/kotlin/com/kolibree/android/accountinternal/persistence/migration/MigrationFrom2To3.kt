/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.migration

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.defensive.PreconditionsKt
import com.kolibree.android.room.intValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import timber.log.Timber

@Suppress("MagicNumber")
internal object MigrationFrom2To3 : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        addNeedsUpdateColumn(database)

        when (verifyDataIntegrity(database)) {
            DataIntegrityState.CORRUPTED -> {
                // This means user data could not be corrected, so the only ways is to log him out.
                // We cannot use [delete] method, as it will re-execute the migration, so we will
                // end up with StackOverflow.
                wipeUserData(database)
            }
            DataIntegrityState.CORRECTION_APPLIED -> {
                // Data integrity issues were found, but we managed to fix them. Account and profiles
                // will be updated on the backend side next time when data will be fetched from DB.
                // No-op here, as data were already marked for update.
            }
            else -> {
                // No integrity issues found, we're all good!
            }
        }
    }

    private fun addNeedsUpdateColumn(database: SupportSQLiteDatabase) {
        // We don't need this info for the Account table cause all issues there are irrecoverable.
        database.execSQL(
            "ALTER TABLE `${ProfileInternal.TABLE_NAME}`" +
                " ADD COLUMN `${ProfileInternal.FIELD_NEEDS_UPDATE}`" +
                " INTEGER NOT NULL DEFAULT 0"
        )
    }

    private fun verifyDataIntegrity(database: SupportSQLiteDatabase): DataIntegrityState {
        var integrityState = DataIntegrityState.OK
        integrityState += verifyProfilesIntegrity(database)
        integrityState += verifyAccountIntegrity(database)
        return integrityState
    }

    private fun verifyProfilesIntegrity(database: SupportSQLiteDatabase): DataIntegrityState {
        var dataState = DataIntegrityState.OK
        val cursor = database.query("SELECT DISTINCT * FROM profiles")
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            dataState += verifyProfileIntegrity(database, cursor)
        }
        return dataState
    }

    private fun verifyAccountIntegrity(database: SupportSQLiteDatabase): DataIntegrityState {
        var dataState = DataIntegrityState.OK
        val cursor = database.query("SELECT * FROM account LIMIT 1")
        if (cursor.count == 0) return dataState

        cursor.moveToFirst()

        val id = cursor.intValueForColumn(AccountInternal.COLUMN_ACCOUNT_ID)
        val ownerProfileId =
            cursor.longValueForColumn(AccountInternal.COLUMN_ACCOUNT_OWNER_PROFILE_ID)
        val currentProfileId = cursor.longValueForColumn(AccountInternal.COLUMN_CURRENT_PROFILE_ID)

        val correctColumn: (String, String) -> Unit = { column, value ->
            database.execSQL("UPDATE account SET $column = $value WHERE id = $id")
        }

        dataState += id.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentNonNegative(this!!)
        })
        dataState += ownerProfileId.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentNonNegative(this!!)
        })
        dataState += currentProfileId.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentNonNegative(this!!)
        }, correction = {
            ownerProfileId?.let { correctColumn(AccountInternal.COLUMN_CURRENT_PROFILE_ID, "$it") }
        })

        return dataState
    }

    private fun verifyProfileIntegrity(
        database: SupportSQLiteDatabase,
        cursor: Cursor
    ): DataIntegrityState {
        var dataState = DataIntegrityState.OK

        val id = cursor.intValueForColumn(ProfileInternal.FIELD_ID)
        val firstName = cursor.stringValueForColumn(ProfileInternal.FIELD_FIRST_NAME)
        val brushingTime = cursor.intValueForColumn(ProfileInternal.FIELD_BRUSHING_GOAL_TIME)
        val creationDate = cursor.stringValueForColumn(ProfileInternal.FIELD_CREATION_DATE)
        val age = cursor.intValueForColumn(ProfileInternal.FIELD_AGE)
        val brushingNumber = cursor.intValueForColumn(ProfileInternal.FIELD_BUSHING_NB)

        dataState += id.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentNonNegative(this!!)
        })

        val correctColumn: (String, String) -> Unit = { column, value ->
            database.execSQL("UPDATE profiles SET needs_update = 1, $column = $value where id = $id")
        }

        dataState += firstName.verifyIntegrity(
            preconditions = {
                Preconditions.checkNotNull(this)
                Preconditions.checkArgument(this!!.isNotEmpty())
            },
            correction = {
                correctColumn(
                    ProfileInternal.FIELD_FIRST_NAME,
                    Profile.DEFAULT_FIRST_NAME
                )
            }
        )

        dataState += brushingTime.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentInRange(
                this!!,
                IBrushing.MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
                IBrushing.MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
                "goal duration"
            )
        }, correction = {
            correctColumn(
                ProfileInternal.FIELD_BRUSHING_GOAL_TIME,
                "${IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS}"
            )
        })

        dataState += creationDate.verifyIntegrity(preconditions = {
            PreconditionsKt.checkArgumentContainsZonedDateTime(this)
        })

        dataState += age.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentNonNegative(this!!)
        }, correction = { correctColumn(ProfileInternal.FIELD_AGE, "${Profile.DEFAULT_AGE}") })

        dataState += brushingNumber.verifyIntegrity(preconditions = {
            Preconditions.checkNotNull(this)
            Preconditions.checkArgumentNonNegative(this!!)
        }, correction = { correctColumn(ProfileInternal.FIELD_BUSHING_NB, "0") })

        return dataState
    }

    private fun wipeUserData(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM ${ProfileInternal.TABLE_NAME}")
        database.execSQL("DELETE FROM ${AccountInternal.TABLE_NAME}")
    }
}

internal enum class DataIntegrityState {

    OK, CORRECTION_APPLIED, CORRUPTED;

    operator fun plus(other: DataIntegrityState): DataIntegrityState =
        if (ordinal > other.ordinal) this else other
}

@Suppress("TooGenericExceptionCaught")
private fun <T> T.verifyIntegrity(
    preconditions: T.() -> Unit,
    correction: (() -> Unit)? = null
): DataIntegrityState = try {
    preconditions()
    DataIntegrityState.OK
} catch (e: Exception) {
    if (correction != null) {
        try {
            correction()
            DataIntegrityState.CORRECTION_APPLIED
        } catch (e: Exception) {
            Timber.e(e, "Data corrupted")
            DataIntegrityState.CORRUPTED
        }
    } else DataIntegrityState.CORRUPTED
}
