/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.extensions

import com.kolibree.android.clock.TrustedClock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

@JvmOverloads
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun TrustedClock.reset(clock: Clock = Clock.systemUTC()) {
    utcClock = clock
    systemZone = ZoneId.systemDefault()
}

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun TrustedClock.setFixedEpochInstant(instantInMillis: Long) {
    utcClock =
        Clock.fixed(Instant.ofEpochMilli(instantInMillis), ZoneId.of("Z"))
    systemZone = utcClock.zone
}

/**
 * Fix date to a given date
 */
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun TrustedClock.setFixedDate(dateTime: ZonedDateTime) {
    utcClock = Clock.fixed(dateTime.toInstant(), dateTime.zone)
    systemZone = utcClock.zone
}

/**
 * Fix date to a given date
 */
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun TrustedClock.setFixedDate(dateTime: OffsetDateTime) {
    utcClock = Clock.fixed(dateTime.toInstant(), dateTime.offset)
    systemZone = utcClock.zone
}

/**
 * Fix Date to now
 */
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun TrustedClock.setFixedDate() {
    utcClock = Clock.fixed(utcClock.instant(), utcClock.zone)
    systemZone = utcClock.zone
}

/**
 * Adds time to current clock
 */
@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun TrustedClock.advanceTimeBy(value: Long, unit: ChronoUnit) {
    val zonedDateTime = ZonedDateTime.now(utcClock).plus(value, unit)
    utcClock = Clock.fixed(zonedDateTime.toInstant(), utcClock.zone)
}

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun withFixedInstant(
    fixedInstant: Instant = TrustedClock.getNowInstant(),
    block: Instant.() -> Unit
) {
    TrustedClock.setFixedEpochInstant(fixedInstant.toEpochMilli())
    block(fixedInstant)
}
