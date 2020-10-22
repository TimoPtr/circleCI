package com.kolibree.android.sba.testbrushing.duringsession

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.kolibree.android.app.dialog.FinishBrushingDialog
import com.kolibree.android.app.ui.dialog.LostConnectionDialog
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.base.HideFinishBrushingDialog
import com.kolibree.android.sba.testbrushing.base.LostConnectionStateChanged
import com.kolibree.android.sba.testbrushing.base.ShowFinishBrushingDialog
import com.kolibree.android.sba.testbrushing.base.UpdateTimer
import com.kolibree.android.sba.testbrushing.base.ViewAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingFragment
import com.kolibree.android.sba.testbrushing.duringsession.TestBrushingDuringSessionViewModel.Factory
import com.kolibree.android.sba.testbrushing.duringsession.view.AnimatedImageView
import com.kolibree.android.sba.testbrushing.tracker.TestBrushingEventTracker
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_test_brushing_during_session.*

internal class TestBrushingDuringSessionFragment :
    LegacyBaseTestBrushingFragment<TestBrushingDuringSessionViewModel, TestBrushingDuringSessionViewState>() {

    @Inject
    internal lateinit var viewModelFactory: Factory

    override fun layoutId() = R.layout.fragment_test_brushing_during_session

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Analytics.send(TestBrushingEventTracker.startBrushing())
    }

    override fun render(viewState: TestBrushingDuringSessionViewState) {
        renderIndicator(viewState.indicatorStep, viewState.withIndicatorAnimation)
        renderDescription(viewState.descriptionId, viewState.highlightedId)
        renderAnimation(viewState.animationId, viewState.backgroundColorRes)
        renderTimerView(viewState.withTimerVisible)

        action(viewState.action)
    }

    private fun renderTimerView(isTimerVisible: Boolean) {
        timer_view.visibility = if (isTimerVisible) View.VISIBLE else View.GONE
    }

    private fun action(action: ViewAction) {
        when (action) {
            is HideFinishBrushingDialog -> {
                FinishBrushingDialog.hide(fragmentManager)
            }
            is ShowFinishBrushingDialog -> {
                FinishBrushingDialog.show(fragmentManager) { isBrushingFinished ->
                    if (isBrushingFinished) {
                        viewModel.userFinishedBrushing()
                        Analytics.send(TestBrushingEventTracker.brushingDone())
                    } else {
                        viewModel.userResumedBrushing()
                        Analytics.send(TestBrushingEventTracker.brushingResume())
                    }
                }
                Analytics.send(TestBrushingEventTracker.pauseBrushingDialog())
            }
            is LostConnectionStateChanged -> {
                LostConnectionDialog.update(fragmentManager, action.state) {
                    viewModel.onUserDismissedLostConnectionDialog()
                }
            }
            is UpdateTimer -> {
                timer_view.updateTime(action.elapsedTime)
            }
        }
    }

    private fun renderAnimation(animationId: Int, backgroundColorId: Int) {
        if (isFirstRun()) {
            animation1.setResource(animationId, backgroundColorId)
            animation2.translateToRight()
        } else if (animation1.shouldAnimate(animationId)) {
            startAnimation(animation1, animation2, animationId, backgroundColorId)
        } else if (animation2.shouldAnimate(animationId)) {
            startAnimation(animation2, animation1, animationId, backgroundColorId)
        }
    }

    private fun startAnimation(
        current: AnimatedImageView,
        next: AnimatedImageView,
        nextAnimationId: Int,
        backgroundColorId: Int
    ) {
        current.slideOut()
        next.setResource(nextAnimationId, backgroundColorId)
        next.slideIn()
    }

    private fun isFirstRun() = animation1.isOnScreen() && animation2.isOnScreen()

    private fun renderIndicator(step: Int, withAnimation: Boolean) {
        carousel_indicator.indicate(step, withAnimation)
    }

    override fun createViewModel(): TestBrushingDuringSessionViewModel {
        return ViewModelProviders.of(this, viewModelFactory)
            .get(TestBrushingDuringSessionViewModel::class.java)
    }

    private fun renderDescription(descriptionRes: Int, highlightedRes: Int) {
        val fullText = getString(descriptionRes)
        val highlightedText = getString(highlightedRes)
        val spannable = SpannableString(fullText)
        val startInx = fullText.indexOf(highlightedText, ignoreCase = true)
        if (startInx >= 0) {
            val endInx = startInx + highlightedText.length
            val color = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            spannable.setSpan(
                ForegroundColorSpan(color), startInx, endInx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        description_text.setText(spannable)
    }
}
