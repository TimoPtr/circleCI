/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GetLinksParseTest : BaseMockWebServerTest<AmazonDashApi>() {

    override fun retrofitServiceClass(): Class<AmazonDashApi> = AmazonDashApi::class.java

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testGetLinks() {
        val jsonResponse = SharedTestUtils.getJson("amazon_dash_get_links_response.json")
        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val body = retrofitService()
            .getLinks()
            .blockingGet()
            .body()

        val expectedBody = AmazonDashGetLinkResponse(
            appUrl = "https://sample.appurl.com",
            fallbackUrl = "https://sample.fallbackurl.com"
        )
        assertEquals(expectedBody, body)
    }
}
