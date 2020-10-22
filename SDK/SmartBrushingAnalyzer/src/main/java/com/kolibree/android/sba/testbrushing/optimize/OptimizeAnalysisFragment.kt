package com.kolibree.android.sba.testbrushing.optimize

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingFragment
import com.kolibree.android.sba.testbrushing.tracker.TestBrushingEventTracker
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_optimize_analysis.*

internal class OptimizeAnalysisFragment :
    LegacyBaseTestBrushingFragment<OptimizeAnalysisViewModel, OptimizeAnalysisViewState>() {

    @Inject
    lateinit var viewModelFactory: OptimizeAnalysisViewModel.Factory

    override fun layoutId() = R.layout.fragment_optimize_analysis

    override fun createViewModel(): OptimizeAnalysisViewModel {
        return ViewModelProviders.of(this, viewModelFactory)
            .get(OptimizeAnalysisViewModel::class.java)
    }

    private var colorSelected: Int = 0
    private var colorUnselected: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            colorSelected = ContextCompat.getColor(it, R.color.colorPrimary)
            colorUnselected = ContextCompat.getColor(it, R.color.black)
        }
        profile_info_left_hand.setOnClickListener { viewModel.onLeftHandClicked() }
        profile_info_right_hand.setOnClickListener { viewModel.onRightHandClicked() }
        iv_lower.setOnClickListener { viewModel.onReduceNbBrushing() }
        iv_highter.setOnClickListener { viewModel.onAddNbBrushing() }
        optimize_next.setOnClickListener { viewModel.onNextClicked() }

        selectHandness(profile_info_right_hand, profile_info_right_hand_text, colorUnselected)
        selectHandness(profile_info_left_hand, profile_info_left_hand_text, colorUnselected)

        Analytics.send(TestBrushingEventTracker.optimizeAnalysisScreen())
    }

    override fun render(viewState: OptimizeAnalysisViewState) {
        if (viewState.isFormValid())
            optimize_next.visibility = View.VISIBLE
        else
            optimize_next.visibility = View.GONE
        viewState.isLeftHanded?.let { isLeftHanded ->
            if (isLeftHanded) {
                isLeftHanded()
            } else {
                isRightHanded()
            }
        }

        nbBrushing.text = "${viewState.amountBrushing}"
    }

    private fun isLeftHanded() {
        selectHandness(profile_info_right_hand, profile_info_right_hand_text, colorUnselected)
        selectHandness(profile_info_left_hand, profile_info_left_hand_text, colorSelected)
    }

    private fun isRightHanded() {
        selectHandness(profile_info_right_hand, profile_info_right_hand_text, colorSelected)
        selectHandness(profile_info_left_hand, profile_info_left_hand_text, colorUnselected)
    }

    private fun selectHandness(imageView: ImageView, tv: TextView, color: Int) {
        imageView.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        tv.setTextColor(color)
    }
}
