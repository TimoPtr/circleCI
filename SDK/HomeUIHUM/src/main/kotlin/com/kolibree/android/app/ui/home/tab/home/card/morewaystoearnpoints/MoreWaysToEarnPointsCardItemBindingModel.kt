/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel

@Parcelize
internal data class MoreWaysToEarnPointsCardItemBindingModel(
    val challenge: EarnPointsChallenge,
    @DrawableRes val icon: Int,
    @StringRes val header: Int,
    @StringRes val body: Int
) : ItemBindingModel, Parcelable {

    @IgnoredOnParcel
    val points = challenge.points

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.binding, R.layout.item_more_ways_to_earn_points)
    }
}
