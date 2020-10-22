/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.brushsyncreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kolibree.android.annotation.VisibleForApp

/*
 * This class is needed for registering app for ACTION_BOOT_COMPLETED
 * Without it, brush sync reminder will not be re-scheduled after reboot of device
 */
@VisibleForApp
class RestoreReminderNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // if action was received it means that Application class has been already created
            // and it means that Brush Sync reminder has been already restored
        }
    }
}
