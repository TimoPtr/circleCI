/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.app.ui.settings.SettingsInitialAction
import com.kolibree.android.app.ui.settings.notifications.startNotificationsActivity
import com.kolibree.android.app.ui.settings.startSettingsIntent

internal class MoreWaysToEarnPointsCardNavigator : BaseNavigator<HomeScreenActivity>() {

    fun showSettingsScreen(withInitialAction: SettingsInitialAction? = null) = withOwner {
        startSettingsIntent(this, withInitialAction)
    }

    fun showNotificationScreen() = withOwner {
        startNotificationsActivity(this)
    }
}
