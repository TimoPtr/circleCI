/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.data

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import org.threeten.bp.LocalDateTime

@VisibleForApp
data class BrushSyncReminder(
    val profileId: Long,
    val isEnabled: Boolean,
    val reminderDate: LocalDateTime
) {
    fun isValid(): Boolean {
        val today = TrustedClock.getNowLocalDateTime()
        return reminderDate.isAfter(today)
    }
}
