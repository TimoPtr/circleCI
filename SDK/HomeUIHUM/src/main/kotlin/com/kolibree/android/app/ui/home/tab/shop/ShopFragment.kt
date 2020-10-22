/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.shop

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.home.HomeScreenAnalytics
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentShopBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

internal class ShopFragment :
    BaseMVIFragment<
        EmptyBaseViewState,
        HomeScreenAction,
        ShopViewModel.Factory,
        ShopViewModel,
        FragmentShopBinding>(),
    TrackableScreen, HasAndroidInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = fragmentInjector

    override fun getViewModelClass(): Class<ShopViewModel> = ShopViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_shop

    override fun getScreenName(): AnalyticsEvent = HomeScreenAnalytics.shop()

    override fun execute(action: HomeScreenAction) {
        // no-op
    }
}
