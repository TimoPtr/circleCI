/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import android.app.Activity
import android.content.Intent
import com.kolibree.android.app.base.BaseNavigator
import org.threeten.bp.OffsetDateTime

internal class HeadspaceMindfulMomentActivityNavigator :
    BaseNavigator<HeadspaceMindfulMomentActivity>() {

    fun finish() = withOwner {
        finish()
    }

    fun finishWithSuccess(collectedTime: OffsetDateTime) = withOwner {
        val data = Intent().apply {
            putExtra(EXTRA_COLLECTED_TIME, collectedTime)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}
