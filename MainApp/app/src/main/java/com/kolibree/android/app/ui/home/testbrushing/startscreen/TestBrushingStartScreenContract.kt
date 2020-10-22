/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.commons.ToothbrushModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestBrushingStartScreenParams(
    val mac: String,
    val model: ToothbrushModel
) : Parcelable

data class TestBrushingStartScreenResult(
    val param: TestBrushingStartScreenParams?,
    val shouldProceed: Boolean
)

class TestBrushingStartScreenContract :
    ActivityResultContract<TestBrushingStartScreenParams, TestBrushingStartScreenResult>() {

    override fun createIntent(context: Context, param: TestBrushingStartScreenParams): Intent =
        startTestBrushingStartScreenIntent(context).apply {
            putExtra(EXTRA_TEST_BRUSHING_PARAMS, param)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): TestBrushingStartScreenResult {
        val params =
            intent?.getParcelableExtra(EXTRA_TEST_BRUSHING_PARAMS) as? TestBrushingStartScreenParams?
        return TestBrushingStartScreenResult(params, resultCode == Activity.RESULT_OK)
    }
}

internal const val EXTRA_TEST_BRUSHING_PARAMS = "extra_test_brushing_params"
