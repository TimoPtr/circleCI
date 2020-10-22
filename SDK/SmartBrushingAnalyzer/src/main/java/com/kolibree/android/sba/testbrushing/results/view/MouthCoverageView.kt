package com.kolibree.android.sba.testbrushing.results.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kolibree.android.app.ui.widget.RingChartView
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.mouthmap.logic.MouthCoverageResult
import com.kolibree.android.sba.R
import com.kolibree.android.utils.TimeUtils
import java.text.NumberFormat

internal class MouthCoverageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Renderable<MouthCoverageResult> {

    init {
        initView()
    }

    private lateinit var coverageRing: RingChartView
    private lateinit var durationRing: RingChartView
    private lateinit var toothIcon: ImageView
    private lateinit var toothDescription: TextView
    private lateinit var coverageText: TextView
    private lateinit var durationText: TextView
    private var coveragePercentage = 0
    private var durationPercentage = 0

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.view_mouth_coverage, this, true)
        initViews(root)
    }

    private fun initViews(root: View) {
        toothIcon = root.findViewById(R.id.tooth)
        toothDescription = root.findViewById(R.id.tooth_description)
        coverageRing = root.findViewById(R.id.mouth_coverage_ring)
        durationRing = root.findViewById(R.id.duration_ring)
        coverageText = root.findViewById(R.id.coverage_value)
        durationText = root.findViewById(R.id.duration_value)
    }

    override fun onRender(result: MouthCoverageResult) {
        if (result.isPerfectCoverage()) {
            toothIcon.setImageResource(R.drawable.ic_clean_teeth)
            toothDescription.setText(R.string.results_mouth_coverage_tooth_clean)
        } else {
            toothIcon.setImageResource(R.drawable.ic_dirty_teeth)
            toothDescription.setText(R.string.results_mouth_coverage_tooth_dirty)
        }

        durationPercentage = durationPercentage(result.duration)
        durationRing.setRingCoverage(durationPercentage)
        durationRing.useDefaultColors(durationPercentage)

        coveragePercentage = result.coverage
        coverageRing.setRingCoverage(coveragePercentage)
        coverageRing.useDefaultColors(coveragePercentage)

        durationText.text = TimeUtils.getFormattedBrushingDuration(result.duration.toLong())
        coverageText.text = coveragePercentage(result.coverage)
    }

    override fun onViewVisible() {
        durationRing.animateRingCoverage(durationPercentage)
        coverageRing.animateRingCoverage(coveragePercentage)
    }

    override fun onViewInvisible() {
        durationRing.setRingCoverage(0)
        coverageRing.setRingCoverage(0)
    }

    private fun durationPercentage(duration: Int): Int {
        return (duration * 100 / DEFAULT_BRUSHING_GOAL)
    }

    private fun coveragePercentage(coverage: Int): String {
        val numberFormat = NumberFormat.getPercentInstance()
        return numberFormat.format((coverage / 100f).toDouble())
    }
}
