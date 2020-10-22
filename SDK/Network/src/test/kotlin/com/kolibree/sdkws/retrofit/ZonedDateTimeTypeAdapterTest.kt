package com.kolibree.sdkws.retrofit

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.gson.ZonedDateTimeTypeAdapter
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ZonedDateTimeTypeAdapterTest : BaseUnitTest() {

    private val expectedZonedDateTime =
        ZonedDateTime.of(2018, 5, 29, 16, 22, 55, 0, ZoneId.of("+02:00"))
    private val adapter = ZonedDateTimeTypeAdapter()

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    @Test
    fun verifyCanWriteDate() {
        adapter.toJsonTree(expectedZonedDateTime)
    }

    /*
    READ
     */

    @Test
    fun `read incorrect date format returns null`() {
        val json = SharedTestUtils.getJson("json/date/incorrect_date_format.json")

        val fromJson = adapter.fromJson(json)

        assertNull("Value is $fromJson", fromJson)
    }

    @Test
    fun `read correct date format returns expected zone`() {
        val json = SharedTestUtils.getJson("json/date/correct_zoneddate_format.json")
        val actualZonedDateTime = adapter.fromJson(json)
        assertThat(actualZonedDateTime, `is`(expectedZonedDateTime))
    }

    /*
    WRITE
     */

    @Test
    fun `write null writes empty string`() {
        assertEquals("", adapter.toJson(null))
    }

    @Test
    fun `write correct format writes expected json`() {
        val expectedJson = SharedTestUtils.getJson("json/date/correct_zoneddate_format.json")
        assertEquals(expectedJson, adapter.toJson(expectedZonedDateTime))
    }
}
