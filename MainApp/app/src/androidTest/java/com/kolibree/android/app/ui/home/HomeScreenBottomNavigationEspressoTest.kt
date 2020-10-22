/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.pressBackUnconditionally
import com.kolibree.android.app.ui.home.BottomNavigationUtils.checkAccount
import com.kolibree.android.app.ui.home.BottomNavigationUtils.checkActivities
import com.kolibree.android.app.ui.home.BottomNavigationUtils.checkHomeSelected
import com.kolibree.android.app.ui.home.BottomNavigationUtils.checkShop
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class HomeScreenBottomNavigationEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun bottomNavigation_happyPath() {
        prepareMocks()

        launchActivity()

        // Dashboard
        checkHomeSelected()

        // Activities
        BottomNavigationUtils.navigateToActivities()
        checkActivities()

        // Shop
        BottomNavigationUtils.navigateToShop()
        checkShop()

        // Account
        BottomNavigationUtils.navigateToProfile()
        checkAccount()

        // Back to Home
        pressBack()
        checkHomeSelected()

        // Finish
        pressBackUnconditionally()
        assertTrue(activityTestRule.activity.isFinishing)
    }
}
