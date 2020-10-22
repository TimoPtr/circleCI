/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.formatter

import android.text.format.DateFormat.is24HourFormat
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.commons.HOUR_12H_FORMATTER
import com.kolibree.android.commons.HOUR_24H_FORMATTER
import javax.inject.Inject
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

@VisibleForApp
class BrushReminderTimeFormatter @Inject constructor(
    private val androidTimeFormat: AndroidTimeFormat
) {

    fun format(time: LocalTime): String {
        return getTimeFormat().format(time)
    }

    private fun getTimeFormat(): DateTimeFormatter {
        return if (androidTimeFormat.is24HourFormat()) HOUR_24H_FORMATTER
        else HOUR_12H_FORMATTER
    }
}

@VisibleForApp
class AndroidTimeFormat @Inject constructor(
    private val context: ApplicationContext
) {
    fun is24HourFormat(): Boolean {
        return is24HourFormat(context)
    }
}
