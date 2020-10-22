package com.kolibree.statsoffline.models.api

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.statsoffline.models.DayAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.PeriodAggregatedStats
import com.kolibree.statsoffline.models.WeekAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

internal class AggregatedStatsRepositoryImpl
@Inject constructor(
    private val statsOfflineDao: StatsOfflineDao,
    @SingleThreadScheduler private val debounceScheduler: Scheduler
) : AggregatedStatsRepository {

    override fun weekStatsStream(
        profileId: Long,
        weeks: Set<YearWeek>
    ): Flowable<Set<WeekAggregatedStatsWithSessions>> {
        return statsOfflineDao.weeksUpdatedStream(profileId, weeks)
            .debounce(STREAM_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS, debounceScheduler)
            .subscribeOn(Schedulers.io())
            .flatMapSingle(
                { weekStatsSingle(profileId, weeks) },
                false,
                1
            )
    }

    override fun weekStats(
        profileId: Long,
        weeks: Set<YearWeek>
    ): Single<Set<WeekAggregatedStatsWithSessions>> {
        return weekStatsSingle(profileId, weeks)
            .subscribeOn(Schedulers.io())
    }

    @VisibleForTesting
    fun weekStatsSingle(
        profileId: Long,
        weeks: Set<YearWeek>
    ): Single<Set<WeekAggregatedStatsWithSessions>> {
        return Single.create { emitter ->
            try {
                val weeksWithDays = statsOfflineDao.weeksWithDays(profileId, weeks)

                val sanitizedWeeksWithDays = weeks.map { week ->
                    weeksWithDays.singleOrNull { it.week == week } ?: WeekWithDayStats.empty(
                        profileId,
                        week
                    )
                }.toSet()

                emitter.onSuccess(sanitizedWeeksWithDays)
            } catch (e: RuntimeException) {
                emitter.tryOnError(e)
            }
        }
    }

    /**
     * This method relies on MonthAggregatedStatsEntity recalculated each time a brushing is added
     * or removed
     */
    override fun monthStatsStream(
        profileId: Long,
        months: Set<YearMonth>
    ): Flowable<Set<MonthAggregatedStatsWithSessions>> {
        return statsOfflineDao.monthsUpdatedStream(profileId, months)
            .debounce(STREAM_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS, debounceScheduler)
            .flatMapSingle(
                { monthStatsSingle(profileId, months) },
                false,
                1
            )
    }

    override fun monthStats(
        profileId: Long,
        months: Set<YearMonth>
    ): Single<Set<MonthAggregatedStatsWithSessions>> {
        return monthStatsSingle(profileId, months)
            .subscribeOn(Schedulers.io())
    }

    @VisibleForTesting
    fun monthStatsSingle(
        profileId: Long,
        months: Set<YearMonth>
    ): Single<Set<MonthAggregatedStatsWithSessions>> {
        return Single.create { emitter ->
            try {
                val monthsWithDays = statsOfflineDao.monthsWithDays(profileId, months)

                val sanitizedMonthsWithDays = months.map { month ->
                    monthsWithDays.singleOrNull { it.month == month } ?: MonthWithDayStats.empty(
                        profileId,
                        month
                    )
                }.toSet()

                emitter.onSuccess(sanitizedMonthsWithDays)
            } catch (e: RuntimeException) {
                emitter.tryOnError(e)
            }
        }
    }

    override fun dayStats(
        profileId: Long,
        days: Set<LocalDate>
    ): Single<Set<DayAggregatedStatsWithSessions>> {
        return Single.create<Set<DayAggregatedStatsWithSessions>> { emitter ->
            try {
                val daysWithSessions = statsOfflineDao.daysWithSessions(profileId, days.toList())

                val sanitizedMonthsWithDays = days.map { day ->
                    daysWithSessions.singleOrNull { it.day == day } ?: DayWithSessions.empty(
                        profileId,
                        day
                    )
                }.toSet()

                emitter.onSuccess(sanitizedMonthsWithDays)
            } catch (e: RuntimeException) {
                emitter.tryOnError(e)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun monthStats(
        profileId: Long,
        month: YearMonth
    ): Single<MonthAggregatedStatsWithSessions> {
        return Single.create<MonthAggregatedStatsWithSessions> {
            try {
                val monthWithDayStats = statsOfflineDao.monthWithDays(profileId, month)

                if (monthWithDayStats == null) {
                    it.onSuccess(MonthWithDayStats.empty(profileId, month))
                } else {
                    it.onSuccess(monthWithDayStats)
                }
            } catch (e: RuntimeException) {
                it.tryOnError(e)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun dayStats(profileId: Long, day: LocalDate): Single<DayAggregatedStatsWithSessions> {
        return Single.create<DayAggregatedStatsWithSessions> {
            try {
                val dayWithSessions = statsOfflineDao.dayWithSessions(profileId, day)

                if (dayWithSessions == null) {
                    it.onSuccess(DayWithSessions.empty(profileId, day))
                } else {
                    it.onSuccess(dayWithSessions)
                }
            } catch (e: RuntimeException) {
                it.tryOnError(e)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun periodStats(
        profileId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Single<PeriodAggregatedStats> {
        return Single.create<PeriodAggregatedStats> {
            try {
                val periodDaysWithSessions =
                    statsOfflineDao.datePeriodDaysWithSessions(profileId, startDate, endDate)

                val periodAggregatedStats = PeriodAggregatedStats.fromDaysWithSessions(
                    profileId = profileId,
                    startDate = startDate,
                    endDate = endDate,
                    daysWithSessions = periodDaysWithSessions
                )

                it.onSuccess(periodAggregatedStats)
            } catch (e: RuntimeException) {
                it.tryOnError(e)
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun truncate(): Completable {
        return Completable.fromAction {
            statsOfflineDao.truncate()
        }.subscribeOn(Schedulers.io())
    }
}

private const val STREAM_DEBOUNCE_MILLIS = 300L
