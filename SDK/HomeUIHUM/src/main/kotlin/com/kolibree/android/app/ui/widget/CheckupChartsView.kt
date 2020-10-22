/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.extention.dimenFloat
import com.kolibree.android.app.ui.home.tab.view.CircleProgressView
import com.kolibree.android.app.ui.home.tab.view.bindColor
import com.kolibree.android.app.ui.home.tab.view.bindUpdateProgress
import com.kolibree.android.commons.DurationFormatter
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.homeui.hum.R
import kotlin.math.min

/** Hum Checkup metrics view */
@VisibleForApp
class CheckupChartsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    internal val formatter = DurationFormatter()

    internal val coverageChart: CircleProgressView

    internal val durationChart: CircleProgressView

    internal val coverageChartLegend: TextView

    internal val durationChartLegend: TextView

    init {
        orientation = HORIZONTAL
        View.inflate(context, R.layout.view_checkup_charts, this)
        coverageChart = findViewById(R.id.checkup_coverage_chart)
        durationChart = findViewById(R.id.checkup_duration_chart)
        coverageChartLegend = findViewById(R.id.checkup_coverage)
        durationChartLegend = findViewById(R.id.checkup_duration)
        setup()
    }

    private fun setup() {
        coverageChart.setProgressWidth(context.dimenFloat(R.dimen.dot))
        coverageChart.bindColor(context.getColorFromAttr(R.attr.colorTertiaryMedium))
        durationChart.setProgressWidth(context.dimenFloat(R.dimen.dot))
        durationChart.bindColor(context.getColorFromAttr(R.attr.colorSecondaryDark))
    }
}

@Keep
@BindingAdapter("coverage")
fun CheckupChartsView.bindCoverage(coverage: Float?) {
    coverage
        ?.let {
            coverageChart.bindUpdateProgress(it)
            coverageChartLegend.text = percentValue(it)
        }
        ?: run {
            coverageChart.bindUpdateProgress(0f)
            coverageChartLegend.text = "- %"
        }
}

@Keep
@BindingAdapter(value = ["durationPercentage", "durationSeconds"], requireAll = true)
@Suppress("MagicNumber")
fun CheckupChartsView.bindDuration(durationPercentage: Float?, durationSeconds: Long?) {
    durationPercentage
        ?.let {
            durationChart.bindUpdateProgress(min(1f, it))
            durationSeconds?.let {
                durationChartLegend.text = formatter.format(durationSeconds, false)
            }
        }
        ?: run {
            durationChart.bindUpdateProgress(0f)
            durationChartLegend.text = "-"
        }
}

@Suppress("MagicNumber")
private fun percentValue(progress: Float) = "${min(100, (100f * progress).toInt())}%"
