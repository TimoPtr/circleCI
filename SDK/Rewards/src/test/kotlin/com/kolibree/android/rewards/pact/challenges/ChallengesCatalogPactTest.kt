package com.kolibree.android.rewards.pact.challenges

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okhttp3.OkHttpClient

class ChallengesCatalogPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    private val catalogBody = SharedTestUtils.getJson("catalog/list/catalog_list.json")

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch challenges catalog")
            .withPathAndDefaultHeader("/v1/rewards/catalog/list/")
            .method("GET")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .status(200)
            .body(catalogBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            RewardsApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.getChallengesCatalog().execute()

        val challengeCatalog = response.body()!!

        assertNotNull(response)

        assertEquals("EN", challengeCatalog.language)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
