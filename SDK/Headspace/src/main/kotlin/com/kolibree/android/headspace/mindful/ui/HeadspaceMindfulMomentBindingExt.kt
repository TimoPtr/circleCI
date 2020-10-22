/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.kolibree.android.failearly.FailEarly

@BindingAdapter(value = ["backgroundColorString", "backgroundColorDefault"], requireAll = true)
internal fun View.setBackgroundColorString(
    colorString: String,
    @ColorRes defaultColor: Int
) = setBackgroundColor(parseColorOrDefault(colorString, defaultColor, context))

@BindingAdapter(value = ["textColorString", "textColorDefault"], requireAll = true)
internal fun TextView.setTextColorString(
    colorString: String,
    @ColorRes defaultColor: Int
) = setTextColor(parseColorOrDefault(colorString, defaultColor, context))

@BindingAdapter(value = ["tintColorString", "tintColorDefault"], requireAll = true)
internal fun MaterialToolbar.setTextAndNavIconColorString(
    colorString: String,
    @ColorRes defaultColor: Int
) = with(parseColorOrDefault(colorString, defaultColor, context)) {
    setTitleTextColor(this)
    navigationIcon?.colorFilter =
        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(this, BlendModeCompat.SRC_IN)
}

private fun parseColorOrDefault(
    colorHexString: String,
    @ColorRes defaultColor: Int,
    context: Context
) = try {
    Color.parseColor(colorHexString)
} catch (e: IllegalArgumentException) {
    FailEarly.fail(
        exception = e,
        message = "Parsing color hexString($defaultColor) failed"
    )
    ContextCompat.getColor(context, defaultColor)
}
