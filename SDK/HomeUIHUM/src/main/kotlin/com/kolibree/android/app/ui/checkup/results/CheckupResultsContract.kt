/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.Keep

@Keep
class CheckupResultsContract : ActivityResultContract<CheckupOrigin, Boolean>() {

    override fun createIntent(context: Context, origin: CheckupOrigin): Intent =
        startCheckupResultsActivityIntent(context, origin)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
