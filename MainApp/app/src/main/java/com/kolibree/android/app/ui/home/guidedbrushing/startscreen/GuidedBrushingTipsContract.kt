/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.guidedbrushing.ui.getGuidedBrushingTipsIntent

@VisibleForApp
class GuidedBrushingTipsContract :
    ActivityResultContract<Unit, Unit>() {

    override fun createIntent(context: Context, param: Unit): Intent =
        getGuidedBrushingTipsIntent(context)

    override fun parseResult(resultCode: Int, intent: Intent?) = Unit
}
