/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.pact.tiers

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okhttp3.OkHttpClient

class TiersCatalogPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch tiers catalog")
            .withPathAndDefaultHeader("/v1/rewards/tier/list/")
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

        val client = buildRetrofitClient(mockServer.getUrl(), RewardsApi::class.java, okHttpClient = okHttpClient())
        val response = client.getTiersCatalog().execute()

        val tiersCatalog = response.body()!!

        assertNotNull(response)
        assertEquals(5, tiersCatalog.tiers.size)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }

    private val catalogBody = newJsonBody { root ->
        root.`object`("tier_list") {
            it.`object`("1") { item ->
                item.stringValue("rank", "Ivory")
                item.numberValue("challenges_needed", 0)
                item.numberValue("smiles_reward_per_brushing", 1)
                item.stringValue("picture_url", "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Ivory.png")
                item.stringValue("message", "Congratulations! You reached Ivory tier")
                item.stringMatcher(
                    "creation_date",
                    state.creationDateRegexp,
                    "2020-02-13"
                )
            }
            it.`object`("2") { item ->
                item.stringValue("rank", "Bronze")
                item.numberValue("challenges_needed", 10)
                item.numberValue("smiles_reward_per_brushing", 2)
                item.stringValue("picture_url", "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Bronze.png")
                item.stringValue("message", "Congratulations! You reached Bronze tier")
                item.stringMatcher(
                    "creation_date",
                    state.creationDateRegexp,
                    "2020-02-13"
                )
            }
            it.`object`("3") { item ->
                item.stringValue("rank", "Silver")
                item.numberValue("challenges_needed", 20)
                item.numberValue("smiles_reward_per_brushing", 3)
                item.stringValue("picture_url", "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Silver.png")
                item.stringValue("message", "Congratulations! You reached Silver tier")
                item.stringMatcher(
                    "creation_date",
                    state.creationDateRegexp,
                    "2020-02-13"
                )
            }
            it.`object`("4") { item ->
                item.stringValue("rank", "Gold")
                item.numberValue("challenges_needed", 30)
                item.numberValue("smiles_reward_per_brushing", 4)
                item.stringValue("picture_url", "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Gold.png")
                item.stringValue("message", "Congratulations! You reached Gold tier")
                item.stringMatcher(
                    "creation_date",
                    state.creationDateRegexp,
                    "2020-02-13"
                )
            }
            it.`object`("5") { item ->
                item.stringValue("rank", "Platinum")
                item.numberValue("challenges_needed", 40)
                item.numberValue("smiles_reward_per_brushing", 5)
                item.stringValue("picture_url", "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Platinum.png")
                item.stringValue("message", "Congratulations! You reached Platinum tier")
                item.stringMatcher(
                    "creation_date",
                    state.creationDateRegexp,
                    "2020-02-13"
                )
            }
        }
    }.build()
}
