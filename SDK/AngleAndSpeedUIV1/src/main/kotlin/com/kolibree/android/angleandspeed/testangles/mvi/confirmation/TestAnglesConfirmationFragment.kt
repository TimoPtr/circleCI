/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.confirmation

import com.kolibree.android.angleandspeed.testangles.mvi.TestAnglesAnalytics
import com.kolibree.android.app.mvi.confirmation.GameConfirmationFragment
import com.kolibree.android.tracker.AnalyticsEvent

internal class TestAnglesConfirmationFragment : GameConfirmationFragment() {

    override fun resourcesProvider() = TestAnglesConfirmationResourceProvider

    override fun getScreenName(): AnalyticsEvent = TestAnglesAnalytics.finishScreen()
}
