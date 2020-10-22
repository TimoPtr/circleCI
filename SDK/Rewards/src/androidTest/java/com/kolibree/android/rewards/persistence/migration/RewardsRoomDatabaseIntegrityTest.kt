/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.persistence.migration

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.rewards.persistence.RewardsRoomDatabase
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringify
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyDuration
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyUnit
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.room.intValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZonedDateTime

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class RewardsRoomDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<RewardsRoomDatabase>(
        RewardsRoomDatabase::class,
        RewardsRoomDatabase.DATABASE_NAME,
        RewardsRoomDatabase.DATABASE_VERSION,
        *RewardsRoomDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }

    @Test
    fun migrationFrom1To2() {
        initializeDatabaseWith(schemaVersion = 1) {}
        runMigrationAndCheck(V2AddPersonalChallengeTableMigration) {}
    }

    @Test
    fun migrationFrom2To3() {
        initializeDatabaseWith(schemaVersion = 2) {}
        runMigrationAndCheck(V3RecreatePersonalChallengeTableMigration) {}
    }

    @Test
    fun migrationFrom1To3() {
        initializeDatabaseWith(schemaVersion = 2) {}
        runMigrationAndCheck(
            V2AddPersonalChallengeTableMigration,
            V3RecreatePersonalChallengeTableMigration
        ) {}
    }

    @Test
    fun personal_challenge_is_migrated() {
        val expectedProfileId1 = 4L
        val expectedProfileId2 = 5L

        val expectedId1 = 2L
        val expectedId2 = 3L

        val expectedBackendId = 3L
        val expectedProgress = 40

        val expectedCompletionDate = ZonedDateTime.now().minusMinutes(1)
        val expectedCreationDate = expectedCompletionDate.minusDays(3)
        val expectedUpdateDate = expectedCompletionDate.minusMinutes(3)

        val expectedPeriod = PersonalChallengePeriod.SEVEN_DAYS
        val expectedLevel = PersonalChallengeLevel.HARD
        val expectedType = PersonalChallengeType.COVERAGE

        val entity1 = PersonalChallengeEntity(
            id = expectedId1,
            backendId = expectedBackendId,
            profileId = expectedProfileId1,
            progress = expectedProgress,
            completionDate = expectedCompletionDate,
            creationDate = expectedCreationDate,
            updateDate = expectedUpdateDate,
            duration = expectedPeriod.stringifyDuration(),
            durationUnit = expectedPeriod.stringifyUnit(),
            difficultyLevel = expectedLevel.stringify(),
            objectiveType = expectedType.stringify()
        )

        val entity2 = PersonalChallengeEntity(
            id = expectedId2,
            backendId = expectedBackendId,
            profileId = expectedProfileId2,
            progress = expectedProgress,
            completionDate = expectedCompletionDate,
            creationDate = expectedCreationDate,
            updateDate = expectedUpdateDate,
            duration = expectedPeriod.stringifyDuration(),
            durationUnit = expectedPeriod.stringifyUnit(),
            difficultyLevel = expectedLevel.stringify(),
            objectiveType = expectedType.stringify()
        )

        initializeDatabaseWith(schemaVersion = 3) {
            insertPersonalChallengeInVersion3(entity1)
            insertPersonalChallengeInVersion3(entity2)

            val cursor = fetchPersonalChallenges()
            assertEquals(2, cursor.count)
            cursor.close()

            val entity1Cursor = fetchPersonalChallengeById(id = expectedId1)
            verifyEntity(entity1Cursor, entity1)
            assertEquals(-1, entity1Cursor.getColumnIndex("uuid"))
            entity1Cursor.close()

            val entity2Cursor = fetchPersonalChallengeById(id = expectedId2)
            verifyEntity(entity2Cursor, entity2)
            assertEquals(-1, entity1Cursor.getColumnIndex("uuid"))
            entity2Cursor.close()
        }
    }

    private fun verifyEntity(
        cursor: Cursor,
        entity: PersonalChallengeEntity
    ) {
        cursor.moveToFirst()

        assertEquals(entity.id, cursor.longValueForColumn("id"))
        assertEquals(entity.backendId, cursor.longValueForColumn("backendId"))
        assertEquals(entity.profileId, cursor.longValueForColumn("profileId"))
        assertEquals(entity.progress, cursor.intValueForColumn("progress"))

        assertEquals(entity.duration, cursor.longValueForColumn("duration"))
        assertEquals(entity.durationUnit, cursor.stringValueForColumn("durationUnit"))
        assertEquals(entity.difficultyLevel, cursor.stringValueForColumn("difficultyLevel"))
        assertEquals(entity.objectiveType, cursor.stringValueForColumn("objectiveType"))

        assertEquals(
            entity.completionDate.stringify(),
            cursor.stringValueForColumn("completionDate")
        )
        assertEquals(
            entity.creationDate.stringify(),
            cursor.stringValueForColumn("creationDate")
        )
        assertEquals(
            entity.updateDate.stringify(),
            cursor.stringValueForColumn("updateDate")
        )
    }

    private fun SupportSQLiteDatabase.insertPersonalChallengeInVersion3(entity: PersonalChallengeEntity) =
        with(entity) {
            execSQL(
                "INSERT OR REPLACE INTO `personal_challenges` (`id`,`backendId`,`profileId`,`objectiveType`,`difficultyLevel`,`duration`,`durationUnit`,`completionDate`, `creationDate`,`updateDate`,`progress`,`uploadStatus`,`isDeletedLocally`)" +
                    " VALUES " +
                    "($id, $backendId, $profileId, '$objectiveType', '$difficultyLevel', $duration, " +
                    "'$durationUnit', '${completionDate.stringify()}', '${creationDate.stringify()}', " +
                    "'${updateDate.stringify()}', $progress, '${UploadStatus.COMPLETED}', 0)"
            )
        }

    private fun SupportSQLiteDatabase.fetchPersonalChallenges() =
        query("SELECT * FROM personal_challenges", emptyArray())

    private fun SupportSQLiteDatabase.fetchPersonalChallengeById(id: Long) =
        query("SELECT * FROM personal_challenges WHERE id=$id", emptyArray())

    private val dateConverter = DateConvertersString()
    private fun ZonedDateTime?.stringify() = dateConverter.setZonedDateTimeToString(this)
}
