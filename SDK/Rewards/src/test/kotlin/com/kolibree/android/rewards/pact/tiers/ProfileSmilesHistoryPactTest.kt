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
import com.kolibree.android.rewards.synchronization.SMILES_DEFAULT_NB_RESULTS
import com.kolibree.android.rewards.test.createSmilesHistoryEventApi
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

class ProfileSmilesHistoryPactTest :
    PactBaseTest<PactProviderState.TwoProfileAccountWithSmilesHistoryExists>(
        PactProviderState.TwoProfileAccountWithSmilesHistoryExists
    ) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch profile smiles history")
            .withPathAndDefaultHeader("/v1/rewards/profile/history/${state.profileId}/all/$SMILES_DEFAULT_NB_RESULTS/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(mockServer.getUrl(), RewardsApi::class.java)
        val response = client.getSmilesHistory(state.profileId).execute()

        val profileSmilesApi = response.body()!!

        assertEquals(7, profileSmilesApi.smilesProfileHistory.size)

        val challengeCompleted = createSmilesHistoryEventApi(
            eventType = "Challenge completed",
            message = "You have completed a challenge",
            smilesRewards = 1,
            challengeId = 1
        )

        val brushingSession = createSmilesHistoryEventApi(
            eventType = "Brushing session",
            message = "You have a brushing session: Coach",
            smilesRewards = 2,
            brushingType = "Coach",
            brushingId = 2
        )

        val tierReached = createSmilesHistoryEventApi(
            eventType = "Tier reached",
            message = "You have reached Silver Tier",
            smilesRewards = 3,
            tierLevel = 2
        )

        val smilesRedeemed = createSmilesHistoryEventApi(
            eventType = "Smiles redeemed",
            message = "You have redeemed smiles",
            smilesRewards = 4,
            rewardsId = 2
        )

        val smilesTransfer = createSmilesHistoryEventApi(
            eventType = "Smiles transfer",
            message = "You have redeemed smiles",
            smilesRewards = 5,
            relatedProfileId = state.secondProfileId
        )

        val crownCompleted = createSmilesHistoryEventApi(
            eventType = "Crown completed",
            message = "You have completed a crown",
            smilesRewards = 6,
            relatedProfileId = state.profileId
        )

        val streakCompleted = createSmilesHistoryEventApi(
            eventType = "Streak completed",
            message = "You have completed a streak",
            smilesRewards = 7,
            relatedProfileId = state.profileId
        )

        val historyEvents = profileSmilesApi.smilesProfileHistory

        assertTrue(historyEvents.contains(brushingSession))
        assertTrue(historyEvents.contains(challengeCompleted))
        assertTrue("Expected $tierReached, was $historyEvents", historyEvents.contains(tierReached))
        assertTrue(historyEvents.contains(smilesRedeemed))
        assertTrue(historyEvents.contains(smilesTransfer))
        assertTrue(historyEvents.contains(crownCompleted))
        assertTrue(historyEvents.contains(streakCompleted))
    }

    private val responseBody = newJsonBody { root ->
        root.array("smiles_profile_history") {
            it.`object` { item ->
                item.stringValue("event_type", "Challenge completed")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have completed a challenge")
                item.numberValue("smiles_rewards", 1)
                item.numberValue("challenge_id", 1)
            }
            it.`object` { item ->
                item.stringValue("event_type", "Brushing session")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have a brushing session: Coach")
                item.numberValue("smiles_rewards", 2)
                item.stringValue("brushing_type", "Coach")
                item.numberValue("brushing_id", 2)
            }
            it.`object` { item ->
                item.stringValue("event_type", "Tier reached")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have reached Silver Tier")
                item.numberValue("smiles_rewards", 3)
                item.numberValue("tier_id", 2)
            }
            it.`object` { item ->
                item.stringValue("event_type", "Smiles redeemed")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have redeemed smiles")
                item.numberValue("smiles_rewards", 4)
                item.numberValue("rewards_id", 2)
            }
            it.`object` { item ->
                item.stringValue("event_type", "Smiles transfer")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have redeemed smiles")
                item.numberValue("smiles_rewards", 5)
                item.numberValue("profile_id", state.secondProfileId)
            }
            it.`object` { item ->
                item.stringValue("event_type", "Crown completed")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have completed a crown")
                item.numberValue("smiles_rewards", 6)
                item.numberValue("profile_id", state.profileId)
            }
            it.`object` { item ->
                item.stringValue("event_type", "Streak completed")
                item.stringMatcher(
                    "creation_time",
                    state.creationTimeRegexp,
                    "2018-12-20T12:50:24.361950+00:00"
                )
                item.stringValue("message", "You have completed a streak")
                item.numberValue("smiles_rewards", 7)
                item.numberValue("profile_id", state.profileId)
            }
        }
    }.build()
}
