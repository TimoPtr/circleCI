/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.defensive

import androidx.annotation.Keep
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.DATETIME_FORMATTER_NO_ZONE
import com.kolibree.android.commons.DATE_FORMATTER
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

/**
 * Simple static methods to be called at the start of your own methods to verify correct arguments
 * and state.
 */
@Keep
object PreconditionsKt {

    /**
     * Ensures that the argument string contains valid datetime string of a given format
     *
     * @param value a string with datetime to parse
     * @param format datetime format to test against
     * @return the validated [ZonedDateTime]
     * @throws DateTimeParseException if `value` does not contain parsable datetime
     */
    @JvmStatic
    @Throws(DateTimeParseException::class)
    fun checkArgumentContainsZonedDateTime(
        value: String?,
        format: DateTimeFormatter = DATETIME_FORMATTER
    ): ZonedDateTime {
        return ZonedDateTime.parse(value, format)
    }

    /**
     * Ensures that the argument string contains valid datetime string of a given format
     *
     * @param value a string with datetime to parse
     * @param format datetime format to test against
     * @return the validated [LocalDateTime]
     * @throws DateTimeParseException if `value` does not contain parsable datetime
     */
    @JvmStatic
    @Throws(DateTimeParseException::class)
    fun checkArgumentContainsDateTime(
        value: String?,
        format: DateTimeFormatter = DATETIME_FORMATTER_NO_ZONE
    ): LocalDateTime {
        return LocalDateTime.parse(value, format)
    }

    /**
     * Ensures that the argument string contains valid datetime string of a given format
     *
     * @param value a string with date to parse
     * @param format date format to test against
     * @return the validated [LocalDate]
     * @throws DateTimeParseException if `value` does not contain parsable date
     */
    @JvmStatic
    @Throws(DateTimeParseException::class)
    fun checkArgumentContainsDate(
        value: String?,
        format: DateTimeFormatter = DATE_FORMATTER
    ): LocalDate {
        return LocalDate.parse(value, format)
    }
}
