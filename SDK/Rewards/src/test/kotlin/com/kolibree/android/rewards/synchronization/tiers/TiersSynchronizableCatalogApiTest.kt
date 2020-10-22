/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.tiers

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.thenReturnResponse
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mock
import retrofit2.Response

internal class TiersSynchronizableCatalogApiTest : BaseUnitTest() {

    @Mock
    lateinit var rewardsApi: RewardsApi

    private lateinit var tiersCatalogApi: TiersSynchronizableCatalogApi

    override fun setup() {
        super.setup()

        tiersCatalogApi = TiersSynchronizableCatalogApi(rewardsApi)
    }

    @Test
    fun get_successfulResponse_returnsExpectedBody() {
        val expectedCatalog = TiersCatalogApi(mapOf())

        mockResponse(Response.success(expectedCatalog))

        assertEquals(expectedCatalog, tiersCatalogApi.get())
    }

    @Test(expected = ApiError::class)
    fun get_errorResponse_throwsBackendError() {
        val expectedCode = 404
        val responseBody = ResponseBody.create(null, "ignored")
        val expectedResponse: Response<TiersCatalogApi> = Response.error(expectedCode, responseBody)

        mockResponse(expectedResponse)
        tiersCatalogApi.get()
    }

    private fun mockResponse(response: Response<TiersCatalogApi>) {
        whenever(rewardsApi.getTiersCatalog()).thenReturnResponse(response)
    }
}
