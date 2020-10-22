/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.app

import android.app.Activity
import android.view.View
import androidx.annotation.Keep

@Keep
fun Activity.setLightStatusBar() {
    window.decorView.systemUiVisibility =
        (window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
}

@Keep
fun Activity.setDarkStatusBar() {
    if (hasLightStatusBar()) {
        window.decorView.systemUiVisibility =
            (window.decorView.systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }
}

@Keep
fun Activity.hasLightStatusBar() =
    (window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) ==
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
