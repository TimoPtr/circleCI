/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.ui.dialog.floatFromAttribute
import com.kolibree.android.app.ui.dialog.styleResFromAttribute
import com.kolibree.android.failearly.FailEarly

@Keep
@BindingAdapter("android:background")
fun View.setBackgroundColorCompat(@ColorRes color: Int) {
    try {
        setBackgroundColor(ContextCompat.getColor(context, color))
    } catch (e: Resources.NotFoundException) {
        FailEarly.fail(exception = e, message = "setBackgroundColorCompat failed for @ColorRes $color!")
    }
}

@Keep
@BindingAdapter("alphaAttr")
fun View.setAlphaAttr(@AttrRes attrId: Int) {
    alpha = floatFromAttribute(context, attrId)
}

@Keep
@BindingAdapter("textAppearance")
fun TextView.setTextAppearanceAttr(@AttrRes attrId: Int) {
    setTextAppearance(styleResFromAttribute(context, attrId))
}
