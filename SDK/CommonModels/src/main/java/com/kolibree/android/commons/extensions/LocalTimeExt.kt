/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.extensions

import com.kolibree.android.annotation.VisibleForApp
import java.util.Calendar
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

@VisibleForApp
fun LocalTime.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(Calendar.HOUR_OF_DAY, this.hour)
    calendar.set(Calendar.MINUTE, this.minute)
    return calendar
}

@VisibleForApp
fun LocalDateTime.toCalendar(zone: ZoneId): Calendar {
    return DateTimeUtils.toGregorianCalendar(this.atZone(zone))
}

@VisibleForApp
fun Calendar.toLocalTime(): LocalTime {
    val hour = this.get(Calendar.HOUR_OF_DAY)
    val minute = this.get(Calendar.MINUTE)
    return LocalTime.of(hour, minute)
}

@VisibleForApp
fun Calendar.toLocalDateTime(): LocalDateTime {
    return DateTimeUtils.toZonedDateTime(this).toLocalDateTime()
}
