/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import android.content.ComponentName
import android.content.pm.PackageManager
import javax.inject.Inject

/**
 * Interface responsible for enabling or disabling brush reminder related components.
 * Disabled component does not receive system broadcast, this prevents
 * the receiver from being called unnecessarily.
 */
internal interface BrushSyncReminderComponentsToggle {
    fun setComponents(isEnabled: Boolean)
}

/**
 * Implementation of [BrushSyncReminderComponentsToggle]
 * If brush reminder feature is disabled it means that related components:
 *  [ReminderNotificationBroadcastReceiver]
 *  [RestoreReminderNotificationBroadcastReceiver]
 *  will not received an appropriates events from system
 */
internal class BrushReminderComponentsToggleImpl @Inject constructor(
    @ComponentNotification private val notificationComponent: ComponentName,
    @ComponentRestoreNotification private val restoreComponent: ComponentName,
    private val packageManager: PackageManager
) : BrushSyncReminderComponentsToggle {

    override fun setComponents(isEnabled: Boolean) = if (isEnabled) {
        enableComponents()
    } else {
        disableComponents()
    }

    private fun enableComponents() {
        setComponentState(notificationComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        setComponentState(restoreComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    private fun disableComponents() {
        setComponentState(notificationComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        setComponentState(restoreComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    private fun setComponentState(component: ComponentName, state: Int) {
        // Once you enable the receiver this way, it will stay enabled,
        // even if the user reboots the device. In other words, programmatically
        // enabling the receiver overrides the manifest setting, even across reboots.
        packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP
        )
    }
}
