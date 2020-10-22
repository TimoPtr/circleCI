package com.kolibree.android.sba.testbrushing.results.card

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kolibree.android.sba.R

@SuppressLint("WrongConstant")
internal class ResultsCardAdapter(
    fm: FragmentManager,
    val mouthCoverageBody: String,
    val speedBody: String
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) = when (position) {
        ANALYSIS_SUCCESSFUL_POSITION -> AnalysisSuccessfulCardFragment()
        MOUTH_COVERAGE_POSITION -> MouthCoverageCardFragment.newInstance(mouthCoverageBody)
        SPEED_POSITION -> SpeedCardFragment.newInstance(speedBody)
        else -> throw IllegalStateException()
    }

    override fun getCount() = 3

    companion object {
        const val ANALYSIS_SUCCESSFUL_POSITION = 0
        const val MOUTH_COVERAGE_POSITION = 1
        const val SPEED_POSITION = 2
    }
}

internal class AnalysisSuccessfulCardFragment : ResultCardFragment() {
    override fun titleRes() = R.string.results_analysis_successful_title

    override fun body() = context?.getString(R.string.results_analysis_successful_body) ?: ""
}

internal interface ResultsCardHintListener {
    fun onMouthCoverageHintClick()
    fun onSpeedHintClick()
}

internal class MouthCoverageCardFragment : ResultCardFragment() {

    companion object {
        const val EXTRA_BODY = "EXTRA_BODY"

        fun newInstance(body: String): MouthCoverageCardFragment {
            val fragment = MouthCoverageCardFragment()
            val args = Bundle()
            args.putString(EXTRA_BODY, body)
            fragment.arguments = args
            return fragment
        }
    }

    override fun titleRes() = R.string.results_mouth_coverage_title

    override fun body() = arguments?.getString(EXTRA_BODY) ?: ""

    override fun hintRes() = R.string.results_mouth_coverage_hint

    override fun onHintClick() {
        val parent = parentFragment
        if (parent is ResultsCardHintListener) {
            parent.onMouthCoverageHintClick()
        }
    }
}

internal class SpeedCardFragment : ResultCardFragment() {

    companion object {
        const val EXTRA_BODY = "EXTRA_BODY"

        fun newInstance(body: String): SpeedCardFragment {
            val fragment = SpeedCardFragment()
            val args = Bundle()
            args.putString(EXTRA_BODY, body)
            fragment.arguments = args
            return fragment
        }
    }

    override fun titleRes() = R.string.speed_card_title

    override fun body() = arguments?.getString(MouthCoverageCardFragment.EXTRA_BODY) ?: ""

    override fun hintRes() = R.string.speed_card_hint

    override fun onHintClick() {
        val parent = parentFragment
        if (parent is ResultsCardHintListener) {
            parent.onSpeedHintClick()
        }
    }
}
