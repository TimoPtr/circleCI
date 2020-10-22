package com.kolibree.android.rewards

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.rewards.synchronization.CHALLENGE_PROGRESS_DATETIME_FORMATTER
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeCompletionDetails
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZonedDateTime

@RunWith(AndroidJUnit4::class)
internal class ChallengeProgressCatalogParseTest : BaseMockWebServerTest<RewardsApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<RewardsApi> {
        return RewardsApi::class.java
    }

    @Test
    fun parseResponse() {
        val jsonResponse = SharedTestUtils.getJson("rewards/challenges/profile/list/challenge_progress_fr.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val profileId = 9L
        val challengeProgressApi = retrofitService().getChallengeProgress(profileId).execute().body()!!

        assertEquals(83, challengeProgressApi.totalSmiles)
        assertEquals("FR", challengeProgressApi.progress.language)

        val firstCategory = challengeProgressApi.progress.catalog.first()
        assertEquals("Débutant", firstCategory.category)
        assertEquals(5, firstCategory.categoryId)
        assertEquals(6, firstCategory.challenges.size)

        /*
            "completion_time": "",
            "completion_details": null,
            "group": null,
            "smiles_reward": 3,
            "challenge_id": 6,
            "percentage": 0,
            "picture_url": "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_FirstSmartBrushingAnalyzer.png",
            "challenge_name": "Premier Analyseur de Brossage Intelligent"
         */
        firstCategory.challenges.first().apply {
            assertEquals(null, completionTime)
            assertEquals(null, completionDetails)
            assertEquals(null, group)
            assertEquals(3, smilesReward)
            assertEquals(6, challengeId)
            assertEquals(0, percentage)
            assertEquals(
                "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_FirstSmartBrushingAnalyzer.png",
                pictureUrl
            )
            assertEquals("Premier Analyseur de Brossage Intelligent", challengeName)
        }

        /*
        {
            "completion_time": "2018-12-04 17:41:24.653342+00:00",
            "completion_details": {
              "completion": 6,
              "rules": 600
            },
            "group": 3,
            "smiles_reward": 10,
            "challenge_id": 17,
            "percentage": 67,
            "picture_url": "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_PirateRunner_10.png",
            "challenge_name": "Pirate 10"
          }
         */
        challengeProgressApi.progress.catalog[1].challenges.first().apply {
            assertEquals(
                ZonedDateTime.parse("2018-12-04 17:41:24.653342+00:00", CHALLENGE_PROGRESS_DATETIME_FORMATTER),
                completionTime
            )
            assertEquals(ChallengeCompletionDetails(6, 600), completionDetails)
            assertEquals(3, group)
            assertEquals(10, smilesReward)
            assertEquals(17, challengeId)
            assertEquals(67, percentage)
            assertEquals(
                "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_PirateRunner_10.png",
                pictureUrl
            )
            assertEquals("Pirate 10", challengeName)
        }

        assertEquals(23, challengeProgressApi.progress.catalog.map { it.challenges }.flatten().size)
    }
}
