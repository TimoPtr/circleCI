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
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.room.DateConvertersLong
import com.kolibree.android.room.doubleValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase
import com.kolibree.statsoffline.persistence.YearMonthConverters
import com.kolibree.statsoffline.persistence.YearWeekConverters
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.DayAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createMonthAggregatedStatEntity
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.createWeekAggregatedStatEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

internal class AddOverPressureKpiColumnMigration4To5Test : BaseRoomMigrationTest<StatsOfflineRoomAppDatabase>(
    StatsOfflineRoomAppDatabase::class,
    StatsOfflineRoomAppDatabase.DATABASE_NAME
) {

    @Test
    fun brushing_stats_entity_hasOverPressureKpiAfterMigration() {
        val brushingSessionStat = createSessionStatsEntity(
            profileId = 1,
            creationTime = TrustedClock.getNowLocalDateTime()
        )

        fun verifyEntityRemainsUnchanged(cursor: Cursor) {
            assertEquals(brushingSessionStat.profileId, cursor.longValueForColumn("profileId"))
            assertEquals(
                brushingSessionStat.creationTime,
                DateConvertersLong().getLocalDateTimeUTCFromLong(cursor.longValueForColumn("creationTime"))
            )
        }

        initializeDatabaseWith(schemaVersion = 4) {
            insertBrushingSessionInSchemaVersion4(brushingSessionStat)
            val cursor = fetchBrushingSessions()
            assertEquals(1, cursor.count)
            assertEquals(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            verifyEntityRemainsUnchanged(cursor)
        }

        runMigrationAndCheck(AddOverPressureKpiColumnMigration4To5) {
            val cursor = fetchBrushingSessions()
            assertEquals(1, cursor.count)
            assertNotSame(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            assertEquals(0.0, cursor.doubleValueForColumn("overPressureAverage"))
            verifyEntityRemainsUnchanged(cursor)
        }
    }

    @Test
    fun month_stats_entity_hasOverPressureKpiAfterMigration() {
        val monthStatsEntity = createMonthAggregatedStatEntity()

        fun verifyEntityRemainsUnchanged(cursor: Cursor) {
            assertEquals(monthStatsEntity.profileId, cursor.longValueForColumn("profileId"))
            assertEquals(
                monthStatsEntity.month,
                YearMonthConverters().getYearMonthFrom(cursor.longValueForColumn("month")!!)
            )
        }

        initializeDatabaseWith(schemaVersion = 4) {
            insertMonthInSchemaVersion4(monthStatsEntity)
            val cursor = fetchMonths()
            assertEquals(1, cursor.count)
            assertEquals(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            verifyEntityRemainsUnchanged(cursor)
        }

        runMigrationAndCheck(AddOverPressureKpiColumnMigration4To5) {
            val cursor = fetchMonths()
            assertEquals(1, cursor.count)
            assertNotSame(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            assertEquals(0.0, cursor.doubleValueForColumn("overPressureAverage"))
            verifyEntityRemainsUnchanged(cursor)
        }
    }

    @Test
    fun week_stats_entity_hasOverPressureKpiAfterMigration() {
        val weekStatsEntity = createWeekAggregatedStatEntity()

        fun verifyEntityRemainsUnchanged(cursor: Cursor) {
            assertEquals(weekStatsEntity.profileId, cursor.longValueForColumn("profileId"))
            assertEquals(
                weekStatsEntity.week,
                YearWeekConverters().getYearWeekFrom(cursor.stringValueForColumn("week")!!)
            )
        }

        initializeDatabaseWith(schemaVersion = 4) {
            insertWeekInSchemaVersion4(weekStatsEntity)
            val cursor = fetchWeeks()
            assertEquals(1, cursor.count)
            assertEquals(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            verifyEntityRemainsUnchanged(cursor)
        }

        runMigrationAndCheck(AddOverPressureKpiColumnMigration4To5) {
            val cursor = fetchWeeks()
            assertEquals(1, cursor.count)
            assertNotSame(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            assertEquals(0.0, cursor.doubleValueForColumn("overPressureAverage"))
            verifyEntityRemainsUnchanged(cursor)
        }
    }

    @Test
    fun day_stats_entity_hasOverPressureKpiAfterMigration() {
        val dayStatsEntity = createDayAggregatedStatsEntity()

        fun verifyEntityRemainsUnchanged(cursor: Cursor) {
            assertEquals(dayStatsEntity.profileId, cursor.longValueForColumn("profileId"))
            assertEquals(
                dayStatsEntity.day,
                DateConvertersLong().getLocalDateFromLong(cursor.longValueForColumn("day")!!)
            )
            assertEquals(
                dayStatsEntity.week,
                YearWeekConverters().getYearWeekFrom(cursor.stringValueForColumn("week")!!)
            )
            assertEquals(
                dayStatsEntity.month,
                YearMonthConverters().getYearMonthFrom(cursor.longValueForColumn("month")!!)
            )
        }

        initializeDatabaseWith(schemaVersion = 4) {
            insertDayInSchemaVersion4(dayStatsEntity)
            val cursor = fetchDays()
            assertEquals(1, cursor.count)
            assertEquals(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            verifyEntityRemainsUnchanged(cursor)
        }

        runMigrationAndCheck(AddOverPressureKpiColumnMigration4To5) {
            val cursor = fetchDays()
            assertEquals(1, cursor.count)
            assertNotSame(-1, cursor.getColumnIndex("overPressureAverage"))

            cursor.moveToFirst()
            assertEquals(0.0, cursor.doubleValueForColumn("overPressureAverage"))
            verifyEntityRemainsUnchanged(cursor)
        }
    }
}

private fun SupportSQLiteDatabase.insertBrushingSessionInSchemaVersion4(stat: BrushingSessionStatsEntity) =
    with(stat) {
        execSQL(
            "INSERT INTO brushing_session_stat " +
                "(profileId, creationTime, duration, averageSurface, _averageCheckupMap," +
                "assignedDate, cleanPercent, missedPercent, plaqueLeftPercent, plaqueAggregate, " +
                "correctMovementAverage, underSpeedAverage, correctSpeedAverage, overSpeedAverage, correctOrientationAverage)" +
                "VALUES " +
                "($profileId, '${DateConvertersLong().setLocalDateTimeUTCToLong(creationTime)}', $duration, $averageSurface, '$_averageCheckupMap', " +
                "$assignedDate, $cleanPercent, '$missedPercent', $plaqueLeftPercent, $plaqueAggregate, " +
                "$correctMovementAverage, $underSpeedAverage, $correctSpeedAverage, $overSpeedAverage, $correctOrientationAverage" +
                ")"
        )
    }

private fun SupportSQLiteDatabase.insertMonthInSchemaVersion4(monthEntity: MonthAggregatedStatsEntity) =
    with(monthEntity) {
        execSQL(
            "INSERT INTO `brushing_month_stat` " +
                "(`profileId`,`month`,`averageDuration`,`averageSurface`,`averageCheckup`,`totalSessions`,`sessionsPerDay`," +
                "correctMovementAverage, underSpeedAverage, correctSpeedAverage, overSpeedAverage, correctOrientationAverage" +
                ") VALUES(" +
                "$profileId, ${YearMonthConverters().setYearMonthTo(month)}, $averageDuration, $averageSurface, '$averageCheckup', $totalSessions, $sessionsPerDay, " +
                "$correctMovementAverage, $underSpeedAverage, $correctSpeedAverage, $overSpeedAverage, $correctOrientationAverage" +
                ")"
        )
    }

private fun SupportSQLiteDatabase.insertWeekInSchemaVersion4(weekEntity: WeekAggregatedStatsEntity) =
    with(weekEntity) {
        execSQL(
            "INSERT INTO `brushing_week_stat` " +
                "(`profileId`,`week`,`averageDuration`,`averageSurface`,`averageCheckup`,`totalSessions`,`sessionsPerDay`, " +
                "correctMovementAverage, underSpeedAverage, correctSpeedAverage, overSpeedAverage, correctOrientationAverage" +
                ") VALUES(" +
                "$profileId, '${YearWeekConverters().setYearWeekTo(week)}', $averageDuration, $averageSurface, '$averageCheckup', $totalSessions, $sessionsPerDay, " +
                "$correctMovementAverage, $underSpeedAverage, $correctSpeedAverage, $overSpeedAverage, $correctOrientationAverage" +
                ")"
        )
    }

private fun SupportSQLiteDatabase.insertDayInSchemaVersion4(dayEntity: DayAggregatedStatsEntity) =
    with(dayEntity) {
        execSQL(
            "INSERT INTO `brushing_day_stat` " +
                "(`profileId`,`day`,`averageDuration`,`averageSurface`,`averageCheckup`,`isPerfectDay`,`totalSessions`,`month`,`week`, " +
                "correctMovementAverage, underSpeedAverage, correctSpeedAverage, overSpeedAverage, correctOrientationAverage" +
                ") VALUES(" +
                "$profileId, ${DateConvertersLong().setLocalDateToLong(day)}, $averageDuration, $averageSurface, '$averageCheckup', ${if (isPerfectDay) 1 else 0}, $totalSessions, " +
                "${YearMonthConverters().setYearMonthTo(month)}, " +
                "'${YearWeekConverters().setYearWeekTo(week)}', " +
                "$correctMovementAverage, $underSpeedAverage, $correctSpeedAverage, $overSpeedAverage, $correctOrientationAverage" +
                ")"
        )
    }

private fun SupportSQLiteDatabase.fetchBrushingSessions() =
    query("SELECT * FROM brushing_session_stat", emptyArray())

private fun SupportSQLiteDatabase.fetchMonths() =
    query("SELECT * FROM brushing_month_stat", emptyArray())

private fun SupportSQLiteDatabase.fetchWeeks() =
    query("SELECT * FROM brushing_week_stat", emptyArray())

private fun SupportSQLiteDatabase.fetchDays() =
    query("SELECT * FROM brushing_day_stat", emptyArray())
