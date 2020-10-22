package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KLTBFileCrcTest : BaseUnitTest() {

    @Test(expected = FileCrcParseException::class)
    fun create_emptyList_throwsFileCrcParseException() {
        KLTBFileCrc(listOf())
    }

    @Test
    fun create_listOfSizeBelow4_throwsFileCrcParseException() {
        var exceptionCounter = 0
        val expectedExceptions = 3
        (0.until(expectedExceptions)).forEach {
            try {
                KLTBFileCrc(it.downTo(0).map { it.toByte() })
            } catch (e: FileCrcParseException) {
                exceptionCounter = exceptionCounter.inc()
            }
        }

        assertEquals(expectedExceptions, exceptionCounter)
    }

    @Test(expected = FileCrcParseException::class)
    fun create_size5_throwsFileCrcParseException() {
        KLTBFileCrc(5.downTo(0).map { it.toByte() })
    }
}
