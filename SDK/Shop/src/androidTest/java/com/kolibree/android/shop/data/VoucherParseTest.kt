/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.shop.data.api.VoucherApi
import com.kolibree.android.shop.data.api.model.VoucherRequest
import com.kolibree.android.shop.data.api.model.VoucherResponse
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class VoucherParseTest : BaseMockWebServerTest<VoucherApi>() {
    override fun retrofitServiceClass(): Class<VoucherApi> = VoucherApi::class.java

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testGetVoucherResponse() {
        val jsonResponse = SharedTestUtils.getJson("voucher/response_voucher.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().getVoucher(1, VoucherRequest("fr", true)).blockingGet().body()!!

        val expected = VoucherResponse(
            "hello world I'm a voucher"
        )

        assertEquals(expected, response)
    }
}
