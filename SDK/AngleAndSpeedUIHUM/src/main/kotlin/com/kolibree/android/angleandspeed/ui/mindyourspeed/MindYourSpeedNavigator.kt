/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import com.kolibree.android.app.base.BaseNavigator

internal class MindYourSpeedNavigator : BaseNavigator<MindYourSpeedActivity>() {

    fun finishWithSuccess() {
        withOwner {
            setResult(RESULT_OK)
            finish()
        }
    }

    fun cancel() {
        withOwner {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
