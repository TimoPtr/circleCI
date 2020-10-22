/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.synchronization

import android.content.Context
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

internal class BrushSyncReminderSynchronizedVersions @Inject constructor(
    context: Context
) : BasePreferencesImpl(context) {

    fun getVersion() = prefs.getInt(KEY_BRUSH_SYNC_REMINDER_VERSION, 0)

    fun setVersion(newVersion: Int) =
        prefsEditor.putInt(KEY_BRUSH_SYNC_REMINDER_VERSION, newVersion).apply()

    companion object {
        const val KEY_BRUSH_SYNC_REMINDER_VERSION = "brush_sync_reminder_version"
    }
}
