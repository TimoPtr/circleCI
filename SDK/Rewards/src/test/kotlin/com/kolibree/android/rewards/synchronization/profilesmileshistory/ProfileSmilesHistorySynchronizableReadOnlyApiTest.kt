/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

class ProfileSmilesHistorySynchronizableReadOnlyApiTest : BaseUnitTest() {
    private val rewardsApi: RewardsApi = mock()

    private val profileSmilesHistoryApi = ProfileSmilesHistorySynchronizableReadOnlyApi(rewardsApi)

    @Test(expected = ApiError::class)
    fun `error response throws backend error`() {
        mockResponse(Response.error(404, ResponseBody.create(null, "ignored")))

        profileSmilesHistoryApi.get(ProfileBuilder.DEFAULT_ID)
    }

    @Test(expected = EmptyBodyException::class)
    fun `success response null body throws EmptyBodyException`() {
        mockResponse(Response.success(null))

        profileSmilesHistoryApi.get(ProfileBuilder.DEFAULT_ID)
    }

    @Test
    fun `success response returns ProfileSmilesHistoryApiWithProfileId`() {
        val apiBody = mock<ProfileSmilesHistoryApi>()
        mockResponse(Response.success(apiBody))

        val expectedValue = ProfileSmilesHistoryApiWithProfileId(
            profileId = ProfileBuilder.DEFAULT_ID,
            profileSmilesHistoryApi = apiBody
        )

        assertEquals(expectedValue, profileSmilesHistoryApi.get(ProfileBuilder.DEFAULT_ID))
    }

    private fun mockResponse(response: Response<ProfileSmilesHistoryApi>) {
        val call = mock<Call<ProfileSmilesHistoryApi>>()
        whenever(call.execute()).thenReturn(response)

        whenever(rewardsApi.getSmilesHistory(ProfileBuilder.DEFAULT_ID)).thenReturn(call)
    }
}
