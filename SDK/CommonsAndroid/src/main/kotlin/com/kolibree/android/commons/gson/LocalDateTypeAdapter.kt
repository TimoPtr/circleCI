package com.kolibree.android.commons.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.failearly.FailEarly
import java.io.IOException
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException

@VisibleForApp
class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
            return
        }

        try {
            out.value(value.format(DATE_FORMATTER))
        } catch (e: DateTimeException) {
            FailEarly.fail(exception = e, message = "Unable to write $value as LocalDate.")
        }
    }

    @Throws(IOException::class)
    override fun read(json: JsonReader): LocalDate? {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull()
            return null
        }

        val value = json.nextString()

        if (value.isNullOrEmpty()) return null

        return try {
            LocalDate.parse(value, DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            FailEarly.fail(exception = e, message = "Unable to read $value into LocalDate.")

            null
        }
    }
}
