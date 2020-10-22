/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.update

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
data class AppUpdateRequested(
    val intentSender: IntentSender,
    val requestCode: Int,
    val fillInIntent: Intent?,
    val flagsMask: Int,
    val flagsValues: Int,
    val extraFlags: Int,
    val options: Bundle?
) {
    fun start(activity: Activity) {
        activity.startIntentSenderForResult(
            intentSender,
            requestCode,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options
        )
    }
}
