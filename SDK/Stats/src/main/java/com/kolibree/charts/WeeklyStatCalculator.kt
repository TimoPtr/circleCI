/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts

import androidx.annotation.VisibleForTesting
import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.charts.models.Stat
import com.kolibree.charts.models.WeeklyStat
import com.kolibree.charts.persistence.repo.StatRepository
import io.reactivex.Flowable
import javax.inject.Inject
import org.threeten.bp.Clock
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by guillaumeagis on 18/5/18.
 * Responsible for providing the weeklyStat for a given profile
 * @param statRepository repository where the stat are stored into
 * @param clock clock object, used to build the weeklyStat and to avoid tz issue.
 */
internal class WeeklyStatCalculator
@Inject constructor(
    private val statRepository: StatRepository,
    private val clock: Clock
) : DashboardCalculatorView {

    /**
     * Get the past weekly stat for a given profile for a given profileId
     * Return an empty Weekly profile if the profileId is null
     *
     * @param profileId the id of the profile to get the stat to
     * @return a Flowable with the past weekly stats
     */
    override fun getWeeklyStatForProfile(profileId: Long?): Flowable<WeeklyStat> {
        return profileId?.let {
            val endDate = currentTime()
            val startDate = adjustedStartDate(endDate)
            statRepository.getStatsSince(startDate, profileId)
                .map(this::createWeeklyStatFromBrushingData)
        } ?: Flowable.just(WeeklyStat(statData = emptyList(), clock = clock))
    }

    /**
     * Constructor a weekly stat given a liar of stats
     * @param stats list of stats used to build the weeklyStat
     */
    @VisibleForTesting
    fun createWeeklyStatFromBrushingData(stats: List<Stat>): WeeklyStat {
        return WeeklyStat(statData = stats, clock = clock)
    }

    /**
     * Calculates the week startDate from an endDate
     *
     * If endDate is after 4AM (Kolibree start time), we want to go back 6 days. Otherwise, we'll go
     * back 7 days
     *
     * Imagine the following scenarios. Both of them use 7 days.
     *
     * 1. Today is Sunday at 11:00 AM
     * · We want to calculate the averages including today's brushings, so we want to take into
     * account brushings starting from Monday's brushings (today - 6.days)
     *
     * 2. Today is Sunday at 3:00 AM
     * · We don't want to take into account today's brushings because from Kolibree's point of view,
     * today is still Saturday, so we need to take into account brushings starting from past Sunday
     * (today - 7.days)
     *
     *
     * @param endDate date to adjust to
     * @return a ZonedDateTime with the adjusted date
     */
    override fun adjustedStartDate(endDate: ZonedDateTime): ZonedDateTime {
        val endDateAtStartOfKolibreeDay = endDate.truncatedTo(ChronoUnit.DAYS)
            .withHour(KOLIBREE_DAY_START_HOUR)

        @Suppress("MagicNumber")
        return if (endDate.isAfter(endDateAtStartOfKolibreeDay)) {
            endDateAtStartOfKolibreeDay.minusDays(6)
        } else
            endDateAtStartOfKolibreeDay.minusDays(7)
    }

    @VisibleForTesting
    fun currentTime(): ZonedDateTime {
        return ZonedDateTime.now(clock)
    }
}
