/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.insets

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly

/**
 * KTX extension for window insets appliance
 */
@Keep
fun Activity.withWindowInsets(view: View, block: WindowInsets.() -> Unit) {
    FailEarly.failInConditionMet(
        this is WindowInsetsOwner,
        "Activity implements WindowInsetsOwner, you should use withWindowInsetsOwner"
    )
    view.setOnApplyWindowInsetsListener { _, insets ->
        insets.block()
        insets
    }
}

@Keep
fun WindowInsets.topStatusBarWindowInset(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getInsets(WindowInsets.Type.statusBars()).top
    } else {
        systemWindowInsetTop
    }

@Keep
fun WindowInsets.bottomNavigationBarInset(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getInsets(WindowInsets.Type.navigationBars()).bottom
    } else {
        systemWindowInsetBottom
    }
