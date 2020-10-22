/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import com.kolibree.android.tracker.AnalyticsEvent

internal object GuidedBrushingStartScreenAnalytics {

    fun main() = AnalyticsEvent("GuidedBrushing_PopUpHelp")

    fun start() = main() + "Start"

    fun cancel() = main() + "Cancel"
}
