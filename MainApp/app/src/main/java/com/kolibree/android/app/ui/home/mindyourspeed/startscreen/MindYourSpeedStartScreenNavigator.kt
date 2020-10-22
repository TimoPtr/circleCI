/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen

import android.app.Activity
import com.kolibree.android.app.base.BaseNavigator

internal class MindYourSpeedStartScreenNavigator :
    BaseNavigator<MindYourSpeedStartScreenActivity>() {

    fun closeScreen() = withOwner {
        finish()
    }

    fun startMindYourSpeedScreen() = withOwner {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
