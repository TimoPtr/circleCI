/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.home.HomeScreenAnalytics
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentHumActivitiesBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class ActivitiesFragment : BaseMVIFragment<
    ActivitiesViewState,
    HomeScreenAction,
    ActivitiesViewModel.Factory,
    ActivitiesViewModel,
    FragmentHumActivitiesBinding>(), TrackableScreen {

    override fun getViewModelClass(): Class<ActivitiesViewModel> = ActivitiesViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_hum_activities

    override fun getScreenName(): AnalyticsEvent = HomeScreenAnalytics.activities()

    override fun execute(action: HomeScreenAction) {
        // TODO
    }
}
