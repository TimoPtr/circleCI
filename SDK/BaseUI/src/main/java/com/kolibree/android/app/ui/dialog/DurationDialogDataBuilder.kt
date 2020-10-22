/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import org.threeten.bp.Duration

private const val SECONDS_FORMAT = "%02d"

internal fun buildValuesRange(start: Duration, end: Duration, increment: Duration): List<Duration> =
    generateSequence(start) {
        if (it.plus(increment) > end) null else it.plus(increment)
    }.toList()

internal fun buildMinuteValues(durations: List<Duration>): List<Int> =
    durations
        .distinctBy { it.toMinutes() }
        .map { it.toMinutes().toInt() }

internal fun buildMinuteStrings(minuteValues: List<Int>): Array<String> =
    minuteValues
        .map { it.toString() }
        .toTypedArray()

internal fun buildSecondsStrings(minutesSeconds: List<Duration>): Array<String> =
    minutesSeconds.map {
        SECONDS_FORMAT.format(it.minusMinutes(it.toMinutes()).seconds)
    }.toTypedArray()
