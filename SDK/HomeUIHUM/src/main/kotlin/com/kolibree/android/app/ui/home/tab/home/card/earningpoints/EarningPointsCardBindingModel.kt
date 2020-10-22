/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class EarningPointsCardBindingModel(
    val data: EarningPointsCardViewState,
    override val layoutId: Int = R.layout.home_card_earning_points
) : DynamicCardBindingModel(data) {

    fun getSubtitle(context: Context) =
        context.resources.getQuantityString(
            R.plurals.earning_points_subtitle,
            data.pointsPerBrush,
            data.pointsPerBrush
        )

    fun getAnnotation(context: Context): SpannableString {
        val base = SpannableString(context.getString(R.string.earning_points_detail_annotation))
        val highlight =
            context.getString(R.string.earning_points_detail_annotation_highlight)

        val startIndex = base.indexOf(highlight)
        if (startIndex == -1) {
            FailEarly.fail("Cannot find highlight in the base string")
            return base
        }

        base.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            startIndex + highlight.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        base.setSpan(UnderlineSpan(), startIndex, startIndex + highlight.length, 0)

        return base
    }

    fun expandedVisibility() = if (data.expanded) View.VISIBLE else View.GONE
}
