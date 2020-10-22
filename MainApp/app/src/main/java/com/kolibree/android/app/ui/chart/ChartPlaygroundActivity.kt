/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.chart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.renderer.CombinedChartRenderer
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ColorTemplate
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.widget.chart.RoundedBarChartRenderer
import com.kolibree.android.app.ui.widget.chart.formatter.DayValueFormatter.Companion.indexOfDayOfWeek
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.tracker.NonTrackableScreen
import com.kolibree.databinding.ActivityChartPlaygroundBinding
import org.threeten.bp.DayOfWeek

internal class ChartPlaygroundActivity :
    BaseMVIActivity<
        ChartPlaygroundViewState,
        ChartPlaygroundActions,
        ChartPlaygroundViewModel.Factory,
        ChartPlaygroundViewModel,
        ActivityChartPlaygroundBinding>(),
    NonTrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // nice documentation can be found here https://weeklycoding.com/mpandroidchart-documentation/
        // and on the github repository there is a lot of samples
        initPieChart()
        initBarChart()
    }

    override fun getViewModelClass(): Class<ChartPlaygroundViewModel> = ChartPlaygroundViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_chart_playground

    override fun execute(action: ChartPlaygroundActions) {
        // no-op
    }

    private fun initPieChart() {
        // settings of the chart
        // TODO we can add attr to the chartView to put this in XML
        binding.piechart.description.isEnabled = false
        binding.piechart.isDrawHoleEnabled = false
        binding.piechart.isHighlightPerTapEnabled = false
        binding.piechart.legend.isEnabled = false
        binding.piechart.isRotationEnabled = false

        // nice animation when we draw the pie for the first time
        binding.piechart.animateY(1400, Easing.EaseInOutQuad)

        // The cool thing here is we don't have to compute anything we just pass the values we have
        // and the library will compute the % of each entry
        val entries = listOf(PieEntry(45.0f), PieEntry(55.0f))
        val dataSet = PieDataSet(entries, "testDataSet")
        // TODO there is an issue in the pieChart the space between the slide are not straight, we need to fix
        // that in a custom renderer probably
        dataSet.sliceSpace = 5f
        // color match the order of the entries
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.setDrawValues(false)
        val pieData = PieData(dataSet)

        binding.piechart.data = pieData
    }

    private fun initBarChart() {
        with(binding.combinedchart) {
            // use CombinedChart to display multiple kind of chart on the same view
            val combinedChartRenderer = CombinedChartRenderer(this, animator, viewPortHandler)
            combinedChartRenderer.subRenderers = listOf(
                // create a custom renderer to have rounded bar
                RoundedBarChartRenderer(
                    this,
                    animator,
                    viewPortHandler,
                    15
                ),
                LineChartRenderer(this, animator, viewPortHandler)
            )

            // xAxis.typeface // TODO set typeface base on the current one we might inject this with Dagger

            // set options of the leftAxis
            axisLeft.spaceTop = 50f

            // define the bar data to put in the graph
            // here an entry is define like this
            // x = index day of the week in the current local
            // y = current value that we want to plot
            val entries = listOf(
                BarEntry(indexOfDayOfWeek(DayOfWeek.SUNDAY).toFloat(), 1f),
                BarEntry(indexOfDayOfWeek(DayOfWeek.MONDAY).toFloat(), 2f),
                BarEntry(indexOfDayOfWeek(DayOfWeek.TUESDAY).toFloat(), 6f),
                BarEntry(indexOfDayOfWeek(DayOfWeek.WEDNESDAY).toFloat(), 5f),
                BarEntry(indexOfDayOfWeek(DayOfWeek.THURSDAY).toFloat(), 3f),
                BarEntry(indexOfDayOfWeek(DayOfWeek.SATURDAY).toFloat(), 2f)
            ).sortedBy { it.x } // sort to ensure that highlights works fine

            val barDataSet = BarDataSet(
                entries, "dataSet"
            )
            barDataSet.setDrawValues(false)
            val color = context.getColorFromAttr(R.attr.backgroundColor)
            barDataSet.setColors(color)
            barDataSet.highLightColor = context.getColorFromAttr(R.attr.colorSecondary)
            barDataSet.isHighlightEnabled = true
            val barData = BarData(listOf(barDataSet))

            // to draw a constant line on the graph we only need two entry in a LineDataSet
            val lineDataSet = LineDataSet(
                listOf(
                    Entry(-1f, 2.5f), // First day of week
                    Entry(7f, 2.5f) // Last day of week
                ), "max"
            )
            lineDataSet.setDrawCircles(false)
            lineDataSet.setDrawValues(false)
            lineDataSet.color = context.getColorFromAttr(R.attr.colorTertiaryMedium)
            lineDataSet.lineWidth = 1f

            val lineData = LineData(lineDataSet)
            // disable highlight => do nothing on click on the line
            lineData.isHighlightEnabled = false

            // put all the data in CombinedData
            val combinedData = CombinedData()
            combinedData.setData(barData)
            combinedData.setData(lineData)

            data = combinedData
            // renderer need to be set after setData because setData create default renderer
            renderer = combinedChartRenderer

            animateXY(2000, 2000)
        }
    }
}

@Keep
fun startChartPlaygroundIntent(context: Context) {
    context.startActivity(Intent(context, ChartPlaygroundActivity::class.java))
}
