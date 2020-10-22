/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import android.content.Context
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

internal class BrushHeadStatusSynchronizedVersions @Inject constructor(context: Context) :
    BasePreferencesImpl(context) {

    companion object {
        const val KEY_BRUSH_HEAD_STATUS = "brush_head_status_version"
    }

    fun brushHeadStatusVersion() = prefs.getInt(KEY_BRUSH_HEAD_STATUS, 0)

    fun setBrushHeadUsageVersion(newVersion: Int) {
        prefsEditor.putInt(KEY_BRUSH_HEAD_STATUS, newVersion).apply()
    }
}
