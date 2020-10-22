/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.sync

import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.lifetimesmiles.LifetimeSmilesResponse
import com.kolibree.android.rewards.synchronization.lifetimesmiles.LifetimeSmilesSynchronizableReadOnlyApi
import com.kolibree.android.rewards.synchronization.thenReturnResponse
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.kolibree.android.test.mocks.createAccountInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

internal class LifetimeSmilesReadOnlyApiTest : BaseUnitTest() {

    private val accountId: Long = 99L

    private val rewardsApi: RewardsApi = mock()

    private val accountDatastore: AccountDatastore = mock()

    private lateinit var statsApi: LifetimeSmilesSynchronizableReadOnlyApi

    override fun setup() {
        super.setup()

        statsApi = LifetimeSmilesSynchronizableReadOnlyApi(accountDatastore, rewardsApi)

        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(createAccountInternal(id = accountId)))
    }

    @Test
    fun get_successfulResponse_returnsExpectedBody() {
        val response = LifetimeSmilesResponse(5)

        mockResponse(Response.success(response))

        val profileId = 6L
        val expectedValue =
            LifetimeSmilesEntity(
                profileId = profileId,
                lifetimePoints = response.lifetimePoints
            )

        assertEquals(expectedValue, statsApi.get(profileId))
    }

    @Test(expected = EmptyBodyException::class)
    fun `get throws EmptyBodyException when response isSucessful and body is null`() {
        val response = mock<Response<LifetimeSmilesResponse>>()
        mockResponse(response)
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(null)

        statsApi.get(1)
    }

    @Test(expected = ApiError::class)
    fun get_errorResponse_throwsBackendError() {
        val expectedCode = 404
        val responseBody = ResponseBody.create(null, "")
        val expectedResponse: Response<LifetimeSmilesResponse> =
            Response.error(expectedCode, responseBody)

        mockResponse(expectedResponse)
        statsApi.get(1)
    }

    private fun mockResponse(response: Response<LifetimeSmilesResponse>) {
        whenever(rewardsApi.getLifetimePoints(any(), any())).thenReturnResponse(response)
    }
}
