/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import android.app.Activity
import androidx.annotation.Keep
import com.kolibree.android.app.utils.allowScreenOff
import com.kolibree.android.app.utils.keepScreenOn
import com.kolibree.android.commons.Weak
import javax.inject.Inject

@Keep
interface KeepScreenOnController {

    fun keepScreenOn()

    fun allowScreenOff()
}

@Keep
@GameScope
class KeepScreenOnControllerImpl @Inject constructor(activity: Activity) : KeepScreenOnController {
    var activity by Weak {
        activity
    }

    override fun keepScreenOn() {
        activity?.keepScreenOn()
    }

    override fun allowScreenOff() {
        activity?.allowScreenOff()
    }
}
