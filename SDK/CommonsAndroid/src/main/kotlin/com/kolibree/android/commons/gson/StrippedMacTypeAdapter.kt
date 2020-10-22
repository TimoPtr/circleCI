package com.kolibree.android.commons.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.models.StrippedMac
import java.io.IOException

@VisibleForApp
class StrippedMacTypeAdapter : TypeAdapter<StrippedMac>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: StrippedMac?) {
        if (value == null) {
            return
        }

        out.value(value.value)
    }

    @Throws(IOException::class)
    override fun read(json: JsonReader): StrippedMac? {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull()
            return null
        }

        val value = json.nextString()

        if (value.isNullOrEmpty()) return null

        return StrippedMac.fromMac(value)
    }
}
