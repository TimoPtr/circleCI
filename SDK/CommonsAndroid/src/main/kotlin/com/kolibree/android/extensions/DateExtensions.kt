/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

@file:Suppress("TooManyFunctions")
package com.kolibree.android.extensions

import androidx.annotation.Keep
import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.clock.TrustedClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.Year
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

@Keep
fun ZonedDateTime.toUTCEpochMilli(): Long =
    withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli()

@Keep
fun ZonedDateTime.toEpochMilli(): Long = toInstant().toEpochMilli()

@Keep
fun OffsetDateTime.toEpochMilli(): Long = toInstant().toEpochMilli()

@Keep
fun LocalDateTime.toEpochMilli(): Long = atZone(TrustedClock.systemZone).toEpochMilli()

@Keep
fun Long.toZonedDateTimeFromUTC(): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC)

@Keep
@SuppressWarnings("MagicNumber")
fun LocalDate.atEndOfDay(): LocalDateTime = atTime(23, 59, 59)

@Keep
fun LocalDate.isFirstDayOfTheMonth(): Boolean = dayOfMonth == 1

@Keep
fun LocalDate.isLastDayOfTheMonth(): Boolean =
    dayOfMonth == month.length(Year.isLeap(year.toLong()))

/**
 * Converts a LocalDateTime to a LocalDate in kolibree day
 *
 * Kolibree days start at 4AM, so the resulting date might be on the same day as the original LocalDateTime, or the day
 * before.
 *
 * The reasoning behind this is that some users that go to bed late (1AM) wanted those brushings to belong to the day
 * they have lived on, not the actual date.
 *
 * Examples
 *
 * - 1/10/2019 23:00 -> Kolibree day is 01/10/2019
 * - 1/10/2019 02:40 -> Kolibree day is 30/09/2019
 */
@Keep
fun LocalDateTime.toKolibreeDay(): LocalDate {
    val kolibreeStartOfDay = truncatedTo(ChronoUnit.DAYS).withHour(KOLIBREE_DAY_START_HOUR)

    return if (isBefore(kolibreeStartOfDay)) {
        toLocalDate().minusDays(1)
    } else {
        toLocalDate()
    }
}

@Keep
fun LocalDate.atStartOfKolibreeDay(): OffsetDateTime = LocalDateTime
    .of(this, LocalTime.of(KOLIBREE_DAY_START_HOUR, 0))
    .atOffset(TrustedClock.systemZoneOffset)

@Keep
fun OffsetDateTime.toCurrentTimeZone(): OffsetDateTime =
    withOffsetSameInstant(TrustedClock.systemZoneOffset)
