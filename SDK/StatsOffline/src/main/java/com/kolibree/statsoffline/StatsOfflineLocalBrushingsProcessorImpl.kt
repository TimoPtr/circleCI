/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.integrityseal.IntegritySeal
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.sumSessions
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.StatsPlaqueAggregate
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import timber.log.Timber

internal class StatsOfflineLocalBrushingsProcessorImpl @Inject internal constructor(
    private val statsOfflineDao: StatsOfflineDao,
    private val checkupCalculator: CheckupCalculator,
    private val statsOfflineFeatureToggle: StatsOfflineFeatureToggle,
    private val integritySeal: IntegritySeal
) : LocalBrushingsProcessor {

    /**
     * Should be invoked when a new Brushing has been created
     */
    override suspend fun onBrushingCreated(brushing: IBrushing) =
        onBrushingsCreated(listOf(brushing))

    /**
     * Should be invoked when a batch of Brushing has been created
     */
    override suspend fun onBrushingsCreated(brushings: List<IBrushing>) {
        if (statsOfflineFeatureToggle.value.not()) return
        if (brushings.isEmpty()) return

        integritySeal.validateIntegrity()

        val sessionStatsPerDay = mapAndGroupSessionsPerDay(brushings)

        val recalculatedMonths = updatedMonthsFromNewSessions(brushings, sessionStatsPerDay)
        val recalculatedWeeks = updatedWeeksFromNewSessions(brushings, sessionStatsPerDay)

        updateDatabase(recalculatedMonths, recalculatedWeeks)
    }

    @VisibleForTesting
    fun updateDatabase(
        monthsWithDayStats: List<MonthWithDayStats>,
        weeksWithDayStats: List<WeekWithDayStats>
    ) {
        val monthEntities = monthsWithDayStats.map { it.monthStats }

        val weekEntities = weeksWithDayStats.map { it.weekStats }

        val daysWithSessions =
            monthsWithDayStats.map { monthStats -> monthStats.sessions }.flatten()

        val dayEntities = daysWithSessions.map { it.dayStats }

        val sessionEntities = daysWithSessions.map { it.brushingSessions }.flatten()

        statsOfflineDao.update(
            monthEntities = monthEntities,
            weekEntities = weekEntities,
            dayEntities = dayEntities,
            brushingSessionEntities = sessionEntities
        )
    }

    override suspend fun onBrushingRemoved(brushing: IBrushing) {
        if (statsOfflineFeatureToggle.value.not()) return

        statsOfflineDao.removeBrushingSessionStat(brushing.toBrushingSessionStat())

        val date = brushing.dateTime.toLocalDate()
        val recalculatedMonthStats = statsOfflineDao.monthWithDays(
            profileId = brushing.profileId,
            month = YearMonth.from(date)
        )?.calculateAverage()

        val recalculatedWeekStats = statsOfflineDao.weekWithDays(
            profileId = brushing.profileId,
            week = date.toYearWeek()
        )?.calculateAverage()

        updateDatabase(
            monthsWithDayStats = recalculatedMonthStats?.let { listOf(it) } ?: listOf(),
            weeksWithDayStats = recalculatedWeekStats?.let { listOf(it) } ?: listOf()
        )
    }

    @VisibleForTesting
    fun mapAndGroupSessionsPerDay(brushings: List<IBrushing>): Map<LocalDate, List<BrushingSessionStatsEntity>> {
        return brushings
            .mapNotNull { brushing ->
                try {
                    brushing.toBrushingSessionStat()
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
            }
            .groupBy { it.assignedDate }
    }

    @VisibleForTesting
    fun updatedMonthsFromNewSessions(
        brushings: List<IBrushing>,
        sessionStatsPerDay: Map<LocalDate, List<BrushingSessionStatsEntity>>
    ): List<MonthWithDayStats> {
        if (brushings.isEmpty() || sessionStatsPerDay.isEmpty()) return listOf()

        val profileId = extractProfileId(brushings)

        return sessionStatsPerDay.keys
            .map { it.toYearMonth() }
            .distinct()
            .map {
                statsOfflineDao.getOrCreateMonthStats(
                    profileId,
                    it
                )
            }
            .map {
                sessionStatsPerDay
                    .filter { dateAndSessions -> dateAndSessions.key.toYearMonth() == it.month }
                    .map { it.key to it.value }
                    .fold(it) { monthStats, pair ->
                        monthStats.withNewSessions(pair.first, pair.second)
                    }
            }
            .groupBy { it.month }
            .map { it.value.sumSessions() }
            .map { it.calculateAverage() }
            .toList()
    }

    @VisibleForTesting
    fun updatedWeeksFromNewSessions(
        brushings: List<IBrushing>,
        sessionStatsPerDay: Map<LocalDate, List<BrushingSessionStatsEntity>>
    ): List<WeekWithDayStats> {
        if (brushings.isEmpty() || sessionStatsPerDay.isEmpty()) return listOf()

        val profileId = extractProfileId(brushings)

        return sessionStatsPerDay.keys
            .map { it.toYearWeek() }
            .distinct()
            .map {
                statsOfflineDao.getOrCreateWeekStats(
                    profileId,
                    it
                )
            }
            .map { weekAggregatedStats ->
                sessionStatsPerDay
                    .filter(entryBelongsToWeekStats(weekAggregatedStats))
                    .map { it.key to it.value }
                    .fold(weekAggregatedStats) { weekStats, pair ->
                        weekStats.withNewSessions(pair.first, pair.second)
                    }
            }
            .map { it.calculateAverage() }
    }

    private fun entryBelongsToWeekStats(
        weekAggregatedStats: WeekWithDayStats
    ): (Map.Entry<LocalDate, List<BrushingSessionStatsEntity>>) -> Boolean = { dateAndSessions ->
        weekAggregatedStats.accept(dateAndSessions.key)
    }

    @VisibleForTesting
    @Throws(IllegalArgumentException::class)
    fun extractProfileId(brushings: List<IBrushing>) =
        brushings.firstOrNull()?.profileId
            ?: throw IllegalArgumentException("brushings can't be empty")

    /**
     * Maps a IBrushing to BrushingSessionStatsEntity, deliberately ignoring timezone
     *
     * Check module's Readme for an explanation
     */
    @VisibleForTesting
    fun IBrushing.toBrushingSessionStat(): BrushingSessionStatsEntity {
        val checkupData = checkupCalculator.calculateCheckup(this)

        return BrushingSessionStatsEntity(
            profileId = profileId,
            creationTime = dateTime.toLocalDateTime(),
            duration = duration.toInt(),
            averageSurface = checkupData.surfacePercentage,
            _averageCheckupMap = checkupData.zoneSurfaceMap,
            cleanPercent = checkupData.plaqlessCheckupData?.cleanPercent,
            missedPercent = checkupData.plaqlessCheckupData?.missedPercent,
            plaqueLeftPercent = checkupData.plaqlessCheckupData?.plaqueLeftPercent,
            plaqueAggregate = checkupData.plaqueAggregateStats(),
            correctMovementAverage = checkupData.correctMovementAverage(),
            underSpeedAverage = checkupData.underSpeedAverage(),
            correctSpeedAverage = checkupData.correctSpeedAverage(),
            overSpeedAverage = checkupData.overSpeedAverage(),
            correctOrientationAverage = checkupData.correctOrientationAverage(),
            overPressureAverage = checkupData.overpressureAverage()
        )
    }
}

@VisibleForTesting
internal fun CheckupData.plaqueAggregateStats(): Map<MouthZone16, StatsPlaqueAggregate>? =
    plaqlessCheckupData?.plaqueAggregate?.mapValues {
        StatsPlaqueAggregate(
            status = it.value.plaqueStatus,
            cleannessPercent = it.value.cleannessPercentage.value()
        )
    }

@Suppress("SdkPublicExtensionMethodWithoutKeep")
internal val STATS_TAG = timberTagFor(StatsOfflineLocalBrushingsProcessorImpl::class)
