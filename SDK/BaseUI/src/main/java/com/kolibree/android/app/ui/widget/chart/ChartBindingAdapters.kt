/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget.chart

import androidx.databinding.BindingAdapter
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.formatter.ValueFormatter

@BindingAdapter(value = ["xAxisValueFormatter"])
internal fun Chart<*>.xAxisValueFormatter(valueFormatter: ValueFormatter) {
    xAxis.valueFormatter = valueFormatter
}

@BindingAdapter(value = ["axisLeftValueFormatter"])
internal fun BarLineChartBase<*>.axisLeftValueFormatter(valueFormatter: ValueFormatter) {
    axisLeft.valueFormatter = valueFormatter
}

@BindingAdapter(value = ["axisRightValueFormatter"])
internal fun BarLineChartBase<*>.axisRightValueFormatter(valueFormatter: ValueFormatter) {
    axisLeft.valueFormatter = valueFormatter
}
