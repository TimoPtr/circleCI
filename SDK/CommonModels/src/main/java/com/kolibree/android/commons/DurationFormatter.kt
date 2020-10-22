/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import androidx.annotation.Keep

@Keep
class DurationFormatter {

    fun format(elapsedSeconds: Long, withFirstLeadingZero: Boolean = true): String {
        val seconds = elapsedSeconds % SECONDS_IN_MINUTE
        val minutes = elapsedSeconds / SECONDS_IN_MINUTE

        val formattedMinutes = if (withFirstLeadingZero) {
            withLeadingZero(minutes)
        } else {
            minutes.toString()
        }
        val formattedSeconds = withLeadingZero(seconds)

        return "$formattedMinutes:$formattedSeconds"
    }

    private fun withLeadingZero(time: Long): String {
        return if (time >= TWO_DIGITS) {
            time.toString()
        } else {
            "$LEADING_ZERO$time"
        }
    }
}

internal const val SECONDS_IN_MINUTE = 60
internal const val TWO_DIGITS = 10
internal const val LEADING_ZERO = "0"
