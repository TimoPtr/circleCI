/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.room

import androidx.room.TypeConverter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.DATETIME_FORMATTER_NO_ZONE
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.extensions.toUTCEpochMilli
import com.kolibree.android.extensions.toZonedDateTimeFromUTC
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/**
 * If you have to deal with the two converters you have to add the affinity field to the
 * ColumnInfo to specify which one you want.
 * For instance for DateConvertersLong you will have to do
 * @ColumnInfo(name = "NAME" , typeAffinity = ColumnInfo.INTEGER)
 */
@Deprecated("ZoneOffsetConverter should be used instead of this one, cause it looses the TZ information")
@VisibleForApp
class DateConvertersString {

    // Convert LocalDate to String

    @TypeConverter
    fun setLocalDateToString(value: LocalDate?): String =
        value?.let { DATE_FORMATTER.format(value) } ?: ""

    @TypeConverter
    fun getLocalDateFromString(value: String?): LocalDate? {
        return if (value.isNullOrEmpty())
            null
        else LocalDate.parse(value, DATE_FORMATTER)
    }

    // Convert ZonedDateTime to String

    @TypeConverter
    fun setZonedDateTimeToString(zonedDateTime: ZonedDateTime?): String =
        zonedDateTime?.let { DATETIME_FORMATTER.format(it) } ?: ""

    @TypeConverter
    fun getZonedDateTimeFromString(dateTime: String?): ZonedDateTime? {
        return if (dateTime.isNullOrEmpty())
            null
        else ZonedDateTime.parse(dateTime, DATETIME_FORMATTER)
    }

    // Convert LocalDateTime to String

    @TypeConverter
    fun setLocalDateTimeToString(localDateTime: LocalDateTime?): String =
        localDateTime?.let { DATETIME_FORMATTER_NO_ZONE.format(it) } ?: ""

    @TypeConverter
    fun getLocalDateTimeFromString(localDateTime: String?): LocalDateTime? {
        return if (localDateTime.isNullOrEmpty())
            null
        else LocalDateTime.parse(localDateTime, DATETIME_FORMATTER_NO_ZONE)
    }
}

@Deprecated("ZoneOffsetConverter should be used instead of this one, cause it looses the TZ information")
@VisibleForApp
class DateConvertersLong {
    // Convert LocalDate to Long

    @TypeConverter
    fun setLocalDateToLong(value: LocalDate?): Long = value?.toEpochDay() ?: 0L

    @TypeConverter
    fun getLocalDateFromLong(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun getLocalDateFromLong(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    // Convert ZonedDateTime to Long

    @TypeConverter
    fun setZonedDateTimeUTCToLong(date: ZonedDateTime?): Long? =
        date?.toUTCEpochMilli() ?: 0L

    @TypeConverter
    fun getZonedDateTimeUTCFromLong(value: Long?): ZonedDateTime? =
        value?.toZonedDateTimeFromUTC()

    @TypeConverter
    fun getZonedDateTimeUTCFromLong(value: Long): ZonedDateTime =
        value.toZonedDateTimeFromUTC()

    // Convert LocalDateTime to Long

    @TypeConverter
    fun setLocalDateTimeUTCToLong(date: LocalDateTime?): Long =
        date?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: 0L

    @TypeConverter
    fun getLocalDateTimeUTCFromLong(value: Long?): LocalDateTime? =
        value?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDateTime() }

    @TypeConverter
    fun getLocalDateTimeUTCFromLong(value: Long): LocalDateTime =
        Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDateTime()
}

@VisibleForApp
object ZoneOffsetConverter {

    @TypeConverter
    @JvmStatic
    fun toZoneOffset(value: String?): ZoneOffset? = value?.let { ZoneOffset.of(it) }

    @TypeConverter
    @JvmStatic
    fun fromZoneOffset(zoneOffset: ZoneOffset?): String? = zoneOffset?.id
}
