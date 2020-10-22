/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.charts.inoff.data.api.InOffBrushingsCountApi
import com.kolibree.charts.inoff.data.api.model.InOffBrushingsCountResponse
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

internal class InOffBrushingsCountSynchronizableReadOnlyApiTest : BaseUnitTest() {
    private val accountId: Long = 99L
    private val profileId: Long = 100L

    private val api: InOffBrushingsCountApi = mock()

    private val accountDatastore: AccountDatastore = mock()

    private lateinit var syncApi: InOffBrushingsCountSynchronizableReadOnlyApi

    override fun setup() {
        super.setup()

        syncApi = InOffBrushingsCountSynchronizableReadOnlyApi(api, accountDatastore)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(createAccountInternal(id = accountId)))
    }

    @Test
    fun `get returns valid in off count entity`() {
        val response = InOffBrushingsCountResponse(1, 11)

        mockResponse(Response.success(response))

        val expectedValue =
            InOffBrushingsCountEntity(
                profileId = profileId,
                offlineBrushingCount = response.offTotal,
                onlineBrushingCount = response.inTotal
            )

        assertEquals(expectedValue, syncApi.get(profileId))
    }

    @Test(expected = NoAccountException::class)
    fun `get throw NoAccountException when account is null`() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.error(RuntimeException()))

        syncApi.get(profileId)
    }

    @Test(expected = EmptyBodyException::class)
    fun `get throws EmptyBodyException when response isSuccessful and body is null`() {
        val response = mock<Response<InOffBrushingsCountResponse>>()
        mockResponse(response)
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(null)

        syncApi.get(profileId)
    }

    @Test(expected = ApiError::class)
    fun `get throw ApiError when not 200`() {
        val expectedCode = 404
        val responseBody = ResponseBody.create(null, "")
        val expectedResponse: Response<InOffBrushingsCountResponse> =
            Response.error(expectedCode, responseBody)

        mockResponse(expectedResponse)
        syncApi.get(profileId)
    }

    private fun mockResponse(response: Response<InOffBrushingsCountResponse>) {
        val call = mock<Call<InOffBrushingsCountResponse>>()

        whenever(call.execute()).thenReturn(response)
        whenever(api.getInOffBrushingsCount(accountId, profileId)).thenReturn(call)
    }
}
