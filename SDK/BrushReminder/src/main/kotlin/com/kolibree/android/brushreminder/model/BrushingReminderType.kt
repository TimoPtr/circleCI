/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.model

import com.kolibree.android.annotation.VisibleForApp
import org.threeten.bp.LocalTime

@VisibleForApp
enum class BrushingReminderType(private val defaultTimeHour: Int) {
    MORNING(MORNING_REMINDER_TIME),
    AFTERNOON(AFTERNOON_REMINDER_TIME),
    EVENING(EVENING_REMINDER_TIME);

    fun defaultLocalTime(): LocalTime = LocalTime.of(defaultTimeHour, 0)
}

private const val MORNING_REMINDER_TIME = 8
private const val AFTERNOON_REMINDER_TIME = 13
private const val EVENING_REMINDER_TIME = 20
