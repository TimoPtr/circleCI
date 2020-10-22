package com.kolibree.android.sba.testbrushing.results

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.mouthmap.widget.jaw.ResultsJawView
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.base.ShowAnalysisSuccessfulResult
import com.kolibree.android.sba.testbrushing.base.ShowAngleResult
import com.kolibree.android.sba.testbrushing.base.ShowMouthCoverageResult
import com.kolibree.android.sba.testbrushing.base.ShowReadDiagramDialog
import com.kolibree.android.sba.testbrushing.base.ShowSpeedHintDialog
import com.kolibree.android.sba.testbrushing.base.ShowSpeedResult
import com.kolibree.android.sba.testbrushing.base.ToggleJawsView
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingFragment
import com.kolibree.android.sba.testbrushing.results.card.ResultsCardAdapter
import com.kolibree.android.sba.testbrushing.results.card.ResultsCardHintListener
import com.kolibree.android.sba.testbrushing.results.view.AnalysisSuccessfulView
import com.kolibree.android.sba.testbrushing.results.view.AngleView
import com.kolibree.android.sba.testbrushing.results.view.MouthCoverageView
import com.kolibree.android.sba.testbrushing.results.view.Renderable
import com.kolibree.android.sba.testbrushing.results.view.SpeedView
import com.kolibree.android.sba.testbrushing.tracker.TestBrushingEventTracker
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject

internal class ResultsFragment :
    LegacyBaseTestBrushingFragment<ResultsViewModel, ResultsViewState>(),
    ResultsCardHintListener {
    @Inject
    internal lateinit var viewModelFactory: ResultsViewModel.Factory

    private lateinit var pager: ViewPager

    private lateinit var analysisSuccessfulView: AnalysisSuccessfulView
    private lateinit var mouthCoverageView: MouthCoverageView
    private lateinit var speedView: SpeedView
    private lateinit var angleView: AngleView
    private lateinit var resultsJawView: ResultsJawView
    private lateinit var tapToChangeHintView: View
    private lateinit var indicators: List<View>

    override fun layoutId() = R.layout.fragment_results

    override fun createViewModel(): ResultsViewModel {
        return ViewModelProviders.of(this, viewModelFactory).get(ResultsViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    override fun render(viewState: ResultsViewState) {
        renderIndicators(viewState.currentIndicator)
        renderViews(viewState)

        renderAction(viewState)

        renderResultsCardAdapter(viewState)

        renderChangeViewHint(viewState.isChangeViewHintVisible)
    }

    private fun renderChangeViewHint(isVisible: Boolean) {
        tapToChangeHintView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun renderResultsCardAdapter(viewState: ResultsViewState) {
        if (pager.adapter == null) {
            pager.adapter = ResultsCardAdapter(
                fm = childFragmentManager,
                mouthCoverageBody = viewState.mouthCoverageCardDescription,
                speedBody = viewState.speedCardDescription
            )
        }
    }

    private fun renderAction(viewState: ResultsViewState) {
        when (viewState.action) {
            is ShowReadDiagramDialog -> {
                ReadDiagramDialog.show(fragmentManager)
            }
            is ShowSpeedHintDialog -> {
                SpeedLearnMoreDialog.show(fragmentManager) {
                    viewModel.userClickNeedHelp()
                }
            }
            is ShowAnalysisSuccessfulResult -> {
                resultsJawView.colorMouthZones(viewState.results.successfulColorMouthZones)
                renderResultViews(analysisSuccessfulView, viewState.results)
                Analytics.send(TestBrushingEventTracker.resultsIntroScreen())
            }
            is ShowMouthCoverageResult -> {
                resultsJawView.colorMouthZones(viewState.results.coverageColorMouthZones)
                renderResultViews(mouthCoverageView, viewState.results)
                Analytics.send(TestBrushingEventTracker.resultsSurfaceScreen())
            }
            is ShowSpeedResult -> {
                resultsJawView.colorMouthZones(viewState.results.speedColorMouthZones)
                renderResultViews(speedView, viewState.results)
                Analytics.send(TestBrushingEventTracker.resultsSpeedScreen())
            }
            is ShowAngleResult -> {
                renderResultViews(angleView, viewState.results)
                Analytics.send(TestBrushingEventTracker.resultsAngleScreen())
            }
            is ToggleJawsView -> {
                resultsJawView.toggleJawsMode()
            }
        }
    }

    private fun renderResultViews(visibleView: Renderable<*>, results: BrushingResults) {
        analysisSuccessfulView.onRender(results)
        analysisSuccessfulView.onViewInvisible()

        mouthCoverageView.onRender(results)
        mouthCoverageView.onViewInvisible()

        speedView.onRender(results)
        speedView.onViewInvisible()

        angleView.onRender(results)
        angleView.onViewInvisible()

        visibleView.onViewVisible()
    }

    private fun renderViews(viewState: ResultsViewState) {
        renderView(analysisSuccessfulView, viewState.analysisSuccessfulAlpha)
        renderView(mouthCoverageView, viewState.mouthCoverageAlpha)
        renderView(speedView, viewState.speedAlpha)
        renderView(angleView, viewState.angleAlpha)
    }

    private fun renderView(view: View, alpha: Float) {
        view.visibility = if (alpha == 0f) View.INVISIBLE else View.VISIBLE
        view.alpha = alpha
    }

    private fun initViews(root: View) {
        initIndicatorsView(root)
        initPagerView(root)
        initResultsView(root)
        initResultsJawView(root)
        initHintsView(root)
        initDoneButtonView(root)
    }

    private fun initDoneButtonView(root: View) {
        root.findViewById<AppCompatImageView>(R.id.results_done).setOnDebouncedClickListener {
            viewModel.userClickDoneOnMenu()
        }
    }

    private fun initHintsView(root: View) {
        tapToChangeHintView = root.findViewById(R.id.tap_to_change_view)
        root.findViewById<View>(R.id.results_preview).setOnClickListener {
            viewModel.userClickTopScreen()
        }
    }

    private fun initResultsJawView(root: View) {
        resultsJawView = root.findViewById(R.id.results_jaw)
    }

    private fun initResultsView(root: View) {
        analysisSuccessfulView = root.findViewById(R.id.results_analysis_successful)
        mouthCoverageView = root.findViewById(R.id.results_mouth_coverage)
        speedView = root.findViewById(R.id.results_speed)
        angleView = root.findViewById(R.id.results_angle)
    }

    private fun initPagerView(root: View) {
        pager = root.findViewById(R.id.results_pager)
        pager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                viewModel.userSlideCards(position, positionOffset)
            }

            override fun onPageSelected(position: Int) {
                viewModel.userChangedCard(position)
            }
        })
    }

    private fun initIndicatorsView(root: View) {
        val indicator1 = root.findViewById<View>(R.id.results_indicator1)
        val indicator2 = root.findViewById<View>(R.id.results_indicator2)
        val indicator3 = root.findViewById<View>(R.id.results_indicator3)
        indicator3.visibility = View.VISIBLE

        indicators = listOf(indicator1, indicator2, indicator3)
    }

    private fun renderIndicators(selectedPosition: Int) {
        indicators.forEach {
            it.setBackgroundResource(R.drawable.shape_oval_white)
        }
        indicators[selectedPosition].setBackgroundResource(R.drawable.shape_oval_primary_color)
    }

    override fun onMouthCoverageHintClick() {
        viewModel.userClickMouthCoverageHint()
    }

    override fun onSpeedHintClick() {
        viewModel.userClickSpeedHint()
    }
}
