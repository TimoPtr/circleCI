package com.kolibree.android.offlinebrushings.sync

import javax.inject.Inject
import org.threeten.bp.ZoneId.systemDefault
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.chrono.IsoChronology.INSTANCE
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle.STRICT
import org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY
import org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR
import org.threeten.bp.temporal.ChronoField.NANO_OF_SECOND
import org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE

@Suppress("MagicNumber")
internal class LastSyncDateFormatterImpl
@Inject constructor() : LastSyncDateFormatter {

    // Builder representation of "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format.
    // Fixes bug https://shorturl.at/eDIOS
    private val formatter =
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendFraction(NANO_OF_SECOND, 3, 3, true)
            .appendLiteral('Z')
            .toFormatter()
            .withResolverStyle(STRICT)
            .withChronology(INSTANCE)
            .withZone(systemDefault())

    override fun parse(textualDate: String): ZonedDateTime {
        return ZonedDateTime.parse(textualDate, formatter)
    }

    override fun format(date: ZonedDateTime): String {
        return formatter.format(date)
    }
}
