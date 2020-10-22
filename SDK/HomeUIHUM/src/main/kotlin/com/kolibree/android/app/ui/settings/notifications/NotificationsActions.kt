/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.brushreminder.model.BrushingReminderType
import org.threeten.bp.LocalTime

internal sealed class NotificationsActions : BaseAction {
    data class ShowTimePicker(
        val type: BrushingReminderType,
        val currentReminderTime: LocalTime
    ) : NotificationsActions()
}
