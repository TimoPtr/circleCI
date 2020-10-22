/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.home.tab.activities.ActivitiesFragment
import com.kolibree.android.app.ui.home.tab.home.HomeFragment
import com.kolibree.android.app.ui.home.tab.profile.ProfileFragment
import com.kolibree.android.app.ui.home.tab.shop.ShopFragment
import com.kolibree.android.app.ui.navigation.horizontal.HorizontalNavigationItem
import com.kolibree.android.homeui.hum.R

@VisibleForApp
enum class BottomNavigationTab {
    DASHBOARD,
    SHOP,
    ACTIVITIES,
    PROFILE
}

internal val dashboardBottomNavigationItem = HorizontalNavigationItem(
    id = BottomNavigationTab.DASHBOARD.ordinal,
    triggerId = R.id.bottom_navigation_home,
    containerId = R.id.dashboard_container,
    fragmentClass = HomeFragment::class.java
)

internal val shopBottomNavigationItem = HorizontalNavigationItem(
    id = BottomNavigationTab.SHOP.ordinal,
    triggerId = R.id.bottom_navigation_shop,
    containerId = R.id.shop_container,
    fragmentClass = ShopFragment::class.java
)

internal val activitiesBottomNavigationItem = HorizontalNavigationItem(
    id = BottomNavigationTab.ACTIVITIES.ordinal,
    triggerId = R.id.bottom_navigation_activities,
    containerId = R.id.activities_container,
    fragmentClass = ActivitiesFragment::class.java
)

internal val profileBottomNavigationItem = HorizontalNavigationItem(
    id = BottomNavigationTab.PROFILE.ordinal,
    triggerId = R.id.bottom_navigation_profile,
    containerId = R.id.profile_container,
    fragmentClass = ProfileFragment::class.java
)

@VisibleForApp
val bottomNavigationItems = listOf(
    dashboardBottomNavigationItem,
    shopBottomNavigationItem,
    activitiesBottomNavigationItem,
    profileBottomNavigationItem
)
