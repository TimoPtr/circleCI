/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.home.HomeScreenAnalytics
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentProfileBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

internal class ProfileFragment : BaseMVIFragment<
    ProfileViewState,
    HomeScreenAction,
    ProfileViewModel.Factory,
    ProfileViewModel,
    FragmentProfileBinding>(), TrackableScreen, HasAndroidInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = fragmentInjector

    override fun getViewModelClass(): Class<ProfileViewModel> = ProfileViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultAnimator = binding.contentRecyclerview.itemAnimator as? DefaultItemAnimator
        defaultAnimator?.supportsChangeAnimations = false
    }

    override fun getScreenName(): AnalyticsEvent = HomeScreenAnalytics.profile()

    override fun execute(action: HomeScreenAction) {
        // no-op
    }
}
