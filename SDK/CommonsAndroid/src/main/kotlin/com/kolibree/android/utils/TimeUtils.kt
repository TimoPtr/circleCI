/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import java.util.Locale
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

@SuppressWarnings("MagicNumber")
@Keep
object TimeUtils {

    /**
     * Get a formatted brushing duration with separator pattern.
     *
     * @param durationSeconds brushing duration in seconds
     * @return non null formatted brushing duration
     */
    @JvmOverloads
    @JvmStatic
    fun getFormattedBrushingDuration(durationSeconds: Long, separator: String = ":"): String {
        return if (durationSeconds > 59L) {
            if (durationSeconds % 60L == 0L) {
                String.format(Locale.getDefault(), "%d" + separator + "00", durationSeconds / 60L)
            } else {
                String.format(
                    Locale.getDefault(),
                    "%d$separator%02d",
                    durationSeconds / 60L,
                    durationSeconds % 60L
                )
            }
        } else {
            String.format(Locale.getDefault(), "0$separator%02d", durationSeconds)
        }
    }

    /**
     * Get a formatted localized date.
     *
     *
     * If the date is in the current week, it will return a short pattern, a longer one if the date
     * is before
     *
     * @param dateTime non null [ZonedDateTime]
     * @param locale non null [Locale]
     * @return non null formatted date
     */
    @JvmStatic
    fun getFormattedDate(dateTime: OffsetDateTime, locale: Locale): String {
        val englishSpeaking = locale.language.startsWith("en")
        val pattern: String

        pattern =
            if (dateTime.isLessThanSixDaysOld()) { // Last week
                if (englishSpeaking) "EEE, h:mm a" else "EEE HH:mm"
            } else {
                if (englishSpeaking) "EEE, MMM d" else "EEE d MMM"
            }

        return DateTimeFormatter.ofPattern(pattern, locale).format(dateTime)
    }

    @Suppress("MagicNumber")
    private fun OffsetDateTime.isLessThanSixDaysOld(): Boolean {
        val dateTimeAtSixDaysOld = TrustedClock.getNowOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).minusDays(6)
        val dateTimeTruncated = truncatedTo(ChronoUnit.SECONDS)
        return dateTimeTruncated.isAfter(dateTimeAtSixDaysOld) || dateTimeTruncated.isEqual(dateTimeAtSixDaysOld)
    }
}
