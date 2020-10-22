/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent

internal object ToolbarEventTracker {

    private var currentPage: AnalyticsEvent? = null
    fun setCurrentPage(currentPage: String) {
        this.currentPage = AnalyticsEvent(currentPage)
    }

    fun toothbrushMenu() = sendEvent(TB_SETTINGS)

    fun cart() = sendEvent(GO_TO_SHOP)

    private fun sendEvent(screenName: String) =
        currentPage?.let { event ->
            Analytics.send(event + screenName)
        }
}

const val TB_SETTINGS = "TBSetting_Menu"
const val GO_TO_SHOP = "GoToShop"
