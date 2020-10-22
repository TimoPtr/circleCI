/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import android.content.Context
import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LifetimeStatsCardBindingModel(
    val data: LifetimeStatsCardViewState,
    override val layoutId: Int = R.layout.item_profile_card_lifetimestats
) : DynamicCardBindingModel(data) {

    fun showLoading() = data.isLoading

    fun formatCurrentPoints(context: Context): String {
        return context.getString(R.string.profile_stats_card_current_points, data.currentPoints)
    }

    fun formatLifetimePoints(context: Context): String {
        return if (data.shouldDisplayKeepEarningPoints) {
            context.getString(R.string.profile_stats_card_keep_earning_points)
        } else {
            context.getString(
                R.string.profile_stats_card_lifetime_points,
                data.lifetimePoints.toString()
            )
        }
    }

    fun formatOfflineCount(context: Context): String {
        val leadingZeroValue = data.offlineCount.withLeadingZero()

        return context.getString(R.string.profile_stats_card_offline_value, leadingZeroValue)
    }

    fun formatInAppCount(context: Context): String {
        val leadingZeroValue = data.inAppCount.withLeadingZero()

        return context.getString(R.string.profile_stats_card_inapp_value, leadingZeroValue)
    }

    private fun Int.withLeadingZero(): String {
        return String.format(BRUSHINGS_COUNT_FORMAT, this)
    }

    fun inAppStatsVisibity() = View.VISIBLE

    fun brushingsChartVisibility(): Int {
        val allBrushings = data.inAppCount + data.offlineCount
        val isVisible = allBrushings > 0
        return if (isVisible) View.VISIBLE else View.GONE
    }
}

private const val BRUSHINGS_COUNT_FORMAT = "%02d"
