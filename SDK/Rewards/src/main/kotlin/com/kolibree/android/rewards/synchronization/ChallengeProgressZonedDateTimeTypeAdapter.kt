package com.kolibree.android.rewards.synchronization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoField
import timber.log.Timber

internal const val CHALLENGE_PROGRESS_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
internal const val SMILES_HISTORY_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

private const val MIN_MILLIS = 1
private const val MAX_MILLIS = 6

// 2018-12-04 17:22:54.493564+00:00
internal val CHALLENGE_PROGRESS_DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern(CHALLENGE_PROGRESS_DATETIME_PATTERN)
    .appendFraction(ChronoField.MILLI_OF_SECOND, MIN_MILLIS, MAX_MILLIS, true)
    .appendZoneOrOffsetId()
    .toFormatter()

// 2018-12-20T12:50:24.361950+00:00
internal val SMILES_HISTORY_DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern(SMILES_HISTORY_DATETIME_PATTERN)
    .appendFraction(ChronoField.MILLI_OF_SECOND, MIN_MILLIS, MAX_MILLIS, true)
    .appendZoneOrOffsetId()
    .toFormatter()

internal class ChallengeProgressZonedDateTimeTypeAdapter :
    CustomFormatterZonedDateTimeTypeAdapter(CHALLENGE_PROGRESS_DATETIME_FORMATTER)

internal class SmilesHistoryZonedDateTimeTypeAdapter :
    CustomFormatterZonedDateTimeTypeAdapter(SMILES_HISTORY_DATETIME_FORMATTER)

internal abstract class CustomFormatterZonedDateTimeTypeAdapter(private val formatter: DateTimeFormatter) :
    TypeAdapter<ZonedDateTime>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: ZonedDateTime?) {
        if (value == null) {
            out.nullValue()
            return
        }

        out.value(value.format(formatter))
    }

    @Throws(IOException::class)
    override fun read(json: JsonReader): ZonedDateTime? {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull()
            return null
        }

        val value = json.nextString()

        if (value.isNullOrEmpty()) return null

        return try {
            ZonedDateTime.parse(value, formatter)
        } catch (e: DateTimeParseException) {
            Timber.e(e, "Unable to convert $value into ZonedDateTime.")

            null
        }
    }
}
