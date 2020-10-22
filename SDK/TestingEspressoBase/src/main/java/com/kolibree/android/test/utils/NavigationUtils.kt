/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import android.annotation.SuppressLint
import android.view.Gravity
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.withId

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun openDrawer(
    @IdRes drawerId: Int,
    gravity: Int = Gravity.START
) {
    onView(withId(drawerId))
        .check(matches(isClosed(gravity)))
        .perform(open())
        .check(matches(isOpen(gravity)))
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun closeDrawer(
    @IdRes drawerId: Int,
    gravity: Int = Gravity.START
) {
    onView(withId(drawerId))
        .check(matches(isOpen(gravity)))
        .perform(close())
        .check(matches(isClosed(gravity)))
}
