/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.transfer

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.models.Transfer
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

class TransferNetworkServiceTest : BaseUnitTest() {
    private val rewardsApi: RewardsApi = mock()

    private val transferNetworkService = TransferNetworkServiceImpl(rewardsApi)

    private val transferData = TransferData(100, 1L, 1L)

    @Test(expected = ApiError::class)
    fun `error response throws backend error`() {
        mockResponse(Response.error(404, ResponseBody.create(null, "ignored")))

        transferNetworkService.transferSmiles(transferData)
    }

    @Test
    fun `error 500 with message as JSON malformed response throws TransferHttpException no message`() {
        try {
            mockResponse(Response.error(500, ResponseBody.create(null, "ignored")))

            transferNetworkService.transferSmiles(transferData)

            TestCase.fail()
        } catch (e: TransferHttpException) {
            TestCase.assertNull(e.userDisplayMessage)
        }
    }

    @Test
    fun `error 500 empty message throws TransferHttpException with no message`() {
        try {
            mockResponse(Response.error(500, ResponseBody.create(null, "")))

            transferNetworkService.transferSmiles(transferData)

            TestCase.fail()
        } catch (e: TransferHttpException) {
            TestCase.assertNull(e.userDisplayMessage)
        }
    }

    @Test
    fun `error 500 with message as JSON throws TransferHttpException with message`() {
        try {
            mockResponse(Response.error(500, ResponseBody.create(null, "{ \"error\": \"custom message\"}")))

            transferNetworkService.transferSmiles(transferData)

            TestCase.fail()
        } catch (e: TransferHttpException) {
            TestCase.assertEquals("custom message", e.userDisplayMessage)
        }
    }

    @Test(expected = EmptyBodyException::class)
    fun `success response null body throws EmptyBodyException`() {
        mockResponse(Response.success(null))

        transferNetworkService.transferSmiles(transferData)
    }

    @Test
    fun `success response returns Transfer`() {
        val apiBody = TransferApi(101, "Good result", 1L, 1L)
        mockResponse(Response.success(apiBody))

        val expectedValue = Transfer(
            apiBody.smiles, apiBody.fromProfileId, apiBody.toProfileId
        )

        TestCase.assertEquals(expectedValue, transferNetworkService.transferSmiles(transferData))
    }

    private fun mockResponse(response: Response<TransferApi>) {
        val call = mock<Call<TransferApi>>()
        whenever(call.execute()).thenReturn(response)

        whenever(rewardsApi.transferSmiles(transferData)).thenReturn(call)
    }
}
