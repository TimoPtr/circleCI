package com.kolibree.sdkws.appdata.persistence

import androidx.room.TypeConverter
import com.kolibree.sdkws.brushing.getTimestampSecondsPrecision
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

internal class ZonedDateTimeConverter {

    @TypeConverter
    fun fromZonedDateTime(zonedDateTime: ZonedDateTime): Long = zonedDateTime
        .getTimestampSecondsPrecision()

    @TypeConverter
    fun toZonedDateTime(epoch: Long): ZonedDateTime = Instant.ofEpochMilli(epoch)
        .atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)
}
