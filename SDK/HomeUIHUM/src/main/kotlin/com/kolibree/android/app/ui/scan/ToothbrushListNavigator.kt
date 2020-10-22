/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.scan

import android.app.Activity
import com.kolibree.android.app.base.BaseNavigator

internal class ToothbrushListNavigator : BaseNavigator<ToothbrushListActivity>() {

    fun closeScreen(selectedToothbrushData: SelectedToothbrushData? = null) = withOwner {
        if (selectedToothbrushData != null) {
            setResult(Activity.RESULT_OK, selectedToothbrushData.toResult())
        }
        finish()
    }
}
