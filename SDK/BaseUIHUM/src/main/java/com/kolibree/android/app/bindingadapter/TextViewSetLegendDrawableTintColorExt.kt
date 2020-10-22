/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.bindingadapter

import android.graphics.drawable.InsetDrawable
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.baseui.hum.R

@Keep
@BindingAdapter("legendDrawableTintColor")
fun TextView.setLegendDrawableTintColor(@ColorInt color: Int) =
    AppCompatResources.getDrawable(context, R.drawable.ic_chart_legend_dot)?.let {
        val wrappedDrawable = DrawableCompat.wrap(it)
        DrawableCompat.setTint(wrappedDrawable, color)
        val bottomInset = context.resources.getDimensionPixelOffset(R.dimen.divider_thickness)
        val centeredDrawable = InsetDrawable(wrappedDrawable, 0, 0, 0, bottomInset)
        this.setCompoundDrawablesWithIntrinsicBounds(centeredDrawable, null, null, null)
    }
