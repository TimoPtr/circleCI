/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.testbrushing.startHumTestBrushingIntent

typealias TestBrushingParams = Pair<String, ToothbrushModel>

class TestBrushingActivityContract :
    ActivityResultContract<TestBrushingParams, Boolean>() {

    override fun createIntent(context: Context, params: TestBrushingParams): Intent =
        startHumTestBrushingIntent(context, params.first, params.second)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}
