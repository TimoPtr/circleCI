/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.redeem

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.models.Redeem
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.fail
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

class RedeemNetworkServiceTest : BaseUnitTest() {
    private val rewardsApi: RewardsApi = mock()

    private val redeemNetworkService = RedeemNetworkServiceImpl(rewardsApi)

    private val redeemData = RedeemData(1, 1)

    @Test(expected = ApiError::class)
    fun `error response throws backend error`() {
        mockResponse(Response.error(404, ResponseBody.create(null, "ignored")))

        redeemNetworkService.claimRedeem(redeemData)
    }

    @Test
    fun `error 500 with message as JSON malformed response throws RedeemHttpException no message`() {
        try {
            mockResponse(Response.error(500, ResponseBody.create(null, "ignored")))

            redeemNetworkService.claimRedeem(redeemData)

            fail()
        } catch (e: RedeemHttpException) {
            assertNull(e.userDisplayMessage)
        }
    }

    @Test
    fun `error 500 empty message throws RedeemHttpException with no message`() {
        try {
            mockResponse(Response.error(500, ResponseBody.create(null, "")))

            redeemNetworkService.claimRedeem(redeemData)

            fail()
        } catch (e: RedeemHttpException) {
            assertNull(e.userDisplayMessage)
        }
    }

    @Test
    fun `error 500 with message as JSON throws RedeemHttpException with message`() {
        try {
            mockResponse(Response.error(500, ResponseBody.create(null, "{ \"error\": \"custom message\"}")))

            redeemNetworkService.claimRedeem(redeemData)

            fail()
        } catch (e: RedeemHttpException) {
            assertEquals("custom message", e.userDisplayMessage)
        }
    }

    @Test(expected = EmptyBodyException::class)
    fun `success response null body throws EmptyBodyException`() {
        mockResponse(Response.success(null))

        redeemNetworkService.claimRedeem(redeemData)
    }

    @Test
    fun `success response returns Redeem`() {
        val apiBody = RedeemApi("url", "success", 1L)
        mockResponse(Response.success(apiBody))

        val expectedValue = Redeem(
            apiBody.redeemUrl, apiBody.result, apiBody.rewardsId
        )

        assertEquals(expectedValue, redeemNetworkService.claimRedeem(redeemData))
    }

    @Test
    fun `success response without url returns Redeem without url`() {
        val apiBody = RedeemApi(null, "success", 1L)
        mockResponse(Response.success(apiBody))

        val expectedValue = Redeem(
            apiBody.redeemUrl, apiBody.result, apiBody.rewardsId
        )

        assertEquals(expectedValue, redeemNetworkService.claimRedeem(redeemData))
    }

    private fun mockResponse(response: Response<RedeemApi>) {
        val call = mock<Call<RedeemApi>>()
        whenever(call.execute()).thenReturn(response)

        whenever(rewardsApi.claimRedeem(redeemData)).thenReturn(call)
    }
}
