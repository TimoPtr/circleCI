/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.intro

import androidx.navigation.findNavController
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlAnalytics
import com.kolibree.android.app.mvi.intro.GameIntroFragment
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.tracker.AnalyticsEvent

internal class SpeedControlIntroFragment : GameIntroFragment() {

    override fun resourcesProvider() =
        SpeedControlIntroResourceProvider

    override fun openBrushScreen() {
        activity?.findNavController(R.id.nav_host_fragment)
            ?.navigateSafe(R.id.action_intro_to_brushStart)
    }

    override fun getScreenName(): AnalyticsEvent = SpeedControlAnalytics.introScreen()
}
