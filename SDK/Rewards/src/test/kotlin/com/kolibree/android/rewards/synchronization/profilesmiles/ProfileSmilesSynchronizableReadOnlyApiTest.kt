/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmiles

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.models.ProfileSmilesEntity
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

internal class ProfileSmilesSynchronizableReadOnlyApiTest : BaseUnitTest() {
    @Mock
    lateinit var rewardsApi: RewardsApi

    private lateinit var profileSmilesApi: ProfileSmilesSynchronizableReadOnlyApi

    override fun setup() {
        super.setup()

        profileSmilesApi = ProfileSmilesSynchronizableReadOnlyApi(rewardsApi)
    }

    @Test(expected = ApiError::class)
    fun `get throws backend error when response is not successful`() {
        val response = Response.error<ProfileSmilesApi>(404, ResponseBody.create(null, "ignored"))

        val profileId = 78L
        val call = mock<Call<ProfileSmilesApi>>()
        whenever(call.execute()).thenReturn(response)
        whenever(rewardsApi.getProfileSmiles(profileId)).thenReturn(call)

        profileSmilesApi.get(profileId)
    }

    @Test
    fun `get maps to ProfileSmilesProfileCatalogInternal when response is successful`() {
        val expectedSmiles = 7
        val expectedResponse = ProfileSmilesApi(expectedSmiles)

        mockResponse(Response.success(expectedResponse))

        val expectedProfileId = 78L

        val expectedProfileSmilesEntity = ProfileSmilesEntity(expectedProfileId, expectedSmiles)

        assertEquals(expectedProfileSmilesEntity, profileSmilesApi.get(expectedProfileId))
    }

    private fun mockResponse(response: Response<ProfileSmilesApi>) {
        whenever(rewardsApi.getProfileSmiles(any())).thenReturnResponse(response)
    }
}
