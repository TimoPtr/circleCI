package com.kolibree.android.guidedbrushing.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.Keep
import com.google.android.material.tabs.TabLayoutMediator
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.guidedbrushing.tips.R
import com.kolibree.android.guidedbrushing.tips.databinding.ActivityGuidedBrushingTipsBinding
import com.kolibree.android.guidedbrushing.ui.adapter.GuidedBrushingTipsAdapter
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class GuidedBrushingTipsActivity : BaseMVIActivity<
    GuidedBrushingTipsViewState,
    NoActions,
    GuidedBrushingTipsViewModel.Factory,
    GuidedBrushingTipsViewModel,
    ActivityGuidedBrushingTipsBinding>(),
    TrackableScreen {

    private val adapter = GuidedBrushingTipsAdapter()

    override fun getViewModelClass() = GuidedBrushingTipsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_guided_brushing_tips

    override fun getScreenName(): AnalyticsEvent = GuidedBrushingTipsAnalytics.main()

    override fun onBackPressed() = viewModel.close()

    override fun execute(action: NoActions) = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)

        setupAdapter()

        with(binding) {
            withWindowInsets(rootContentLayout) {
                toolbar.layoutParams = (toolbar.layoutParams as MarginLayoutParams).apply {
                    this.topMargin = topStatusBarWindowInset()
                }
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    private fun setupAdapter() {
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, _ ->
            tab.view.isClickable = false
        }.attach()
    }
}

@Keep
fun getGuidedBrushingTipsIntent(context: Context): Intent {
    return Intent(context, GuidedBrushingTipsActivity::class.java)
}
