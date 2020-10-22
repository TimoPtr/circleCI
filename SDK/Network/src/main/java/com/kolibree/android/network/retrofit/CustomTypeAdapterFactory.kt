package com.kolibree.android.network.retrofit

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.commons.gson.LocalDateTypeAdapter
import com.kolibree.android.commons.gson.OffsetDateTimeTypeAdapter
import com.kolibree.android.commons.gson.StrippedMacTypeAdapter
import com.kolibree.android.commons.gson.ZonedDateTimeTypeAdapter
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.retrofit.ParentalConsentTypeAdapter
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime

/**
 * Factory for custom Type for Gson.
 * Decide which adapter should be used according to a type
 */
internal object CustomTypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    val FACTORY: TypeAdapterFactory = object : TypeAdapterFactory {
        override fun <T> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? =
            when (typeToken.rawType) {
                ParentalConsent::class.java -> ParentalConsentTypeAdapter() as TypeAdapter<T>
                LocalDate::class.java -> LocalDateTypeAdapter() as TypeAdapter<T>
                ZonedDateTime::class.java -> ZonedDateTimeTypeAdapter() as TypeAdapter<T>
                OffsetDateTime::class.java -> OffsetDateTimeTypeAdapter() as TypeAdapter<T>
                StrippedMac::class.java -> StrippedMacTypeAdapter() as TypeAdapter<T>
                else -> null
            }
    }
}
