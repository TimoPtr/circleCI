/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models.api

import androidx.annotation.Keep
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.statsoffline.models.DayAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.PeriodAggregatedStats
import com.kolibree.statsoffline.models.WeekAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.YearWeek
import io.reactivex.Flowable
import io.reactivex.Single
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

@Keep
interface AggregatedStatsRepository : Truncable {
    /**
     * Emit a [MonthAggregatedStatsWithSessions] with the aggregated stats for the given profile and [YearMonth]
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for the specified [YearMonth], the emitted [MonthAggregatedStatsWithSessions] will show
     * empty values
     *
     * [monthStats] operates by default on Schedulers.io() scheduler
     *
     * @param[profileId] the profile id
     * @param[month] the month to return
     * @return non-null [Single] that will emit a [MonthAggregatedStatsWithSessions]
     */
    fun monthStats(profileId: Long, month: YearMonth): Single<MonthAggregatedStatsWithSessions>

    /**
     * Emit a Set<[MonthAggregatedStatsWithSessions]> with the aggregated stats for the given profile and [months]
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for the specified [YearMonth], the emitted [MonthAggregatedStatsWithSessions] will show
     * empty values
     *
     * [monthStats] operates by default on Schedulers.io() scheduler
     *
     * @param[profileId] the profile id
     * @param[months] the months to return. Every [YearMonth] in the Set will have its equivalent in the emitted value
     * @return non-null [Single] that will emit a Set<[MonthAggregatedStatsWithSessions]>
     */
    fun monthStats(
        profileId: Long,
        months: Set<YearMonth>
    ): Single<Set<MonthAggregatedStatsWithSessions>>

    /**
     * Emit a Set<[MonthAggregatedStatsWithSessions]> with the aggregated stats for the given profile and [months]
     * whenever the database content corresponding to [months] changes
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for the specified [YearMonth], the emitted [MonthAggregatedStatsWithSessions] will show
     * empty values
     *
     * [monthStatsStream] operates by default on Schedulers.computation() scheduler
     *
     * @param[profileId] the profile id
     * @param[months] the months to return. Every [YearMonth] in the Set will have its equivalent in every emitted value
     * @return non-null [Flowable] that will emit a stream of Set<[MonthAggregatedStatsWithSessions]>
     */
    fun monthStatsStream(
        profileId: Long,
        months: Set<YearMonth>
    ): Flowable<Set<MonthAggregatedStatsWithSessions>>

    /**
     * Emit a Set<[WeekAggregatedStatsWithSessions]> with the aggregated stats for the given profile and [weeks]
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for the specified [YearWeek], the emitted [WeekAggregatedStatsWithSessions] will show
     * empty values
     *
     * [weekStats] operates by default on Schedulers.io() scheduler
     *
     * @param[profileId] the profile id
     * @param[weeks] the weeks to return. Every [YearWeek] in the Set will have its equivalent in the emitted value
     * @return non-null [Single] that will emit a Set<[WeekAggregatedStatsWithSessions]>
     */
    fun weekStats(
        profileId: Long,
        weeks: Set<YearWeek>
    ): Single<Set<WeekAggregatedStatsWithSessions>>

    /**
     * Emit a Set<[WeekAggregatedStatsWithSessions]> with the aggregated stats for the given profile and [weeks]
     * whenever the database content corresponding to [weeks] changes
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for the specified [YearWeek], the emitted [WeekAggregatedStatsWithSessions] will show
     * empty values
     *
     * [weekStatsStream] operates by default on Schedulers.computation() scheduler
     *
     * @param[profileId] the profile id
     * @param[weeks] the weeks to return. Every [YearWeek] in the Set will have its equivalent in every emitted value
     * @return non-null [Flowable] that will emit a stream of Set<[WeekAggregatedStatsWithSessions]>
     */
    fun weekStatsStream(
        profileId: Long,
        weeks: Set<YearWeek>
    ): Flowable<Set<WeekAggregatedStatsWithSessions>>

    /**
     * Emit a [DayAggregatedStatsWithSessions] with the aggregated stats for the given profile and [LocalDate]
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for the specified LocalDate, the emitted [DayAggregatedStatsWithSessions] will show empty
     * values
     *
     * [dayStats] operates by default on Schedulers.io() scheduler
     *
     * @return non-null [Single] that will emit a [DayAggregatedStatsWithSessions]
     */
    fun dayStats(profileId: Long, day: LocalDate): Single<DayAggregatedStatsWithSessions>

    /**
     * Emit a Set<[DayAggregatedStatsWithSessions]> with the aggregated stats for the given profile and [days]
     *
     * The data is precalculated, so reading it is very lightweight.
     *
     * If there's no data for a given [LocalDate], the emitted [DayAggregatedStatsWithSessions] will show empty values
     *
     * [dayStats] operates by default on Schedulers.io() scheduler
     *
     * @param[profileId] the profile id
     * @param[days] the months to return. Every [LocalDate] in the Set will have its equivalent in the emitted value
     * @return non-null [Single] that will emit a [DayAggregatedStatsWithSessions]
     */
    fun dayStats(profileId: Long, days: Set<LocalDate>): Single<Set<DayAggregatedStatsWithSessions>>

    /**
     * Emit a [PeriodAggregatedStats] instance with the aggregated stats for the given profile and date range, both
     * [startDate] and [endDate] included
     *
     * The data is calculated on the fly every time it's requested, so reading it is expensive. Prefer [monthStats] or
     * [dayStats], if possible.
     *
     * If there's no data for the specified date range, the emitted [PeriodAggregatedStats] will show empty values
     *
     * [periodStats] operates by default on Schedulers.io() scheduler
     *
     * @return non-null [Single] that will emit a [PeriodAggregatedStats]
     */
    fun periodStats(
        profileId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Single<PeriodAggregatedStats>
}
