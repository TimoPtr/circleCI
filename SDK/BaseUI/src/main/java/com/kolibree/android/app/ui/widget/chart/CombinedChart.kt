/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget.chart

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.core.content.res.getFloatOrThrow
import androidx.core.content.res.getIntOrThrow
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.R

@VisibleForApp
class CombinedChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    CombinedChart(context, attrs, defStyle) {

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CombinedChart)
        try {
            with(styledAttributes) {
                zoom()
                legend()
                description()
                xAxisPosition()
                axisEnabled()
                drawAxisLines()
                drawGridLines()
                gridColor()
                granularity()
                labelCount()
                axisMinimum()
                axisMaximum()
            }
        } finally {
            styledAttributes.recycle()
        }
    }

    private fun TypedArray.zoom() {
        isDoubleTapToZoomEnabled = getBoolean(R.styleable.CombinedChart_doubleTapToZoomEnabled, false)
    }

    private fun TypedArray.legend() {
        legend.isEnabled = getBoolean(R.styleable.CombinedChart_legendEnabled, true)
    }

    private fun TypedArray.description() {
        val desc = getString(R.styleable.CombinedChart_description)
        description.isEnabled = !desc.isNullOrEmpty()
        description.text = desc
    }

    private fun TypedArray.xAxisPosition() {
        xAxis.position = XAxis.XAxisPosition.values()[getInt(
            R.styleable.CombinedChart_xAxisPosition,
            XAxis.XAxisPosition.BOTTOM.ordinal
        )]
    }

    private fun TypedArray.axisEnabled() {
        xAxis.isEnabled = getBoolean(R.styleable.CombinedChart_xAxisEnabled, true)
        axisLeft.isEnabled = getBoolean(R.styleable.CombinedChart_axisLeftEnabled, true)
        axisRight.isEnabled = getBoolean(R.styleable.CombinedChart_axisRightEnabled, true)
    }

    private fun TypedArray.drawAxisLines() {
        xAxis.setDrawAxisLine(getBoolean(R.styleable.CombinedChart_xAxisDrawAxisLine, true))
        axisLeft.setDrawAxisLine(getBoolean(R.styleable.CombinedChart_axisLeftDrawAxisLine, true))
        axisRight.setDrawAxisLine(getBoolean(R.styleable.CombinedChart_axisRightDrawAxisLine, true))
    }

    private fun TypedArray.drawGridLines() {
        xAxis.setDrawGridLines(getBoolean(R.styleable.CombinedChart_xAxisDrawGridLines, false))
        axisLeft.setDrawGridLines(getBoolean(R.styleable.CombinedChart_axisLeftDrawGridLines, false))
        axisRight.setDrawGridLines(getBoolean(R.styleable.CombinedChart_axisRightDrawGridLines, false))
    }

    private fun TypedArray.gridColor() {
        xAxis.axisLineColor = getColor(R.styleable.CombinedChart_xAxisGridColor, DEFAULT_GRID_COLOR)
        axisLeft.axisLineColor = getColor(R.styleable.CombinedChart_axisLeftGridColor, DEFAULT_GRID_COLOR)
        axisRight.axisLineColor = getColor(R.styleable.CombinedChart_axisRightGridColor, DEFAULT_GRID_COLOR)
    }

    private fun TypedArray.granularity() {
        if (hasValue(R.styleable.CombinedChart_xAxisGranularity)) {
            xAxis.granularity = getFloatOrThrow(R.styleable.CombinedChart_xAxisGranularity)
        }
        if (hasValue(R.styleable.CombinedChart_axisLeftGranularity)) {
            axisLeft.granularity = getFloatOrThrow(R.styleable.CombinedChart_axisLeftGranularity)
        }
        if (hasValue(R.styleable.CombinedChart_axisRightGranularity)) {
            axisRight.granularity = getFloatOrThrow(R.styleable.CombinedChart_axisRightGranularity)
        }
    }

    private fun TypedArray.labelCount() {
        if (hasValue(R.styleable.CombinedChart_xAxisLabelCount)) {
            xAxis.labelCount = getIntOrThrow(R.styleable.CombinedChart_xAxisLabelCount)
        }
        if (hasValue(R.styleable.CombinedChart_axisLeftLabelCount)) {
            axisLeft.labelCount = getIntOrThrow(R.styleable.CombinedChart_axisLeftLabelCount)
        }
        if (hasValue(R.styleable.CombinedChart_axisRightLabelCount)) {
            axisLeft.labelCount = getIntOrThrow(R.styleable.CombinedChart_axisRightLabelCount)
        }
    }

    private fun TypedArray.axisMinimum() {
        if (hasValue(R.styleable.CombinedChart_xAxisMinimum)) {
            xAxis.axisMinimum = getFloatOrThrow(R.styleable.CombinedChart_xAxisMinimum)
        }
        if (hasValue(R.styleable.CombinedChart_axisLeftMinimum)) {
            axisLeft.axisMinimum = getFloatOrThrow(R.styleable.CombinedChart_axisLeftMinimum)
        }
        if (hasValue(R.styleable.CombinedChart_axisRightMinimum)) {
            axisLeft.axisMinimum = getFloatOrThrow(R.styleable.CombinedChart_axisRightMinimum)
        }
    }

    private fun TypedArray.axisMaximum() {
        if (hasValue(R.styleable.CombinedChart_xAxisMaximum)) {
            xAxis.axisMaximum = getFloatOrThrow(R.styleable.CombinedChart_xAxisMaximum)
        }
        if (hasValue(R.styleable.CombinedChart_axisLeftMaximum)) {
            axisLeft.axisMaximum = getFloatOrThrow(R.styleable.CombinedChart_axisLeftMaximum)
        }
        if (hasValue(R.styleable.CombinedChart_axisRightMaximum)) {
            axisLeft.axisMaximum = getFloatOrThrow(R.styleable.CombinedChart_axisRightMaximum)
        }
    }
}

private const val DEFAULT_GRID_COLOR = -7829368
