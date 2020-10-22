/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.prizes

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

internal class PrizesSynchronizableCatalogApiTest : BaseUnitTest() {
    private val rewardsApi: RewardsApi = mock()

    private val prizesCatalogApi = PrizesSynchronizableCatalogApi(rewardsApi)

    @Test(expected = ApiError::class)
    fun `error response throws backend error`() {
        mockResponse(Response.error(404, ResponseBody.create(null, "ignored")))

        prizesCatalogApi.get()
    }

    @Test(expected = EmptyBodyException::class)
    fun `success response null body throws EmptyBodyException`() {
        mockResponse(Response.success(null))

        prizesCatalogApi.get()
    }

    @Test
    fun `success response returns response body`() {
        val expectedBody = mock<PrizesCatalogApi>()
        mockResponse(Response.success(expectedBody))

        assertEquals(expectedBody, prizesCatalogApi.get())
    }

    private fun mockResponse(response: Response<PrizesCatalogApi>) {
        val call = mock<Call<PrizesCatalogApi>>()
        whenever(call.execute()).thenReturn(response)

        whenever(rewardsApi.getAllPrizes()).thenReturn(call)
    }
}
