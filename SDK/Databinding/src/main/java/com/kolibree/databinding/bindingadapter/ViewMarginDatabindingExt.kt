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
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter

@Keep
@BindingAdapter(
    value = [
        "android:layout_marginLeft",
        "android:layout_marginTop",
        "android:layout_marginRight",
        "android:layout_marginBottom"
    ], requireAll = false
)
fun View.bindLegacyMargins(
    @DimenRes marginLeft: Int?,
    @DimenRes marginTop: Int?,
    @DimenRes marginRight: Int?,
    @DimenRes marginBottom: Int?
) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.let { marginParams ->
        marginParams.leftMargin = marginLeft?.fromDimen(resources) ?: marginParams.leftMargin
        marginParams.topMargin = marginTop?.fromDimen(resources) ?: marginParams.topMargin
        marginParams.rightMargin = marginRight?.fromDimen(resources) ?: marginParams.rightMargin
        marginParams.bottomMargin = marginBottom?.fromDimen(resources) ?: marginParams.bottomMargin
        this.layoutParams = marginParams
    }
}

@Keep
@BindingAdapter(
    value = [
        "android:layout_marginStart",
        "android:layout_marginTop",
        "android:layout_marginEnd",
        "android:layout_marginBottom"
    ], requireAll = false
)
fun View.bindMargins(
    @DimenRes marginStart: Int?,
    @DimenRes marginTop: Int?,
    @DimenRes marginEnd: Int?,
    @DimenRes marginBottom: Int?
) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.let { marginParams ->
        marginParams.marginStart = marginStart?.fromDimen(resources) ?: marginParams.marginStart
        marginParams.topMargin = marginTop?.fromDimen(resources) ?: marginParams.topMargin
        marginParams.marginEnd = marginEnd?.fromDimen(resources) ?: marginParams.marginEnd
        marginParams.bottomMargin = marginBottom?.fromDimen(resources) ?: marginParams.bottomMargin
        this.layoutParams = marginParams
    }
}

private inline fun Int.fromDimen(resources: Resources): Int = resources.getDimension(this).toInt()
