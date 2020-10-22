/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import android.app.Activity.RESULT_OK
import com.kolibree.android.app.base.BaseNavigator

internal class TestBrushingStartScreenNavigator : BaseNavigator<TestBrushingStartScreenActivity>() {

    fun startTestBrushing() {
        withOwner {
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    fun finish() {
        withOwner {
            finish()
        }
    }
}
