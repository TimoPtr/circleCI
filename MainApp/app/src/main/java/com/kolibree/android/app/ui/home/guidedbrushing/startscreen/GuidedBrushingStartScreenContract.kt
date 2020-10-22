/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.commons.ToothbrushModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuidedBrushingStartScreenParams(
    val isManual: Boolean,
    val mac: String? = null,
    val model: ToothbrushModel? = null
) : Parcelable

data class GuidedBrushingStartScreenResult(
    val param: GuidedBrushingStartScreenParams?,
    val shouldProceed: Boolean
)

class GuidedBrushingStartScreenContract :
    ActivityResultContract<GuidedBrushingStartScreenParams, GuidedBrushingStartScreenResult>() {

    override fun createIntent(context: Context, param: GuidedBrushingStartScreenParams): Intent =
        startGuidedBrushingStartScreenIntent(context).apply {
            putExtra(EXTRA_GUIDED_BRUSHING_PARAMS, param)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): GuidedBrushingStartScreenResult {
        val params =
            intent?.getParcelableExtra(EXTRA_GUIDED_BRUSHING_PARAMS) as? GuidedBrushingStartScreenParams?
        return GuidedBrushingStartScreenResult(params, resultCode == Activity.RESULT_OK)
    }
}

internal const val EXTRA_GUIDED_BRUSHING_PARAMS = "extra_guided_brushing_params"
