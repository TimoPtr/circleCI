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
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import com.kolibree.android.app.utils.setSpan
import com.kolibree.android.extensions.resolveAttribute
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel

@Parcelize
internal data class RewardYourselfItemBinding(
    val item: RewardYourselfItem
) : ItemBindingModel, Parcelable {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.binding, R.layout.item_reward_yourself)
    }

    fun formatPriceAndPoints(context: Context): Spanned {
        val price = item.price.formattedPrice()
        val points = item.price.smilePoints.toString()
        val priceAndPoints = context.getString(
            R.string.reward_yourself_card_item_price,
            price,
            points
        )

        return SpannableStringBuilder(priceAndPoints).apply {
            setSpan(price, context.appearanceSpan())
        }
    }

    private fun Context.appearanceSpan(): TextAppearanceSpan {
        return TextAppearanceSpan(this, resolveAttribute(R.attr.textAppearanceSubtitle2))
    }

    companion object {
        fun from(item: RewardYourselfItem): RewardYourselfItemBinding {
            return RewardYourselfItemBinding(item)
        }
    }
}
