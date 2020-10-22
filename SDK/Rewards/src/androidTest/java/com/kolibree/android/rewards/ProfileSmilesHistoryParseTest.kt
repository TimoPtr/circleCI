/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ProfileSmilesHistoryParseTest : BaseMockWebServerTest<RewardsApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<RewardsApi> {
        return RewardsApi::class.java
    }

    @Test
    fun parseResponse() {
        val jsonResponse = SharedTestUtils.getJson("catalog/list/profile_smiles_history.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val parsedProfileSmilesHistoryApi = retrofitService().getSmilesHistory(1).execute().body()!!

        assertEquals(7, parsedProfileSmilesHistoryApi.smilesProfileHistory.size)

        // actual parsing is tested in PactTest
    }
}
