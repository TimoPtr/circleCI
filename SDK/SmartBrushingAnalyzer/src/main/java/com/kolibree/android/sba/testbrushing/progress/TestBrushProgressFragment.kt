package com.kolibree.android.sba.testbrushing.progress

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.base.FadeInAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingFragment
import com.kolibree.android.sba.testbrushing.tracker.TestBrushingEventTracker
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_test_brush_progress.*

internal class TestBrushProgressFragment :
    LegacyBaseTestBrushingFragment<TestBrushProgressViewModel, TestBrushProgressViewState>() {

    @Inject
    lateinit var viewModelFactory: TestBrushProgressViewModel.Factory

    private lateinit var fadeIn: Animation

    override fun layoutId() = R.layout.fragment_test_brush_progress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fadeIn = AlphaAnimation(0.3f, 1f).apply {
            duration = FADE_IN_DURATION
        }
        init()

        Analytics.send(TestBrushingEventTracker.analysisInProgressScreen())
    }

    override fun render(viewState: TestBrushProgressViewState) {
        renderUI(viewState)

        if (viewState.action is FadeInAction) {

            when (viewState.action.secOffset) {
                1 -> fadeIn(test_brushing_instruction_1)
                2 -> fadeIn(test_brushing_instruction_2)
                3 -> fadeIn(test_brushing_instruction_3)
                4 -> fadeIn(test_brushing_instruction_4)
            }
        }
    }

    private fun renderUI(viewState: TestBrushProgressViewState) {
        renderTextInstruction3(viewState)

        if (viewState.isProgressStep1Completed)
            stepCompleted(iv_sba_progress_1, test_brushing_instruction_1)
        if (viewState.isProgressStep2Completed)
            stepCompleted(iv_sba_progress_2, test_brushing_instruction_2)
        if (viewState.isProgressStep3Completed)
            stepCompleted(iv_sba_progress_3, test_brushing_instruction_3)
        if (viewState.isProgressStep4Completed)
            stepCompleted(iv_sba_progress_4, test_brushing_instruction_4)
    }

    private fun renderTextInstruction3(viewState: TestBrushProgressViewState) {
        val resTextInstruction3 = if (viewState.hasBuildUpStep) {
            R.string.pql_brushing_progress_msg
        } else {
            R.string.brushing_progress_msg_3
        }
        test_brushing_instruction_3.text = getString(resTextInstruction3)
    }

    private fun fadeIn(tv: TextView) {
        tv.startAnimation(fadeIn)
    }

    private fun init() {
        stepInit(iv_sba_progress_1, test_brushing_instruction_1)
        stepInit(iv_sba_progress_2, test_brushing_instruction_2)
        stepInit(iv_sba_progress_3, test_brushing_instruction_3)
        stepInit(iv_sba_progress_4, test_brushing_instruction_4)
    }

    private fun stepInit(iv: ImageView, tv: TextView) {
        iv.setImageResource(R.drawable.check_mark_disabled)
        tv.setTextColor(ContextCompat.getColor(tv.context, R.color.progress_step_disabled))
    }

    private fun stepCompleted(iv: ImageView, tv: TextView) {
        iv.setImageResource(R.drawable.check_mark_enabled)
        tv.setTextColor(ContextCompat.getColor(tv.context, R.color.black))
    }

    override fun createViewModel(): TestBrushProgressViewModel {
        return ViewModelProviders.of(this, viewModelFactory)
            .get(TestBrushProgressViewModel::class.java)
    }

    companion object {
        private const val FADE_IN_DURATION = 300L
    }
}
