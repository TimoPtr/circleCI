/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import android.app.Activity.RESULT_OK
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.base.BaseNavigator

internal class GuidedBrushingStartScreenNavigator :
    BaseNavigator<GuidedBrushingStartScreenActivity>() {

    private lateinit var guidedBrushingTipsStartScreenContract: ActivityResultLauncher<Unit>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        withOwner { setupGuidedBrushingTipsStartScreen() }
    }

    fun startGuidedBrushing() {
        withOwner {
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    fun startGuidedBrushingTips() {
        guidedBrushingTipsStartScreenContract.launch(Unit)
    }

    fun finish() {
        withOwner {
            finish()
        }
    }

    private fun GuidedBrushingStartScreenActivity.setupGuidedBrushingTipsStartScreen() {
        guidedBrushingTipsStartScreenContract =
            registerForActivityResult(GuidedBrushingTipsContract()) { startGuidedBrushing() }
    }
}
