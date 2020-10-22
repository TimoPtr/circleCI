/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.logic

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.calendar.logic.model.CalendarBrushingState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.calendar.di.CalendarApiModule
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.YearMonth

internal class CalendarBrushingsUseCaseImpl @Inject constructor(
    private val calendarBrushingsRepository: CalendarBrushingsRepository,
    private val aggregatedStatsRepository: AggregatedStatsRepository,
    private val brushingRepository: BrushingsRepository,
    private val kolibreeConnector: InternalKolibreeConnector,
    @Named(CalendarApiModule.CALCULATE_STREAKS_ON_THE_FLY) private val calculateOnTheFly: Boolean
) : CalendarBrushingsUseCase {

    @VisibleForTesting
    val today: LocalDate
        get() = LocalDate.now(TrustedClock.utcClock)

    @VisibleForTesting
    val currentMonth: YearMonth
        get() = YearMonth.now(TrustedClock.utcClock)

    // Simple cache to avoid unneeded API calls in corner cases
    @VisibleForTesting
    val queriesInProgress = mutableSetOf<Triple<Long, LocalDate, LocalDate>>()

    override fun getBrushingDateRange(
        profile: Profile
    ): Single<Pair<YearMonth, YearMonth>> {
        return calendarStartDateSingle(profile)
            .map { date -> YearMonth.from(date) }
            .map { month -> Pair(month, currentMonth) }
            .subscribeOn(Schedulers.io())
    }

    override fun getBrushingState(
        profile: Profile
    ): Flowable<CalendarBrushingState> = Flowable.defer {
        FailEarly.failIfExecutedOnMainThread()
        if (calculateOnTheFly) {
            getBrushingsForProfile(profile)
                .flatMap { brushings ->
                    calendarBrushingsRepository
                        .getStreaksForProfile(profileId = profile.id)
                        .map { CalendarBrushingState.from(brushings, it) }
                }
        } else {
            Flowable.combineLatest(
                getBrushingsForProfile(profile),
                calendarBrushingsRepository.getStreaksForProfile(profileId = profile.id),
                { brushings, streaks -> CalendarBrushingState.from(brushings, streaks) }
            )
        }
    }.subscribeOn(Schedulers.io())

    override fun maybeFetchBrushingsBeforeMonth(
        profile: Profile,
        month: YearMonth
    ): Completable {
        return Completable.defer {
            FailEarly.failIfExecutedOnMainThread()
            if (previousBrushingsAlreadyChecked(profile.id, month)) {
                Completable.complete()
            } else {
                fetchPreviousBrushings(profile, month)
            }
        }.subscribeOn(Schedulers.io())
    }

    @VisibleForTesting
    fun previousBrushingsAlreadyChecked(profileId: Long, month: YearMonth): Boolean {
        FailEarly.failIfExecutedOnMainThread()

        val fromDate = month.minusMonths(1).atDay(1)

        // Let's see if we have them in persistent storage
        val oldestLocalBrushingDate = getOldestLocalBrushingDate(profileId)

        if (oldestLocalBrushingDate.isBefore(fromDate)) {
            return true
        }

        return false
    }

    @VisibleForTesting
    fun fetchPreviousBrushings(
        profile: Profile,
        month: YearMonth
    ): Completable {
        FailEarly.failIfExecutedOnMainThread()

        val accountId = kolibreeConnector.currentAccount()?.id
            ?: return Completable.error(IllegalStateException("No account!"))

        synchronized(queriesInProgress) {
            val toDate = month.atEndOfMonth()
            val fromDate = toDate.minusMonths(1).withDayOfMonth(1)

            val queryTriple = Triple(profile.id, fromDate, toDate)
            @Suppress("ComplexCondition")
            if (queriesInProgress.contains(queryTriple) ||
                queriesInProgress.asSequence().any {
                    it.first == profile.id &&
                        it.second.isBefore(toDate) &&
                        it.third.isAfter(fromDate)
                }
            ) {
                return Completable.complete()
            }
            queriesInProgress += queryTriple

            return brushingRepository.fetchRemoteBrushings(
                accountId,
                profileId = profile.id,
                fromDate = fromDate,
                toDate = toDate
            ).ignoreElement()
                .doOnError {
                    synchronized(queriesInProgress) {
                        queriesInProgress -= queryTriple
                    }
                }.doOnComplete {
                    synchronized(queriesInProgress) {
                        queriesInProgress -= queryTriple
                    }
                }
        }
    }

    @VisibleForTesting
    fun getOldestLocalBrushingDate(profileId: Long): LocalDate {
        FailEarly.failIfExecutedOnMainThread()
        val oldestLocalBrushing = brushingRepository.getFirstBrushingSession(profileId = profileId)
        return oldestLocalBrushing?.dateTime?.toLocalDate() ?: TrustedClock.getNowLocalDate()
    }

    @VisibleForTesting
    fun getBrushingsForProfile(profile: Profile): Flowable<Set<MonthAggregatedStatsWithSessions>> {
        FailEarly.failIfExecutedOnMainThread()
        return aggregatedStatsRepository.monthStatsStream(
            profileId = profile.id,
            months = monthsBetween(calendarStartDate(profile), calendarEndDate(today))
        )
    }

    /**
     * Returns [calendarStartDate] for the current profile.
     */
    @VisibleForTesting
    fun calendarStartDateSingle(profile: Profile): Single<LocalDate> {
        return Single.just(profile).map(::calendarStartDate)
    }

    /**
     * Returns calendar start date - the date that will be displayed as a first day of the calendar.
     * This is always the first day of the month.
     * The month is determined based on first brushing date and profile creation date - we always take
     * the earlier date from those two.
     */
    @VisibleForTesting
    fun calendarStartDate(profile: Profile): LocalDate {
        val firstBrushingDate =
            brushingRepository.getFirstBrushingSession(profileId = profile.id)?.dateTime
        val profileCreationDate = profile.getCreationDate()
        val calendarStartDate =
            if (firstBrushingDate != null && firstBrushingDate.isBefore(profileCreationDate))
                firstBrushingDate
            else
                profileCreationDate
        return calendarStartDate.withDayOfMonth(1).toLocalDate()
    }

    @VisibleForTesting
    companion object {

        @VisibleForTesting
        fun monthsBetween(startDate: LocalDate, endDate: LocalDate): Set<YearMonth> {
            val months = mutableSetOf<YearMonth>()
            for (i in 0..Period.between(startDate, endDate).toTotalMonths()) {
                months += YearMonth.from(startDate.plusMonths(i))
            }
            return months
        }

        /**
         * Returns calendar end date - the date that will be displayed as a last day of the calendar.
         * This is always the last day of the current month.
         */
        @VisibleForTesting
        fun calendarEndDate(today: LocalDate): LocalDate = YearMonth.from(today).atEndOfMonth()
    }
}
