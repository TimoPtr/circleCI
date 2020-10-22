/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushstart

import androidx.navigation.findNavController
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlAnalytics
import com.kolibree.android.app.mvi.brushstart.BrushStartFragment
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.auditor.UserStep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class SpeedControlBrushStartFragment : BrushStartFragment(), UserStep,
    TrackableScreen {

    override fun onBrushStarted(model: ToothbrushModel, mac: String) {
        activity?.findNavController(R.id.nav_host_fragment)
            ?.navigateSafe(R.id.action_brushStart_to_brushing)
    }

    override fun getScreenName(): AnalyticsEvent = SpeedControlAnalytics.startScreen()
}
