/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.clock

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/**
 * This is the clock to use everywhere
 */
@Keep
object TrustedClock {
    var utcClock: Clock = Clock.systemUTC()
        @JvmStatic get
        @VisibleForTesting @JvmStatic set

    var systemZone: ZoneId = ZoneId.systemDefault()
        @JvmStatic get
        @VisibleForTesting @JvmStatic set

    val systemZoneOffset: ZoneOffset
        @JvmStatic get() = systemZone.rules.getOffset(getNowInstant())

    fun systemClock(): Clock = utcClock.withZone(systemZone)

    @JvmStatic
    fun getNowZonedDateTime(): ZonedDateTime = ZonedDateTime.now(systemClock())

    @JvmStatic
    fun getNowOffsetDateTime(): OffsetDateTime = OffsetDateTime.now(systemClock())

    @JvmStatic
    fun getNowZonedDateTimeUTC(): ZonedDateTime = ZonedDateTime.now(utcClock)

    @JvmStatic
    fun getNowInstant(): Instant = Instant.now(systemClock())

    @JvmStatic
    fun getNowInstantUTC(): Instant = Instant.now(utcClock)

    @JvmStatic
    fun getNowLocalTime(): LocalTime = LocalTime.now(systemClock())

    @JvmStatic
    fun getNowLocalDate(): LocalDate = LocalDate.now(systemClock())

    @JvmStatic
    fun getNowLocalDateTime(): LocalDateTime = LocalDateTime.now(systemClock())

    @JvmStatic
    fun getCurrentMonth(): YearMonth = YearMonth.now(systemClock())
}
