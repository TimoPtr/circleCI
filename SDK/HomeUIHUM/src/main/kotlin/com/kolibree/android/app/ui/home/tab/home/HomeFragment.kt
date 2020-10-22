/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.home.HomeScreenAnalytics
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterVisibilityUseCase
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentHomeBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject

internal class HomeFragment : BaseMVIFragment<
    HomeViewState,
    HomeScreenAction,
    HomeFragmentViewModel.Factory,
    HomeFragmentViewModel,
    FragmentHomeBinding>(), TrackableScreen {

    @Inject
    lateinit var smilesCounterVisibilityUseCase: SmilesCounterVisibilityUseCase

    override fun getViewModelClass(): Class<HomeFragmentViewModel> =
        HomeFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultItemAnimator = binding.contentRecyclerview.itemAnimator as? DefaultItemAnimator
        defaultItemAnimator?.supportsChangeAnimations = false

        smilesCounterVisibilityUseCase.setView(view.findViewById(R.id.shade_view))
    }

    override fun execute(action: HomeScreenAction) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = HomeScreenAnalytics.main()

    fun showSmileCounter(animate: Boolean) {
        val layoutManager = binding.contentRecyclerview.layoutManager as? LinearLayoutManager
        when {
            layoutManager?.findFirstCompletelyVisibleItemPosition() == 0 ->
                binding.appbar.setExpanded(true, animate)
            animate -> showSmileCounterWithAnimation()
            else -> showSmileCounterWithoutAnimation()
        }
    }

    private fun showSmileCounterWithoutAnimation() {
        with(binding.contentRecyclerview) {
            val layoutManager = layoutManager as? LinearLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (layoutManager?.findFirstCompletelyVisibleItemPosition() == 0) {
                        removeOnScrollListener(this)
                        binding.appbar.setExpanded(true, false)
                    }
                }
            })
            scrollToPosition(0)
        }
    }

    private fun showSmileCounterWithAnimation() {
        with(binding.contentRecyclerview) {
            val layoutManager = layoutManager as? LinearLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (layoutManager?.findFirstVisibleItemPosition() == 0) {
                        removeOnScrollListener(this)
                        binding.appbar.setExpanded(true, true)
                    }
                }
            })
            smoothScrollToPosition(0)
        }
    }
}
