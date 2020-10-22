package com.kolibree.android.accountinternal.internal

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.accountinternal.AccountRoomDatabase
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom1To2
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom2To3
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom3To4
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.room.booleanValueForColumn
import com.kolibree.android.room.intValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.android.test.sqlBool
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ProfileInternalMigrationTest : BaseRoomMigrationTest<AccountRoomDatabase>(
    AccountRoomDatabase::class,
    AccountRoomDatabase.DATABASE_NAME
) {

    @Test
    fun migrationFrom1To2_containsCorrectData() {
        val profile = createProfileInternal()

        initializeDatabaseWith(schemaVersion = 1) {
            insertProfileInSchemaVersion1(profile)
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom1To2) {
            val cursor = fetchProfiles()
            assertEquals(1, cursor.count)
            cursor.moveToFirst()

            with(profile) {
                assertEquals(id, cursor.longValueForColumn("id"))
                assertEquals(firstName, cursor.stringValueForColumn("first_name"))
                assertEquals(points, cursor.intValueForColumn("points"))
                assertEquals(isOwnerProfile, cursor.booleanValueForColumn("is_owner_profile"))
                assertEquals(accountId, cursor.intValueForColumn("account"))
                assertEquals(brushingTime, cursor.intValueForColumn("brushing_goal_time"))
                assertEquals(transitionSounds, cursor.booleanValueForColumn("transition_sounds"))
                assertEquals(creationDate, cursor.stringValueForColumn("created_at"))
                assertEquals(exactBirthday, cursor.booleanValueForColumn("exact_birthday"))
                assertEquals(age, cursor.intValueForColumn("age"))
                assertEquals(0, cursor.intValueForColumn("brushing_number"))
            }
        }
    }

    @Test
    fun migrationFrom2To3_passesCorrectlyForUncorruptedData() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(createProfileInternal())
            insertAccount(createAccountInternal())

            assertEquals(1, fetchProfiles().count)
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            run {
                val cursor = fetchProfiles()
                assertEquals(1, cursor.count)

                cursor.moveToFirst()
                assertEquals(0, cursor.intValueForColumn("needs_update"))
            }

            run {
                val cursor = fetchAccounts()
                assertEquals(1, cursor.count)
            }
        }
    }

    @Test
    fun migrationFrom2To3_wipesDatabaseForNegativeAccountId() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertAccount(createAccountInternal(), id = -1)
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchAccounts()
            assertEquals(0, cursor.count)
        }
    }

    @Test
    fun migrationFrom2To3_wipesDatabaseForNullOwnerProfileId() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertAccount(createAccountInternal(), ownerProfileId = null)
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchAccounts()
            assertEquals(0, cursor.count)
        }
    }

    @Test
    fun migrationFrom2To3_wipesDatabaseForNegativeOwnerProfileId() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertAccount(createAccountInternal(), ownerProfileId = -1)
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchAccounts()
            assertEquals(0, cursor.count)
        }
    }

    @Test
    fun migrationFrom2To3_correctsDatabaseForNullCurrentProfileId() {
        val account = createAccountInternal()
        initializeDatabaseWith(schemaVersion = 2) {
            insertAccount(account, currentProfileId = null)
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchAccounts()
            assertEquals(1, cursor.count)

            cursor.moveToFirst()
            assertEquals(account.ownerProfileId, cursor.longValueForColumn("current_profile_id"))
        }
    }

    @Test
    fun migrationFrom2To3_correctsDatabaseForNegativeCurrentProfileId() {
        val account = createAccountInternal()
        initializeDatabaseWith(schemaVersion = 2) {
            insertAccount(account, currentProfileId = -1)
            assertEquals(1, fetchAccounts().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchAccounts()
            assertEquals(1, cursor.count)

            cursor.moveToFirst()
            assertEquals(account.ownerProfileId, cursor.longValueForColumn("current_profile_id"))
        }
    }

    @Test
    fun migrationFrom2To3_wipesDatabaseForNegativeProfileId() {
        val profile = createProfileInternal()

        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(profile, id = -1)
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            assertEquals(0, fetchProfiles().count)
        }
    }

    @Test
    fun migrationFrom2To3_wipesDatabaseForEmptyFirstName() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(createProfileInternal(), firstName = "")
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            assertEquals(0, fetchProfiles().count)
        }
    }

    @Test
    fun migrationFrom2To3_correctsDatabaseForTooLowBrushingTime() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(
                createProfileInternal(),
                brushingTime = MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS - 1
            )
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchProfiles()
            assertEquals(1, cursor.count)

            cursor.moveToFirst()
            assertEquals(1, cursor.intValueForColumn("needs_update"))
            assertEquals(
                IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS,
                cursor.intValueForColumn("brushing_goal_time")
            )
        }
    }

    @Test
    fun migrationFrom2To3_wipesDatabaseForIncorrectCreationDateFormat() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(
                createProfileInternal(),
                creationDate = "IncorrectDateFormat"
            )
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            assertEquals(0, fetchProfiles().count)
        }
    }

    @Test
    fun migrationFrom2To3_correctsDatabaseForNegativeAge() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(createProfileInternal(), age = -10)
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchProfiles()
            assertEquals(1, cursor.count)

            cursor.moveToFirst()
            assertEquals(1, cursor.intValueForColumn("needs_update"))
            assertEquals(Profile.DEFAULT_AGE, cursor.intValueForColumn("age"))
        }
    }

    @Test
    fun migrationFrom2To3_correctsDatabaseForNegativeBrushingNumber() {
        initializeDatabaseWith(schemaVersion = 2) {
            insertProfileInSchemaVersion2(createProfileInternal(), brushingNumber = -1)
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom2To3) {
            val cursor = fetchProfiles()
            assertEquals(1, cursor.count)

            cursor.moveToFirst()
            assertEquals(1, cursor.intValueForColumn("needs_update"))
            assertEquals(0, cursor.intValueForColumn("brushing_number"))
        }
    }

    @Test
    fun migrationFrom3To4_containsCorrectData() {
        val profile = createProfileInternal()

        initializeDatabaseWith(schemaVersion = 3) {
            insertProfileInSchemaVersion3(profile)
            assertEquals(1, fetchProfiles().count)
        }

        runMigrationAndCheck(MigrationFrom3To4) {
            val cursor = fetchProfiles()
            assertEquals(1, cursor.count)
            cursor.moveToFirst()

            with(profile) {
                assertEquals(id, cursor.longValueForColumn("id"))
                assertEquals(firstName, cursor.stringValueForColumn("first_name"))
                assertEquals(points, cursor.intValueForColumn("points"))
                assertEquals(isOwnerProfile, cursor.booleanValueForColumn("is_owner_profile"))
                assertEquals(accountId, cursor.intValueForColumn("account"))
                assertEquals(brushingTime, cursor.intValueForColumn("brushing_goal_time"))
                assertEquals(transitionSounds, cursor.booleanValueForColumn("transition_sounds"))
                assertEquals(creationDate, cursor.stringValueForColumn("created_at"))
                assertEquals(exactBirthday, cursor.booleanValueForColumn("exact_birthday"))
                assertEquals(age, cursor.intValueForColumn("age"))
                assertEquals(0, cursor.intValueForColumn("brushing_number"))
                assertEquals(pictureLastModifier, cursor.stringValueForColumn("picture_last_modifier"))
            }
        }
    }

    companion object {

        private fun SupportSQLiteDatabase.insertAccount(
            account: AccountInternal,
            id: Long = account.id,
            ownerProfileId: Long? = account.ownerProfileId,
            currentProfileId: Long? = account.currentProfileId,
            refreshToken: String = REFRESH_TOKEN,
            accessToken: String = ACCESS_TOKEN,
            tokenExpires: String = TOKEN_EXPIRES,
            isEmailVerified: Boolean = EMAIL_VERIFIED,
            isAllowDataCollecting: Boolean = ALLOW_DATA_COLLECTING,
            isDigestEnabled: Boolean = WEEKLY_DIGEST_SUBSCRIPTION,
            beta: Boolean = BETA
        ) {
            execSQL(
                "INSERT INTO account " +
                    "(id, owner_profile_id, current_profile_id, refresh_token, access_token, " +
                    "token_expires, email_verified, allow_data_collecting, " +
                    "weekly_digest_subscription, beta) " +
                    "VALUES " +
                    "($id, $ownerProfileId, $currentProfileId, '$refreshToken', '$accessToken', " +
                    "'$tokenExpires', ${isEmailVerified.sqlBool()}, ${isAllowDataCollecting.sqlBool()}, " +
                    "${isDigestEnabled.sqlBool()}, ${beta.sqlBool()})"
            )
        }

        private fun SupportSQLiteDatabase.insertProfileInSchemaVersion1(
            profile: ProfileInternal,
            id: Long = profile.id,
            firstName: String = profile.firstName,
            points: Int = profile.points,
            isOwnerProfile: Boolean = profile.isOwnerProfile,
            accountId: Int = profile.accountId,
            brushingTime: Int = profile.brushingTime,
            transitionSounds: Boolean = profile.transitionSounds,
            creationDate: String = profile.creationDate,
            exactBirthday: Boolean = profile.exactBirthday,
            age: Int = profile.age
        ) {
            execSQL(
                "INSERT INTO profiles " +
                    "(id, first_name, points, is_owner_profile, account," +
                    "brushing_goal_time, transition_sounds, created_at," +
                    "exact_birthday, age)" +
                    "VALUES " +
                    "($id, '$firstName', $points, ${isOwnerProfile.sqlBool()}, $accountId, " +
                    "$brushingTime, ${transitionSounds.sqlBool()}, '$creationDate'," +
                    "${exactBirthday.sqlBool()}, $age)"
            )
        }

        private fun SupportSQLiteDatabase.insertProfileInSchemaVersion2(
            profile: ProfileInternal,
            id: Long = profile.id,
            firstName: String = profile.firstName,
            points: Int = profile.points,
            isOwnerProfile: Boolean = profile.isOwnerProfile,
            accountId: Int = profile.accountId,
            brushingTime: Int = profile.brushingTime,
            transitionSounds: Boolean = profile.transitionSounds,
            creationDate: String = profile.creationDate,
            exactBirthday: Boolean = profile.exactBirthday,
            age: Int = profile.age,
            brushingNumber: Int = profile.brushingNumber
        ) {
            execSQL(
                "INSERT INTO profiles " +
                    "(id, first_name, points, is_owner_profile, account," +
                    "brushing_goal_time, transition_sounds, created_at," +
                    "exact_birthday, age, brushing_number)" +
                    "VALUES " +
                    "($id, '$firstName', $points, ${isOwnerProfile.sqlBool()}, $accountId, " +
                    "$brushingTime, ${transitionSounds.sqlBool()}, '$creationDate'," +
                    "${exactBirthday.sqlBool()}, $age, $brushingNumber)"
            )
        }

        private fun SupportSQLiteDatabase.insertProfileInSchemaVersion3(
            profile: ProfileInternal,
            id: Long = profile.id,
            firstName: String = profile.firstName,
            points: Int = profile.points,
            isOwnerProfile: Boolean = profile.isOwnerProfile,
            accountId: Int = profile.accountId,
            brushingTime: Int = profile.brushingTime,
            transitionSounds: Boolean = profile.transitionSounds,
            creationDate: String = profile.creationDate,
            exactBirthday: Boolean = profile.exactBirthday,
            age: Int = profile.age,
            brushingNumber: Int = profile.brushingNumber,
            needsUpdate: Boolean = profile.needsUpdate
        ) {
            execSQL(
                "INSERT INTO profiles " +
                    "(id, first_name, points, is_owner_profile, account," +
                    "brushing_goal_time, transition_sounds, created_at," +
                    "exact_birthday, age, brushing_number, needs_update)" +
                    "VALUES " +
                    "($id, '$firstName', $points, ${isOwnerProfile.sqlBool()}, $accountId, " +
                    "$brushingTime, ${transitionSounds.sqlBool()}, '$creationDate'," +
                    "${exactBirthday.sqlBool()}, $age, $brushingNumber, ${needsUpdate.sqlBool()})"
            )
        }

        private fun SupportSQLiteDatabase.fetchAccounts() =
            query("SELECT DISTINCT * FROM account", emptyArray())

        private fun SupportSQLiteDatabase.fetchProfiles() =
            query("SELECT DISTINCT * FROM profiles", emptyArray())

        private fun createAccountInternal(
            id: Long = defaultAccountId,
            ownerProfileId: Long = defaultProfileId,
            currentProfileId: Long? = defaultProfileId
        ) = AccountInternal(id, ownerProfileId, currentProfileId)

        private fun createProfileInternal(
            id: Long = defaultProfileId,
            firstName: String = FIRST_NAME,
            gender: String = GENDER,
            points: Int = POINTS,
            accountId: Long = defaultAccountId,
            brushingTime: Int = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
        ) = ProfileInternal(
            id = id,
            firstName = firstName,
            gender = gender,
            points = points,
            accountId = accountId.toInt(),
            birthday = birthday,
            age = getAgeFromBirthDate(birthday),
            brushingTime = brushingTime,
            creationDate = creationDate,
            brushingNumber = 0
        )
    }
}

private const val FIRST_NAME = "user1"
private const val GENDER = "M"
private const val POINTS = 10
private const val defaultAccountId = 101L
private const val defaultProfileId = 1235L
private val birthday = DateConvertersString().getLocalDateFromString("1990-02-04")!!
private const val creationDate = "1990-02-04T10:00:00+0000"
private const val REFRESH_TOKEN = "REFRESH_TOKEN"
private const val ACCESS_TOKEN = "ACCESS_TOKEN"
private const val TOKEN_EXPIRES = "1990-02-04"
private const val EMAIL_VERIFIED = true
private const val ALLOW_DATA_COLLECTING = true
private const val WEEKLY_DIGEST_SUBSCRIPTION = true
private const val BETA = true
