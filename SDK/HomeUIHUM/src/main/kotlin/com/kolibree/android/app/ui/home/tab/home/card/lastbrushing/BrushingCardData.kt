/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import android.os.Parcelable
import com.kolibree.kml.MouthZone16
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime

@Parcelize
internal data class BrushingCardData(
    val isManual: Boolean,
    val coverage: Float?,
    val durationPercentage: Float,
    val day: String,
    val dayOfWeek: String,
    val position: Int = 0,
    val date: LocalDate,
    val brushingDate: OffsetDateTime?,
    val isSelected: Boolean = false,
    val type: BrushingType,
    val colorMouthZones: Map<MouthZone16, Float>,
    val durationInSeconds: Long
) : Parcelable {
    @IgnoredOnParcel
    val isClickable = day != DATA_NOT_AVAILABLE

    @IgnoredOnParcel
    val isDashedDay = day == DATA_NOT_AVAILABLE

    @IgnoredOnParcel
    val isEmptyDay = coverage == 0f && durationPercentage == 0f

    @IgnoredOnParcel
    val isBrushingDay = brushingDate != null

    companion object {
        fun empty() = BrushingCardData(
            isManual = false,
            coverage = null,
            durationPercentage = 0f,
            date = LocalDate.MIN,
            position = 0,
            dayOfWeek = "",
            day = "",
            brushingDate = null,
            isSelected = false,
            type = BrushingType.OfflineBrushing,
            colorMouthZones = mapOf(),
            durationInSeconds = 0L
        )
    }
}
