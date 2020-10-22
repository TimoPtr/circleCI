/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DATETIME_FORMATTER_NO_ZONE
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.test.createSessionStatsEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDateTime

@RunWith(AndroidJUnit4::class)
internal class RemoveBrushingSessionGeneratedKeyMigrationTest :
    BaseRoomMigrationTest<StatsOfflineRoomAppDatabase>(
        StatsOfflineRoomAppDatabase::class,
        StatsOfflineRoomAppDatabase.DATABASE_NAME
    ) {

    @Test
    fun migration_drops_id_column() {
        val brushingSessionStat = createBrushingSessionStat(
            profileId = 1,
            creationTime = TrustedClock.getNowLocalDateTime()
        )

        initializeDatabaseWith(schemaVersion = 1) {
            insertStatInSchemaVersion1(brushingSessionStat)
            val cursor = fetchStats()
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.getColumnIndex("id"))
            cursor.moveToFirst()
            assertEquals(1L, cursor.longValueForColumn("id"))
        }

        runMigrationAndCheck(RemoveBrushingSessionGeneratedKeyMigration) {
            val cursor = fetchStats()
            assertEquals(1, cursor.count)
            assertEquals(-1, cursor.getColumnIndex("id"))
            cursor.moveToFirst()
            assertEquals(null, cursor.longValueForColumn("id"))
        }
    }

    @Test
    fun migration_deduplicates_objects_with_the_same_profile_id_and_creation_date() {
        val profileId = 1L
        val creationTime = TrustedClock.getNowLocalDate().atTime(12, 34, 56)
        val brushingSessionStat = createBrushingSessionStat(
            profileId = profileId,
            creationTime = creationTime
        )

        val checkStatAtPosition: (Int, Cursor) -> Unit = { position, cursor ->
            cursor.moveToPosition(position)
            assertEquals(profileId, cursor.longValueForColumn("profileId"))
            assertEquals(
                creationTime.format(DATETIME_FORMATTER_NO_ZONE),
                cursor.stringValueForColumn("creationTime")
            )
        }

        initializeDatabaseWith(schemaVersion = 1) {
            insertStatInSchemaVersion1(brushingSessionStat)
            insertStatInSchemaVersion1(brushingSessionStat)

            val cursor = fetchStats()
            assertEquals(2, cursor.count)

            0.until(2).forEach { checkStatAtPosition(it, cursor) }
        }

        runMigrationAndCheck(RemoveBrushingSessionGeneratedKeyMigration) {
            val cursor = fetchStats()
            assertEquals(1, cursor.count)

            checkStatAtPosition(0, cursor)
        }
    }

    @Test
    fun migration_keeps_data_for_multiple_profiles() {
        val firstProfileId = 1L
        val secondProfileId = 2L
        val creationTime = TrustedClock.getNowLocalDate().atTime(12, 34, 56)

        val firstProfileBrushingStat = createBrushingSessionStat(
            profileId = firstProfileId,
            creationTime = creationTime
        )

        val secondProfileBrushingStat = createBrushingSessionStat(
            profileId = secondProfileId,
            creationTime = creationTime
        )

        val checkStatAtPosition: (Int, Cursor, Long) -> Unit = { position, cursor, profileId ->
            cursor.moveToPosition(position)
            assertEquals(profileId, cursor.longValueForColumn("profileId"))
            assertEquals(
                creationTime.format(DATETIME_FORMATTER_NO_ZONE),
                cursor.stringValueForColumn("creationTime")
            )
        }

        initializeDatabaseWith(schemaVersion = 1) {
            insertStatInSchemaVersion1(firstProfileBrushingStat)
            insertStatInSchemaVersion1(secondProfileBrushingStat)

            val cursor = fetchStats()
            assertEquals(2, cursor.count)

            checkStatAtPosition(0, cursor, firstProfileId)
            checkStatAtPosition(1, cursor, secondProfileId)
        }

        runMigrationAndCheck(RemoveBrushingSessionGeneratedKeyMigration) {
            val cursor = fetchStats()
            assertEquals(2, cursor.count)

            checkStatAtPosition(0, cursor, firstProfileId)
            checkStatAtPosition(1, cursor, secondProfileId)
        }
    }

    companion object {

        private fun SupportSQLiteDatabase.insertStatInSchemaVersion1(stat: BrushingSessionStatsEntity) =
            with(stat) {
                execSQL(
                    "INSERT INTO brushing_session_stat " +
                        "(profileId, creationTime, duration, averageSurface, _averageCheckupMap," +
                        "assignedDate, cleanPercent, missedPercent, plaqueLeftPercent, plaqueAggregate)" +
                        "VALUES " +
                        "($profileId, '$creationTime', $duration, $averageSurface, '$_averageCheckupMap', " +
                        "$assignedDate, $cleanPercent, '$missedPercent', $plaqueLeftPercent, $plaqueAggregate)"
                )
            }

        private fun SupportSQLiteDatabase.fetchStats() =
            query("SELECT * FROM brushing_session_stat", emptyArray())

        private fun createBrushingSessionStat(
            profileId: Long,
            creationTime: LocalDateTime
        ) = createSessionStatsEntity(
            profileId = profileId,
            creationTime = creationTime,
            duration = MINIMUM_BRUSHING_GOAL_TIME_SECONDS.toLong(),
            averageSurface = 100
        )
    }
}
