/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.binding

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

@Parcelize
internal data class BrushHeadConditionBindingModel(
    val headCondition: BrushHeadConditionState?,
    val lastReplacementDate: LocalDate?
) : ToothbrushSettingsItemBindingModel {

    @IgnoredOnParcel
    private val localizedDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_tb_settings_head_condition_item)
    }

    fun lastReplacementDate(context: Context): String = when {
        lastReplacementDate != null -> {
            val date = localizedDateFormatter.format(lastReplacementDate)
            context.getString(R.string.tb_settings_head_condition_last_replaced_date, date)
        }
        else -> context.getString(R.string.value_not_available)
    }

    @DrawableRes
    fun headConditionIcon(): Int {
        return headCondition?.iconRes ?: BrushHeadConditionState.NEEDS_REPLACEMENT.iconRes
    }

    fun headConditionDescription(context: Context): String = when {
        headCondition != null -> context.getString(headCondition.descriptionRes)
        else -> context.getString(R.string.value_not_available)
    }
}

internal enum class BrushHeadConditionState(
    @DrawableRes val iconRes: Int,
    @StringRes val descriptionRes: Int
) {
    GOOD(
        R.drawable.ic_brush_head_good,
        R.string.tb_settings_head_condition_good
    ),
    GETTING_OLDER(
        R.drawable.ic_brush_head_half,
        R.string.tb_settings_head_condition_getting_older
    ),
    NEEDS_REPLACEMENT(
        R.drawable.ic_brush_head_replace,
        R.string.tb_settings_head_condition_needs_replacing
    ),
}
