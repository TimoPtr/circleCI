/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit.DAYS

/**
 * Created by guillaume agis on 18/5/18.
 * Constructor the weekly stat given a list of stats
 */
@Keep
@Parcelize
data class WeeklyStat(val statData: List<Stat>, val clock: @RawValue Clock) : Parcelable {

    @IgnoredOnParcel
    private val averageSurfaceCache = hashMapOf<Stat, Int>()

    // only processed when data is called
    @IgnoredOnParcel
    val data: List<DayStat> by lazy {
        calculateDayBrushings(statData)
    }

    // only processed when averageBrushingTime is called
    // get the averagBrushingTime
    @IgnoredOnParcel
    val averageBrushingTime: Int by lazy {
        calculateAverageBrushingtime(statData)
    }

    private fun calculateDayBrushings(statsData: List<Stat>): List<DayStat> {
        val dayStatsData = arrayOfNulls<DayStat>(DAY_COUNT)

        var kolibreeDay = toStartOfKolibreeDay(currentTime())

        val dayBrushingList = arrayListOf<Stat>()

        for (i in 0 until DAY_COUNT) {
            dayBrushingList.clear()

            for (stat in statsData) {
                if (dateOnCurrentDay(kolibreeDay, stat.date)) {
                    dayBrushingList.add(stat)
                }
            }
            dayStatsData[DAY_COUNT - 1 - i] = DayStat(
                dateTime = kolibreeDay,
                data = dayBrushingList.toList()
            )

            kolibreeDay = kolibreeDay.minusDays(1)
        }

        return dayStatsData.filterNotNull()
    }

    /**
     * Get average surface cleaned
     *
     * @return average surface cleaned
     */
    fun getAverageSurface(): Int {
        var count = 0
        var sum = 0

        for (day in data) {
            for (brushing in day.data.filter { it.hasProcessedData() }) {
                count++

                sum += brushing.surface()
            }
        }

        return if (count != 0) Math.round((sum / count).toFloat()) else 0
    }

    /**
     * Calculates the average brushing count, given a profile creation date
     *
     * For recently created profiles, it takes into account the creation date so that the user
     * doesn't get low averages just because he's new.
     *
     * Also, if there's a brushing older than the creation date, we take into account its date. This
     * is a feasible scenario due to Orphan brushings
     */
    fun getAverageBrushingCount(profileCreationDate: OffsetDateTime): Float {
        var count = 0f
        for (dayStat in data) {
            count += dayStat.count().toFloat()
        }

        val dayCount = nbOfDaysDividendToCalculateAverageBrushings(profileCreationDate)

        return Math.round(ROUND_AVERAGE_BY * count / dayCount) / ROUND_AVERAGE_BY
    }

    /**
     * Calculates the days math dividend to use when calculating average values
     *
     * Given a date, there are 3 possible scenarios
     *
     * - The profile is older than (now - DAY_COUNT), we return DAY_COUNT
     *
     * - The profile is newer than (now - DAY_COUNT) and no DayBrushing older than date has
     * brushings, we return the number days between now and date, which will always be < DAY_COUNT.
     * Since we need to take today into account for average calculations, we add 1 to the number of
     * days between now and the profile creation date
     *
     * - The profile is newer than (now - DAY_COUNT) and there's a DayBrushing older than date with
     * brushings, we return the number days between now and that DayBrushing, which will always
     * be < DAY_COUNT. Since we need to take today into account for average calculations, we add 1 to
     * the number of days between now and the profile creation date
     *
     * @return the number of days to use as math dividend. If the account was created today, it
     * returns 1
     */
    @VisibleForTesting
    fun nbOfDaysDividendToCalculateAverageBrushings(profileCreateDate: OffsetDateTime): Long {
        val profileCreationKolibreeDay = toStartOfKolibreeDay(profileCreateDate)

        val oldestKolibreeDayWithBrushings = toStartOfKolibreeDay(oldestDayWithStats())

        val relevantKolibreeDay = when {
            profileCreationKolibreeDay.isBefore(oldestKolibreeDayWithBrushings) -> profileCreationKolibreeDay
            else -> oldestKolibreeDayWithBrushings
        }

        val nowKolibreeDay = toStartOfKolibreeDay(currentTime())
        val daysToRelevantDate = abs(DAYS.between(nowKolibreeDay, relevantKolibreeDay)) + 1
        val adjustedDaysToOldestDate = min(DAY_COUNT.toLong(), daysToRelevantDate)
        return max(1L, adjustedDaysToOldestDate)
    }

    /**
     * @return the oldest LocalDateTime in the list of DayStat that contains at least 1 stat
     */
    fun oldestDayWithStats(): OffsetDateTime {
        var oldestDayWithStat = maxDateTime()
        for (dayStat in data) {
            if (!dayStat.isEmpty()) {
                if (dayStat.dateTime.isBefore(oldestDayWithStat)) {
                    oldestDayWithStat = dayStat.dateTime
                }
            }
        }

        return oldestDayWithStat
    }

    private fun calculateAverageBrushingtime(statData: List<Stat>): Int {
        val brushingCount = statData.size
        var sum = 0
        var i = 0
        val size = statData.size
        while (i < size) {
            sum += statData[i].duration.toInt()
            i++
        }
        return if (brushingCount != 0) sum / brushingCount else 0
    }

    private fun dateOnCurrentDay(
        currentTime: OffsetDateTime,
        brushingDateTime: OffsetDateTime
    ): Boolean {
        return !brushingDateTime.isBefore(currentTime) && brushingDateTime.isBefore(
            currentTime.plusDays(
                1
            )
        )
    }

    /**
     * Adjusts dateTime to the start of a kolibreeDay.
     */
    private fun toStartOfKolibreeDay(dateTime: OffsetDateTime): OffsetDateTime =
        dateTime.truncatedTo(DAYS).withHour(KOLIBREE_DAY_START_HOUR)

    private fun maxDateTime(): OffsetDateTime =
        OffsetDateTime.of(LocalDateTime.MAX, ZoneOffset.MAX)

    private fun currentTime(): OffsetDateTime =
        OffsetDateTime.now(clock)

    companion object {
        const val DAY_COUNT = 7

        private const val ROUND_AVERAGE_BY = 10F
    }
}
