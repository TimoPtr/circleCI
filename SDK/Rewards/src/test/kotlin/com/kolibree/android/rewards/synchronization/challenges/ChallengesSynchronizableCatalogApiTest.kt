package com.kolibree.android.rewards.synchronization.challenges

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

internal class ChallengesSynchronizableCatalogApiTest : BaseUnitTest() {

    @Mock
    lateinit var rewardsApi: RewardsApi

    private lateinit var challengesCatalogApi: ChallengesSynchronizableCatalogApi

    override fun setup() {
        super.setup()

        challengesCatalogApi = ChallengesSynchronizableCatalogApi(rewardsApi)
    }

    @Test
    fun get_successfulResponse_returnsExpectedBody() {
        val expectedCatalog = ChallengesCatalogApi(listOf(), language = "EN")

        mockResponse(Response.success(expectedCatalog))

        assertEquals(expectedCatalog, challengesCatalogApi.get())
    }

    @Test(expected = ApiError::class)
    fun get_errorResponse_throwsBackendError() {
        val expectedCode = 404
        val responseBody = ResponseBody.create(null, "ignored")
        val expectedResponse: Response<ChallengesCatalogApi> = Response.error(expectedCode, responseBody)

        mockResponse(expectedResponse)
        challengesCatalogApi.get()
    }

    private fun mockResponse(response: Response<ChallengesCatalogApi>) {
        whenever(rewardsApi.getChallengesCatalog()).thenReturnResponse(response)
    }
}
