/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp

/** [ProgressBar] extension for metric charts */
@VisibleForApp
class MetricProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    init {
        max = MAX_PROGRESS_PERCENT
        isIndeterminate = false
    }

    // Makes the view a square with layout_width sides dimension
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    @VisibleForApp
    companion object {

        private const val MAX_PROGRESS_PERCENT = 100
    }
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("metricProgressDrawable")
fun ProgressBar.setMetricProgressDrawable(@DrawableRes metricProgressDrawable: Int) {
    // Won't work without the cast
    progressDrawable = ContextCompat.getDrawable(context, metricProgressDrawable) as RotateDrawable
}
