/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import android.content.Context
import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.checkup.CheckupUtils.formatBrushingDate
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LastBrushingCardBindingModel(
    val data: LastBrushingCardViewState,
    override val layoutId: Int = R.layout.home_card_lastbrushing
) : DynamicCardBindingModel(data) {

    fun brushingDateDescription(context: Context, data: BrushingCardData): String = when {
        data.brushingDate != null ->
            formatBrushingDate(context, data.brushingDate, data.type)
        else -> context.getString(R.string.last_brushing_card_no_brushing)
    }

    fun deleteButtonVisibility(item: BrushingCardData) = if (item.isBrushingDay) {
        View.VISIBLE
    } else {
        View.GONE
    }

    fun coverage(item: BrushingCardData): Float? = if (item.isBrushingDay) item.coverage else null

    fun durationPercentage(item: BrushingCardData): Float? = if (item.isBrushingDay) item.durationPercentage else null

    fun durationSeconds(item: BrushingCardData): Long? =
        if (item.isBrushingDay) item.durationInSeconds else null

    fun showData(item: BrushingCardData): Boolean = item.isBrushingDay

    fun pulsingDotVisibility(): Int = if (data.pulsingDotVisible) View.VISIBLE else View.GONE
}
