/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profiletier

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.models.ProfileTierEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.thenReturnResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mock
import retrofit2.Call
import retrofit2.Response

internal class ProfileTierSynchronizableReadOnlyApiTest : BaseUnitTest() {
    @Mock
    lateinit var rewardsApi: RewardsApi

    private lateinit var profileTierApi: ProfileTierSynchronizableReadOnlyApi

    override fun setup() {
        super.setup()

        profileTierApi = ProfileTierSynchronizableReadOnlyApi(rewardsApi)
    }

    @Test(expected = ApiError::class)
    fun `get throws backend error when response is not successful`() {
        val response = Response.error<ProfileTierApi>(404, ResponseBody.create(null, "ignored"))

        val profileId = 78L
        val call = mock<Call<ProfileTierApi>>()
        whenever(call.execute()).thenReturn(response)
        whenever(rewardsApi.getProfileTier(profileId)).thenReturn(call)

        profileTierApi.get(profileId)
    }

    @Test
    fun `get maps to ProfileTierProfileCatalogInternal when response is successful`() {
        val expectedTierId = 7
        val expectedResponse = ProfileTierApi("Wood", "http://www.example.com", expectedTierId)

        mockResponse(Response.success(expectedResponse))

        val expectedProfileId = 78L

        val expectedProfileTierEntity = ProfileTierEntity(expectedProfileId, expectedTierId)

        assertEquals(expectedProfileTierEntity, profileTierApi.get(expectedProfileId))
    }

    private fun mockResponse(response: Response<ProfileTierApi>) {
        whenever(rewardsApi.getProfileTier(any())).thenReturnResponse(response)
    }
}
