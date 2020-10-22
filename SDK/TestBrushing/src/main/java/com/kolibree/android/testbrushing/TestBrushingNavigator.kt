/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import androidx.navigation.findNavController
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.tracker.Analytics

@VisibleForApp
class TestBrushingNavigator : BaseNavigator<TestBrushingActivity>() {

    fun startOngoingBrushing() {
        withOwner {
            findNavController(R.id.nav_host_fragment)
                .navigateSafe(R.id.action_fragment_brushingstart_to_fragment_ongoing_brushing)
        }
    }

    fun finishWithSuccess() {
        withOwner {
            Analytics.send(TestBrushingAnalytics.finishedWithSuccess())
            setResult(RESULT_OK)
            finish()
        }
    }

    fun terminate() {
        withOwner {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
