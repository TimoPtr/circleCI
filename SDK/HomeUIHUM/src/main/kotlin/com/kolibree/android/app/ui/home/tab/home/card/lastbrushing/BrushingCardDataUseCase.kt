/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.ui.checkup.CheckupUtils.brushingType
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toCurrentTimeZone
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Flowable
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@Suppress("TooManyFunctions")
internal class BrushingCardDataUseCase @Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val brushingsRepository: BrushingsRepository,
    private val checkupCalculator: CheckupCalculator
) {
    private val dayOfWeekTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE")
    private val amOrPmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("a")

    fun load(): Flowable<List<BrushingCardData>> = currentProfileProvider.currentProfileFlowable()
        .switchMap { brushingsRepository.brushingsFlowable(it.id) }
        .map(::takeLastMonthBrushings)
        .map { brushings -> brushings.sortedByDescending { it.dateTime } }
        .map(::mapToBrushingCardData)
        .map(::updateSelection)

    @VisibleForTesting
    fun takeLastMonthBrushings(brushings: List<Brushing>): List<Brushing> {
        val monthAgo = TrustedClock.getNowOffsetDateTime().minusDays(DAYS_TO_SHOW)
        return brushings.filter { it.dateTime.isAfter(monthAgo) }
    }

    @VisibleForTesting
    fun mapToBrushingCardData(brushings: List<Brushing>): List<BrushingCardData> {
        val today = TrustedClock.getNowLocalDate()
        val endDate = endDate()
        return fillDatesWithBrushingCardData(brushings, today, endDate)
    }

    private fun fillDatesWithBrushingCardData(
        brushings: List<Brushing>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<BrushingCardData> {
        val data = mutableListOf<BrushingCardData>()

        var current = startDate
        while (shouldAddNextData(current, endDate, data.size)) {
            val brushingOnDate = brushingOnDate(current, brushings)
            when {
                brushingOnDate.isNotEmpty() -> data += toBrushingCardsData(brushingOnDate)
                isOlderThenProfile(current) -> data += dashedBrushingCardData(current)
                else -> data += noBrushingDay(current)
            }

            current = current.minusDays(1)
        }

        return data
    }

    @VisibleForTesting
    fun updateSelection(data: List<BrushingCardData>): List<BrushingCardData> {
        return data.mapIndexed { position: Int, brushingCardData: BrushingCardData ->
            brushingCardData.copy(isSelected = position == 0)
        }
    }

    @VisibleForTesting
    fun shouldAddNextData(current: LocalDate, endDate: LocalDate, dataSize: Int): Boolean {
        return when {
            current.isAfter(endDate) || current.isEqual(endDate) -> true
            else -> dataSize < MIN_BRUSHING_DATA
        }
    }

    @VisibleForTesting
    fun dashedBrushingCardData(date: LocalDate) = BrushingCardData(
        isManual = false,
        day = DATA_NOT_AVAILABLE,
        dayOfWeek = DATA_NOT_AVAILABLE,
        coverage = 0f,
        durationPercentage = 0f,
        date = date,
        brushingDate = null,
        type = BrushingType.None,
        colorMouthZones = mapOf(),
        durationInSeconds = 0L
    )

    @VisibleForTesting
    fun isOlderThenProfile(date: LocalDate): Boolean {
        val profileCreationDate = currentProfileProvider.currentProfile().getCreationDate()
        return date.isBefore(profileCreationDate.toLocalDate())
    }

    private fun toBrushingCardsData(brushing: List<Brushing>): List<BrushingCardData> =
        brushing.map(::toBrushingCardData)

    @VisibleForTesting
    fun brushingOnDate(current: LocalDate, brushings: List<Brushing>): List<Brushing> =
        brushings.filter {
            it.dateTime.toLocalDate() == current
        }

    @VisibleForTesting
    fun endDate(): LocalDate {
        val profileCreationDate =
            currentProfileProvider.currentProfile().getCreationDate().toLocalDate()
        val monthAgo = TrustedClock.getNowLocalDate().minusDays(DAYS_TO_SHOW - 1)
        return when {
            monthAgo.isAfter(profileCreationDate) -> monthAgo
            else -> profileCreationDate
        }
    }

    private fun noBrushingDay(date: LocalDate) = BrushingCardData(
        isManual = false,
        dayOfWeek = dayOfWeekTimeFormatter.format(date),
        day = withLeadingZero(date.dayOfMonth),
        coverage = 0f,
        durationPercentage = 0f,
        date = date,
        brushingDate = null,
        type = BrushingType.None,
        colorMouthZones = mapOf(),
        durationInSeconds = 0L
    )

    private fun toBrushingCardData(brushing: Brushing): BrushingCardData {
        val checkupData = checkupData(brushing)
        return BrushingCardData(
            isManual = checkupData.isManual,
            coverage = checkupData.coverage,
            durationPercentage = brushing.duration / brushing.goalDuration.toFloat(),
            dayOfWeek = brushingDayOfWeek(brushing),
            day = withLeadingZero(brushing.dateTime.dayOfMonth),
            date = brushing.dateTime.toLocalDate(),
            brushingDate = brushing.dateTime,
            type = brushingType(brushing.game),
            colorMouthZones = checkupData.zoneSurfaceMap,
            durationInSeconds = brushing.duration
        )
    }

    private fun checkupData(brushing: Brushing): CheckupData = checkupCalculator.calculateCheckup(
        processedData = brushing.processedData,
        timestampInSeconds = brushing.duration,
        duration = brushing.durationObject
    )

    @VisibleForTesting
    fun withLeadingZero(dayOfMonth: Int) = String.format(DAY_FORMAT, dayOfMonth)

    private fun brushingDayOfWeek(brushing: Brushing): String =
        brushing.dateTime.toCurrentTimeZone()
            .let { dayOfWeekTimeFormatter.format(it) + " " + amOrPmFormatter.format(it) }
}

internal const val DAYS_TO_SHOW = 30L
internal const val MIN_BRUSHING_DATA = 5L
internal const val DATA_NOT_AVAILABLE = "--"
internal const val DAY_FORMAT = "%02d"
