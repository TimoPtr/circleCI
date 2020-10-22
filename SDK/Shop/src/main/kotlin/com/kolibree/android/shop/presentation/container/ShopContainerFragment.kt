/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.container

import android.os.Bundle
import android.view.View
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.viewpager.TabFragmentController
import com.kolibree.android.shop.R
import com.kolibree.android.shop.databinding.FragmentContainerBinding
import com.kolibree.android.tracker.NonTrackableScreen

@VisibleForApp
class ShopContainerFragment : BaseMVIFragment<
    ShopContainerViewState,
    ShopContainerActions,
    ShopContainerViewModel.Factory,
    ShopContainerViewModel,
    FragmentContainerBinding>(),
    NonTrackableScreen {

    private lateinit var controller: TabFragmentController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = TabFragmentController(
            requireContext(),
            childFragmentManager,
            lifecycle,
            binding.pager,
            binding.tabLayout
        )
        controller.setTabs(
            if (viewModel.tabsEnabled) listOf(ShopTabs.PRODUCTS, ShopTabs.BRAND_DEALS)
            else listOf(ShopTabs.PRODUCTS)
        )
    }

    override fun getViewModelClass(): Class<ShopContainerViewModel> =
        ShopContainerViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_container

    override fun execute(action: ShopContainerActions) = when (action) {
        ShopContainerActions.SwitchToProductTab ->
            controller.setCurrentTab(ShopTabs.PRODUCTS, false)
    }
}
