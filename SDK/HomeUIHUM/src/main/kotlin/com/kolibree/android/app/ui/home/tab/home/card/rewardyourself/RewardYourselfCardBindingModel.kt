/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.utils.setSpan
import com.kolibree.android.extensions.resolveAttribute
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class RewardYourselfCardBindingModel(
    val viewState: RewardYourselfCardViewState,
    override val layoutId: Int = R.layout.home_reward_yourself
) : DynamicCardBindingModel(viewState) {

    fun items() = viewState.items

    fun body(context: Context): Spanned {
        val credits = viewState.userCredits.formattedPrice()

        val points = context.getString(
            R.string.reward_yourself_card_body_points,
            viewState.userCredits.smilePoints.toString()
        )

        val body = context.getString(
            R.string.reward_yourself_card_body,
            viewState.userName,
            points,
            credits
        )

        return SpannableStringBuilder(body).apply {
            setSpan(points, context.appearanceSpan())
            setSpan(credits, context.appearanceSpan())
        }
    }

    private fun Context.appearanceSpan(): TextAppearanceSpan {
        return TextAppearanceSpan(this, resolveAttribute(R.attr.textAppearanceSubtitle2))
    }
}
