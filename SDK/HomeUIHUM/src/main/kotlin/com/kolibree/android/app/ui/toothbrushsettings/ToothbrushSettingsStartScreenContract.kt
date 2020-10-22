/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
class ToothbrushSettingsStartScreenContract :
    ActivityResultContract<String, ToothbrushSettingsScreenResult>() {

    override fun createIntent(context: Context, mac: String): Intent =
        toothbrushSettingsIntent(context, mac)

    override fun parseResult(resultCode: Int, intent: Intent?): ToothbrushSettingsScreenResult {
        return when (resultCode) {
            RESULT_OK -> ToothbrushSettingsScreenResult.OpenShop
            else -> ToothbrushSettingsScreenResult.Canceled
        }
    }
}

@VisibleForApp
enum class ToothbrushSettingsScreenResult {
    OpenShop,
    Canceled
}
