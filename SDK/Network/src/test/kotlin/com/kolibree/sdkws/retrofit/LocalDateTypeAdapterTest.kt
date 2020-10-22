package com.kolibree.sdkws.retrofit

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.gson.LocalDateTypeAdapter
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import junit.framework.TestCase.assertNull
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.threeten.bp.LocalDate

class LocalDateTypeAdapterTest : BaseUnitTest() {

    private val expectedLocalDate = LocalDate.of(1983, 12, 12)
    private val adapter = LocalDateTypeAdapter().nullSafe()

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
        adapter.toJsonTree(expectedLocalDate)
    }

    @Test
    fun readIncorrectDateFormat() {
        val json = SharedTestUtils.getJson("json/date/incorrect_date_format.json")
        val actualLocalDate = adapter.fromJson(json)
        assertNull(actualLocalDate)
    }

    @Test
    fun readCorrectDateFormat() {
        val json = SharedTestUtils.getJson("json/date/correct_localdate_format.json")
        val actualLocalDate = adapter.fromJson(json)
        assertThat(actualLocalDate, `is`(expectedLocalDate))
    }
}
