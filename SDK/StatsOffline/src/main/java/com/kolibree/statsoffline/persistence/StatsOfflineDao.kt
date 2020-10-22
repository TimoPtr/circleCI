/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Transaction
import com.kolibree.statsoffline.dateRangeBetween
import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.DayAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity
import io.reactivex.Flowable
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/**
 * Sanitized interface for dealing with StatsOffline database conditions
 *
 * - [BrushingSessionStatsEntity] belongs to a [DayAggregatedStatsEntity]
 * - [DayAggregatedStatsEntity] belongs to a [MonthAggregatedStatsEntity] and has [0-N] [BrushingSessionStatsEntity]
 * - [DayAggregatedStatsEntity] also belongs to a [WeekAggregatedStatsEntity]
 * - [MonthAggregatedStatsEntity] has N [DayAggregatedStatsEntity], one for each day of the month
 * - [WeekAggregatedStatsEntity] has [1-7] [DayAggregatedStatsEntity], depending on the week.
 *
 * Invoking any of the exposed methods leaves the DB in a proper state.
 *
 * All of the classes returned by the methods in the class are in UTC timezone
 */
@Dao
internal abstract class StatsOfflineDao {
    internal lateinit var sessionStatDao: BrushingSessionStatDao
    internal lateinit var dayStatDao: DayAggregatedStatsDao
    internal lateinit var weekStatDao: WeekAggregatedStatsDao
    internal lateinit var monthStatDao: MonthAggregatedStatsDao

    /**
     * Returns a [List] [DayWithSessions] instance with the DB content for the given profile and the
     * date period, both startDate and endDate included.
     *
     * Each [DayWithSessions] in the list refers to a unique date
     *
     * List will be empty if there's no data for the parameters
     *
     * @throws [IllegalArgumentException] if startDate isn't before endDate
     */
    fun datePeriodDaysWithSessions(
        profileId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DayWithSessions> {
        return daysWithSessions(profileId, dateRangeBetween(startDate, endDate))
    }

    /**
     * Returns a [List] [DayWithSessions] instance with the DB content for the given profile and [date]
     *
     * Each [DayWithSessions] in the list refers to a unique date
     *
     * List will be empty if there's no data for the parameters
     */
    fun daysWithSessions(profileId: Long, date: List<LocalDate>): List<DayWithSessions> {
        return dayStatDao.readByDate(profileId, date)
            .map { dayStats ->
                val daySessions = sessionStatDao.readByKolibreeDay(profileId, dayStats.day)

                DayWithSessions(dayStats, daySessions)
            }
    }

    /**
     * Returns a [List] [DayWithSessions] instance with the DB content for the given profile and [weeks]
     *
     * Each [DayWithSessions] in the list refers to a unique date
     *
     * List will be empty if there's no data for the parameters
     */
    fun daysWithSessionsFromWeeks(profileId: Long, weeks: List<YearWeek>): List<DayWithSessions> {
        return dayStatDao.readByWeeks(profileId, weeks)
            .map { dayStats ->
                val daySessions = sessionStatDao.readByKolibreeDay(profileId, dayStats.day)

                DayWithSessions(dayStats, daySessions)
            }
    }

    /**
     * @return a [DayWithSessions] instance with the DB content for the given profile and date
     *
     * null if there's no data for the combination of profileId and [LocalDate]
     */
    fun dayWithSessions(profileId: Long, day: LocalDate): DayWithSessions? {
        val dayStats = dayStatDao.readByDate(profileId, listOf(day))

        if (dayStats.isEmpty() || dayStats.size > 1) return null

        val daySessions = sessionStatDao.readByKolibreeDay(profileId, day)

        return DayWithSessions(dayStats.single(), daySessions)
    }

    /**
     * Given a profileId and [YearWeek], returns the existing [WeekWithDayStats] with all the associated data via
     * relationships
     *
     * If there isn't a existing [WeekWithDayStats], it creates and inserts a new one as well
     * as N [DayAggregatedStatsEntity], one for each day of the month
     */
    @Transaction
    open fun getOrCreateWeekStats(profileId: Long, week: YearWeek): WeekWithDayStats {
        return weekWithDays(profileId, week) ?: createWeekAndDays(profileId, week)
    }

    @VisibleForTesting
    fun createWeekAndDays(profileId: Long, week: YearWeek): WeekWithDayStats {
        val newWeekStats = WeekAggregatedStatsEntity(profileId, week)

        weekStatDao.insert(newWeekStats)

        return weekWithDays(profileId, week)
            ?: throw IllegalStateException("We just inserted content, shouldn't return null")
    }

    fun weekWithDays(profileId: Long, week: YearWeek): WeekWithDayStats? {
        return weekStatDao.readByWeek(profileId, week)?.let { weekStats ->
            val daysWithSessions = daysWithSessionsFromWeeks(profileId, listOf(weekStats.week))

            WeekWithDayStats(weekStats, daysWithSessions.associateBy { it.day })
        }
    }

    fun weeksWithDays(profileId: Long, weeks: Set<YearWeek>): Set<WeekWithDayStats> {
        return weekStatDao.readByWeek(profileId, weeks.toList()).map { weekStats ->
            val daysWithSessions = daysWithSessionsFromWeeks(profileId, listOf(weekStats.week))

            WeekWithDayStats(weekStats, daysWithSessions.associateBy { it.day })
        }.toSet()
    }

    /**
     * Given a profileId and [YearMonth], returns the existing [MonthWithDayStats] with all the associated data via
     * relationships
     *
     * If there isn't a existing [MonthWithDayStats], it creates and inserts a new one as well
     * as N [DayAggregatedStatsEntity], one for each day of the month
     */
    @Transaction
    open fun getOrCreateMonthStats(profileId: Long, month: YearMonth): MonthWithDayStats {
        return monthWithDays(profileId, month) ?: insertSanitizedEntities(profileId, month)
    }

    /**
     * Creates and inserts a new [MonthAggregatedStatsEntity] as well as
     * - P [WeekAggregatedStatsEntity], one for each week of the month
     * - N [DayAggregatedStatsEntity], one for each day of the month
     *
     * If any given entity with the same primary key existed, we won't replace it
     *
     * @return [MonthWithDayStats] with just created entities
     */
    @VisibleForTesting
    @Transaction
    open fun insertSanitizedEntities(
        profileId: Long,
        month: YearMonth
    ): MonthWithDayStats {
        val newMonthStats = MonthAggregatedStatsEntity(profileId, month)

        val newDayEntities = newMonthStats.createEmptyDayStats()
        val newWeekEntities = newMonthStats.createEmptyWeekStats()

        monthStatDao.insert(newMonthStats)

        weekStatDao.insert(newWeekEntities)

        dayStatDao.insert(newDayEntities)

        return monthWithDays(profileId, month)
            ?: throw IllegalStateException("We just inserted content, shouldn't return null")
    }

    /**
     * Returns a [MonthWithDayStats] instance with the DB content for the given profile and [YearMonth]
     *
     * @return null if there's no DB content for the parameters
     */
    fun monthWithDays(profileId: Long, month: YearMonth): MonthWithDayStats? {
        return monthStatDao.readByMonth(profileId, month)?.let { monthStats ->
            val dayWithSessions = daysWithSessions(profileId, monthStats.dates)

            MonthWithDayStats(monthStats, dayWithSessions.associateBy { it.day })
        }
    }

    /**
     * @return [Flowable]<[Boolean]> that will emit true whenever any of the [months] database
     * content is updated
     */
    fun monthsUpdatedStream(profileId: Long, months: Set<YearMonth>): Flowable<Boolean> {
        return monthStatDao.monthsUpdated(profileId, months.toList()).map { true }
    }

    /**
     * @return [Flowable]<[Boolean]> that will emit true whenever any of the [weeks] database
     * content is updated
     */
    fun weeksUpdatedStream(profileId: Long, weeks: Set<YearWeek>): Flowable<Boolean> {
        return weekStatDao.weeksUpdated(profileId, weeks.toList()).map { true }
    }

    /**
     * Returns a List<[MonthWithDayStats]> with the DB content for the given profile and N [months]
     *
     * @return a List<[MonthWithDayStats]> with the DB content for the parameters. The size will be `[0, N]`
     */
    fun monthsWithDays(profileId: Long, months: Set<YearMonth>): Set<MonthWithDayStats> {
        return monthStatDao.readByMonth(profileId, months.toList()).map { monthStats ->
            val dayWithSessions = daysWithSessions(profileId, monthStats.dates)

            MonthWithDayStats(monthStats, dayWithSessions.associateBy { it.day })
        }.toSet()
    }

    /**
     * Inserts [monthEntities], [weekEntities], [dayEntities] and [brushingSessionEntities]
     *
     * This is a transaction. Any failure while inserting will result on a rollback
     */
    @Transaction
    open fun update(
        monthEntities: List<MonthAggregatedStatsEntity>,
        weekEntities: List<WeekAggregatedStatsEntity>,
        dayEntities: List<DayAggregatedStatsEntity>,
        brushingSessionEntities: List<BrushingSessionStatsEntity>
    ) {

        monthStatDao.insert(monthEntities)

        weekStatDao.insert(weekEntities)

        dayStatDao.insert(dayEntities)

        sessionStatDao.insert(brushingSessionEntities)
    }

    open fun removeBrushingSessionStat(brushingSessionStat: BrushingSessionStatsEntity) {
        sessionStatDao.removeByCreationTime(
            brushingSessionStat.profileId,
            brushingSessionStat.creationTime
        )
    }

    @Transaction
    open fun truncate() {
        monthStatDao.truncate()
        weekStatDao.truncate()
        dayStatDao.truncate()
        sessionStatDao.truncate()
    }
}
