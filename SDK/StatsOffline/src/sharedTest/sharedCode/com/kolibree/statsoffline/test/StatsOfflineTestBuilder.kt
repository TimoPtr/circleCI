/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.test

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toKolibreeDay
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.PlaqlessCheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqueAggregate
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.dateRangeBetween
import com.kolibree.statsoffline.models.AverageCheckup
import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.DayAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity
import com.kolibree.statsoffline.roundOneDecimal
import com.kolibree.statsoffline.toYearWeek
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlin.random.Random
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.YearMonth

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

internal fun createSessionStatsEntity(
    creationTime: LocalDateTime = TrustedClock.getNowLocalDateTime(),
    profileId: Long = DEFAULT_PROFILE_ID,
    duration: Long = DEFAULT_DURATION,
    checkup: AverageCheckup = emptyAverageCheckup(),
    averageSurface: Int = 0,
    correctMovementAverage: Double = 0.0,
    underSpeedAverage: Double = 0.0,
    correctSpeedAverage: Double = 0.0,
    overSpeedAverage: Double = 0.0,
    correctOrientationAverage: Double = 0.0,
    overPressureAverage: Double = 0.0,
    assignedDate: LocalDate = creationTime.toKolibreeDay()
) = BrushingSessionStatsEntity(
    profileId = profileId,
    duration = duration.toInt(),
    creationTime = creationTime,
    _averageCheckupMap = checkup,
    averageSurface = averageSurface,
    correctMovementAverage = correctMovementAverage,
    underSpeedAverage = underSpeedAverage,
    correctSpeedAverage = correctSpeedAverage,
    overSpeedAverage = overSpeedAverage,
    correctOrientationAverage = correctOrientationAverage,
    overPressureAverage = overPressureAverage,
    assignedDate = assignedDate
)

internal fun createWeekWithDayStats(
    weekAggregatedStatsEntity: WeekAggregatedStatsEntity = createWeekAggregatedStatEntity(),
    dayStats: Map<LocalDate, DayWithSessions> = mapOf()
): WeekWithDayStats {
    val newDayStats = fullDayStatsForWeek(dayStats, weekAggregatedStatsEntity)

    return WeekWithDayStats(
        weekStats = weekAggregatedStatsEntity,
        dayStats = newDayStats
    )
}

internal fun fullDayStatsForWeek(
    dayStats: Map<LocalDate, DayWithSessions>,
    weekAggregatedStatsEntity: WeekAggregatedStatsEntity
): Map<LocalDate, DayWithSessions> {
    val newDayStats: MutableMap<LocalDate, DayWithSessions> = mutableMapOf()
    newDayStats.putAll(dayStats)

    weekAggregatedStatsEntity.dates.forEach { date ->
        if (!newDayStats.containsKey(date)) {
            newDayStats[date] =
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = date))
        }
    }

    return newDayStats.toMap()
}

internal fun createMonthWithDayStats(
    monthAggregatedStatsEntity: MonthAggregatedStatsEntity = createMonthAggregatedStatEntity(),
    dayStats: Map<LocalDate, DayWithSessions> = mapOf()
): MonthWithDayStats {
    val newDayStats = fullDayStatsForMonth(dayStats, monthAggregatedStatsEntity)

    return MonthWithDayStats(
        monthStats = monthAggregatedStatsEntity,
        dayStats = newDayStats
    )
}

private fun fullDayStatsForMonth(
    dayStats: Map<LocalDate, DayWithSessions>,
    brushingMonthStatEntity: MonthAggregatedStatsEntity
): Map<LocalDate, DayWithSessions> {
    val newDayStats: MutableMap<LocalDate, DayWithSessions> = mutableMapOf()
    newDayStats.putAll(dayStats)

    brushingMonthStatEntity.createEmptyDayStats()
        .groupBy { it.day }
        .filterNot { dayStats.containsKey(it.key) } // make sure we don't override any association specified as parameter
        .forEach {
            val date = it.key
            newDayStats[date] =
                createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = date))
        }

    return newDayStats.toMap()
}

internal fun createDayWithSessions(
    dayAggregatedEntity: DayAggregatedStatsEntity = createDayAggregatedStatsEntity(),
    sessions: List<BrushingSessionStatsEntity> = listOf()
) = DayWithSessions(
    dayStats = dayAggregatedEntity.copy(totalSessions = sessions.size),
    brushingSessions = sessions
)

internal fun createMonthAggregatedStatEntity(
    yearMonth: YearMonth = YearMonth.now(TrustedClock.systemClock()),
    profileId: Long = DEFAULT_PROFILE_ID,
    averageDuration: Double = 0.0,
    averageSurface: Double = 0.0,
    averageCheckup: AverageCheckup = emptyAverageCheckup(),
    totalSessions: Int = 0
) = MonthAggregatedStatsEntity(
    profileId = profileId,
    month = yearMonth,
    averageCheckup = averageCheckup,
    averageDuration = averageDuration,
    averageSurface = averageSurface,
    totalSessions = totalSessions
)

internal fun createWeekAggregatedStatEntity(
    yearWeek: YearWeek = YearWeek.now(),
    profileId: Long = DEFAULT_PROFILE_ID,
    averageDuration: Double = 0.0,
    averageSurface: Double = 0.0,
    averageCheckup: AverageCheckup = emptyAverageCheckup(),
    totalSessions: Int = 0
) = WeekAggregatedStatsEntity(
    profileId = profileId,
    week = yearWeek,
    averageCheckup = averageCheckup,
    averageDuration = averageDuration,
    averageSurface = averageSurface,
    totalSessions = totalSessions
)

internal fun createDayAggregatedStatsEntity(
    averageDuration: Double = 0.0,
    averageSurface: Double = 0.0,
    profileId: Long = DEFAULT_PROFILE_ID,
    day: LocalDate = TrustedClock.getNowLocalDate(),
    month: YearMonth = YearMonth.from(day),
    week: YearWeek = day.toYearWeek(),
    averageCheckup: AverageCheckup = emptyAverageCheckup(),
    totalSessions: Int = 0,
    correctMovementAverage: Double = 0.0,
    underSpeedAverage: Double = 0.0,
    correctSpeedAverage: Double = 0.0,
    overSpeedAverage: Double = 0.0,
    correctOrientationAverage: Double = 0.0,
    overPressureAverage: Double = 0.0
) = DayAggregatedStatsEntity(
    profileId = profileId,
    day = day,
    month = month,
    week = week,
    averageDuration = averageDuration,
    averageSurface = averageSurface,
    averageCheckup = averageCheckup,
    totalSessions = totalSessions,
    correctMovementAverage = correctMovementAverage,
    underSpeedAverage = underSpeedAverage,
    correctSpeedAverage = correctSpeedAverage,
    overSpeedAverage = overSpeedAverage,
    correctOrientationAverage = correctOrientationAverage,
    overPressureAverage = overPressureAverage
)

internal fun mockIBrushing(
    date: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
    duration: Long = DEFAULT_DURATION,
    processedData: String? = DEFAULT_PROCESSED_DATA,
    profileId: Long = DEFAULT_PROFILE_ID
): IBrushing {
    val brushing = mock<IBrushing>()

    whenever(brushing.duration).thenReturn(duration)
    whenever(brushing.processedData).thenReturn(processedData)
    whenever(brushing.profileId).thenReturn(profileId)
    whenever(brushing.dateTime).thenReturn(date)

    return brushing
}

internal fun mockCheckupData(
    surface: Int = DEFAULT_SURFACE,
    zoneCheckupMap: Map<MouthZone16, Float> = DEFAULT_ZONE_MAP,
    cleanPercent: Int? = null,
    missedPercent: Int? = null,
    plaqueLeftPercent: Int? = null,
    plaqueAggregate: Map<MouthZone16, PlaqueAggregate>? = null,
    correctMovementAverage: Double = 0.0,
    underSpeedAverage: Double = 0.0,
    correctSpeedAverage: Double = 0.0,
    overSpeedAverage: Double = 0.0,
    correctOrientationAverage: Double = 0.0,
    overPressureAverage: Double = 0.0
): CheckupData {
    val checkupData: CheckupData = mock()

    whenever(checkupData.surfacePercentage).thenReturn(surface)
    whenever(checkupData.zoneSurfaceMap).thenReturn(zoneCheckupMap)

    val plaqlessCheckupData = mockPlaqlessCheckupData(
        cleanPercent = cleanPercent,
        missedPercent = missedPercent,
        plaqueLeftPercent = plaqueLeftPercent,
        plaqueAggregate = plaqueAggregate
    )
    whenever(checkupData.plaqlessCheckupData).thenReturn(plaqlessCheckupData)

    whenever(checkupData.correctMovementAverage()).thenReturn(correctMovementAverage)
    whenever(checkupData.underSpeedAverage()).thenReturn(underSpeedAverage)
    whenever(checkupData.correctSpeedAverage()).thenReturn(correctSpeedAverage)
    whenever(checkupData.overSpeedAverage()).thenReturn(overSpeedAverage)
    whenever(checkupData.correctOrientationAverage()).thenReturn(correctOrientationAverage)
    whenever(checkupData.overpressureAverage()).thenReturn(overPressureAverage)

    return checkupData
}

internal fun mockPlaqlessCheckupData(
    cleanPercent: Int? = null,
    missedPercent: Int? = null,
    plaqueLeftPercent: Int? = null,
    plaqueAggregate: Map<MouthZone16, PlaqueAggregate>? = null
): PlaqlessCheckupData? {
    if (cleanPercent == null &&
        missedPercent == null &&
        plaqueLeftPercent == null &&
        plaqueAggregate == null
    ) {
        return null
    }

    val plaqlessCheckupData: PlaqlessCheckupData = mock()

    whenever(plaqlessCheckupData.plaqueAggregate).thenReturn(plaqueAggregate)
    whenever(plaqlessCheckupData.cleanPercent).thenReturn(cleanPercent)
    whenever(plaqlessCheckupData.missedPercent).thenReturn(missedPercent)
    whenever(plaqlessCheckupData.plaqueLeftPercent).thenReturn(plaqueLeftPercent)
    whenever(plaqlessCheckupData.plaqueAggregate).thenReturn(plaqueAggregate)

    return plaqlessCheckupData
}

internal fun createAverageCheckup(values: Map<MouthZone16, Float>): AverageCheckup {
    return emptyAverageCheckup().mapValues { values.getOrElse(it.key, { 0f }) }
}

internal fun createPeriodAggregatedStats(
    startDate: LocalDate,
    endDate: LocalDate,
    profileId: Long = DEFAULT_PROFILE_ID,
    averageDuration: Double = 0.0,
    averageSurface: Double = 0.0,
    averageCheckup: AverageCheckup = emptyAverageCheckup(),
    dayAggregatedStats: Set<DayWithSessions> = dateRangeBetween(
        startDate,
        endDate
    ).map { createDayWithSessions(dayAggregatedEntity = createDayAggregatedStatsEntity(day = it)) }.toSet()
): com.kolibree.statsoffline.models.PeriodAggregatedStats {
    return com.kolibree.statsoffline.models.PeriodAggregatedStats(
        dateRange = com.kolibree.statsoffline.models.SanitizedDateRange(
            startDate = startDate,
            endDate = endDate
        ),
        profileId = profileId,
        averageDuration = averageDuration,
        averageSurface = averageSurface,
        averageCheckup = averageCheckup,
        dayAggregatedStats = dayAggregatedStats
    )
}

internal fun mockDayWithSessions(
    averageSurface: Double = 0.0,
    averageDuration: Double = 0.0,
    averageCheckup: AverageCheckup = emptyAverageCheckup(),
    correctMovementAverage: Double = 0.0,
    underSpeedAverage: Double = 0.0,
    correctSpeedAverage: Double = 0.0,
    overSpeedAverage: Double = 0.0,
    correctOrientationAverage: Double = 0.0,
    overPressureAverage: Double = 0.0
): DayWithSessions {
    val dayWithSessions = mock<DayWithSessions>()
    whenever(dayWithSessions.averageSurface).thenReturn(averageSurface)
    whenever(dayWithSessions.averageDuration).thenReturn(averageDuration)
    whenever(dayWithSessions.averageCheckup).thenReturn(averageCheckup)
    whenever(dayWithSessions.correctMovementAverage).thenReturn(correctMovementAverage)
    whenever(dayWithSessions.underSpeedAverage).thenReturn(underSpeedAverage)
    whenever(dayWithSessions.correctSpeedAverage).thenReturn(correctSpeedAverage)
    whenever(dayWithSessions.overSpeedAverage).thenReturn(overSpeedAverage)
    whenever(dayWithSessions.correctOrientationAverage).thenReturn(correctOrientationAverage)
    whenever(dayWithSessions.overPressureAverage).thenReturn(overPressureAverage)

    whenever(dayWithSessions.calculateAverage()).thenReturn(dayWithSessions)
    return dayWithSessions
}

internal const val DEFAULT_DURATION = 90L
internal const val DEFAULT_PROCESSED_DATA = ""
internal const val DEFAULT_PROFILE_ID = 45L
internal const val DEFAULT_SURFACE = 0
internal val DEFAULT_ZONE_MAP: AverageCheckup = emptyAverageCheckup()

internal fun randomPercentageDouble(): Double = Random.nextDouble(from = 0.0, until = 100.0).roundOneDecimal()

internal fun randomPercentageInt(): Int = Random.nextInt(from = 0, until = 100)

internal fun randomPercentageLong(): Long = Random.nextLong(from = 0, until = 100).roundOneDecimal()
