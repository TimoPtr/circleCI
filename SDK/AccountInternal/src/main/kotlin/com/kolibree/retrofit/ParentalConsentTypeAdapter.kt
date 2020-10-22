/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.retrofit

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.annotation.VisibleForApp
import java.io.IOException
import org.json.JSONException
import timber.log.Timber

@VisibleForApp
class ParentalConsentTypeAdapter : TypeAdapter<ParentalConsent>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: ParentalConsent) {
        when (value) {
            ParentalConsent.GRANTED -> out.value(true)
            ParentalConsent.PENDING -> out.value(false)
            ParentalConsent.UNKNOWN -> {} // does not do anything
        }
    }

    @Throws(IOException::class)
    override fun read(value_in: JsonReader?): ParentalConsent {
        if (value_in == null)
            return ParentalConsent.UNKNOWN

        if (value_in.peek() == JsonToken.NULL) {
            value_in.nextNull()
            return ParentalConsent.UNKNOWN
        }

        return try {
            val booleanParentConsent = value_in.nextBoolean()
            if (booleanParentConsent) ParentalConsent.GRANTED else ParentalConsent.PENDING
        } catch (e: JSONException) {
            Timber.e(e, "Error in parentalConsent")
            ParentalConsent.UNKNOWN
        }
    }
}
