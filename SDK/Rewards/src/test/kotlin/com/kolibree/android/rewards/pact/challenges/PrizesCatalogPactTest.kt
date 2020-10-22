/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.pact.challenges

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertNotNull
import okhttp3.OkHttpClient

class PrizesCatalogPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch prizes catalog")
            .withPathAndDefaultHeader("/v1/rewards/prize/list/*/")
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
        val response = client.getAllPrizes().execute()

        val prizesCatalog = response.body()!!

        assertNotNull(prizesCatalog)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }

    private val catalogBody = LambdaDsl.newJsonBody { root ->
        root.array("rewards") {
            it.`object` { item ->
                item.numberValue("category_id", 1)
                item.stringValue("category", "rewards")
                item.array("details") { detailsArray ->
                    detailsArray.`object` { details ->
                        details.numberValue("rewards_id", 1)
                        details.stringValue("title", "GoPirate Gold Amplifier")
                        details.stringValue(
                            "description",
                            "You want to get this new ship faster? Speed up your GoPirate progression with an in-game Gold Booster. This booster increases your GoPirate Gold by 1000."
                        )
                        details.numberValue("smiles_required", 500)
                        details.stringValue(
                            "picture_url",
                            "https://dyg43gvcnlkrm.cloudfront.net/rewards/PirateBooster.png"
                        )
                        details.numberValue("voucher_discount", 0.0)
                        details.nullValue("product_id")
                        details.stringValue("company", "kolibree")
                        details.booleanValue("purchasable", true)
                        details.stringMatcher(
                            "creation_date",
                            state.creationDateRegexp,
                            "2020-02-13"
                        )
                    }
                }
            }
        }
    }.build()
}
