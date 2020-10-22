/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.homeui.hum.R

@VisibleForApp
class CoverageDurationProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var coverageView: CircleProgressView
    private lateinit var durationView: CircleProgressView

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_coverage_duration_progress, this, true)?.let { root ->
            coverageView = root.findViewById(R.id.coverage_progress)
            coverageView.setColor(context.getColorFromAttr(R.attr.colorTertiaryMedium))

            durationView = root.findViewById(R.id.duration_progress)
            durationView.setColor(context.getColorFromAttr(R.attr.colorSecondaryDark))
        }
    }

    fun setCoverageProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        coverageView.setProgress(progress)
    }

    fun setDurationProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        durationView.setProgress(progress)
    }

    fun getDuration() = durationView.progress

    fun getCoverage() = coverageView.progress
}

@BindingAdapter("coverage")
internal fun CoverageDurationProgressView.bindCoverage(progress: Float) {
    setCoverageProgress(progress)
}

@BindingAdapter("durationPercentage")
internal fun CoverageDurationProgressView.bindDuration(progress: Float) {
    setDurationProgress(progress)
}
