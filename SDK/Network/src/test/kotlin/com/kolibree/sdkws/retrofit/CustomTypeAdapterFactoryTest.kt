package com.kolibree.sdkws.retrofit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kolibree.android.commons.gson.LocalDateTypeAdapter
import com.kolibree.android.commons.gson.OffsetDateTimeTypeAdapter
import com.kolibree.android.commons.gson.ZonedDateTimeTypeAdapter
import com.kolibree.android.network.retrofit.CustomTypeAdapterFactory
import java.util.Date
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime

class CustomTypeAdapterFactoryTest {

    @Test
    fun createTypeAdapterFromLocalDateFactory() {
        val typeToken = TypeToken.getParameterized(LocalDate::class.java)
        val result = CustomTypeAdapterFactory.FACTORY.create(Gson(), typeToken).javaClass

        assertTrue(result.isAssignableFrom(LocalDateTypeAdapter::class.java))
    }

    @Test
    fun createTypeAdapterFromZonedDateTime() {
        val typeToken = TypeToken.getParameterized(ZonedDateTime::class.java)
        val result = CustomTypeAdapterFactory.FACTORY.create(Gson(), typeToken).javaClass
        assertTrue(result.isAssignableFrom(ZonedDateTimeTypeAdapter::class.java))
    }

    @Test
    fun createTypeAdapterFromOffsetDateTime() {
        val typeToken = TypeToken.getParameterized(OffsetDateTime::class.java)
        val result = CustomTypeAdapterFactory.FACTORY.create(Gson(), typeToken).javaClass
        assertTrue(result.isAssignableFrom(OffsetDateTimeTypeAdapter::class.java))
    }

    @Test
    fun createTypeAdapterFromUnknownType() {
        val typeToken = TypeToken.getParameterized(Date::class.java)
        val result = CustomTypeAdapterFactory.FACTORY.create(Gson(), typeToken)
        assertNull(result)
    }
}
