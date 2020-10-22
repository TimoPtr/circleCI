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
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.commons.ToothbrushModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MindYourSpeedStartScreenParams(
    val mac: String,
    val model: ToothbrushModel
) : Parcelable

data class MindYourSpeedStartScreenResult(
    val param: MindYourSpeedStartScreenParams?,
    val shouldProceed: Boolean
)

class MindYourSpeedStartScreenContract :
    ActivityResultContract<MindYourSpeedStartScreenParams, MindYourSpeedStartScreenResult>() {

    override fun createIntent(context: Context, param: MindYourSpeedStartScreenParams): Intent =
        startMindYourSpeedStartScreenIntent(context).apply {
            putExtra(EXTRA_MIND_YOUR_SPEED_PARAMS, param)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): MindYourSpeedStartScreenResult {
        val params =
            intent?.getParcelableExtra(EXTRA_MIND_YOUR_SPEED_PARAMS) as? MindYourSpeedStartScreenParams?
        return MindYourSpeedStartScreenResult(params, resultCode == Activity.RESULT_OK)
    }
}

internal const val EXTRA_MIND_YOUR_SPEED_PARAMS = "extra_mind_your_speed_params"
