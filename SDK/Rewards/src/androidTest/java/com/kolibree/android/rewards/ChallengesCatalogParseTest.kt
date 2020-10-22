package com.kolibree.android.rewards

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.rewards.models.ChallengeEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChallengesCatalogParseTest : BaseMockWebServerTest<RewardsApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<RewardsApi> {
        return RewardsApi::class.java
    }

    @Test
    fun parseResponse() {
        val jsonResponse = SharedTestUtils.getJson("catalog/list/catalog_list.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val challengesCatalogInternal = retrofitService().getChallengesCatalog().execute().body()!!

        assertEquals("EN", challengesCatalogInternal.language)

        val categories = challengesCatalogInternal.categories
        assertEquals(6, categories.size)

        val discoverConnectCategory = categories.firstOrNull { it.name == "Discover Connect" }!!
        assertNotNull(discoverConnectCategory)
        assertNotNull(categories.firstOrNull { it.name == "Expert Smile" })
        assertNotNull(categories.firstOrNull { it.name == "Freestyle your Smile" })
        assertNotNull(categories.firstOrNull { it.name == "Keep good habits" })
        assertNotNull(categories.firstOrNull { it.name == "Special Smile" })
        assertNotNull(categories.firstOrNull { it.name == "Spread the smile" })

        assertEquals(7, discoverConnectCategory.challenges.size)

        val challengeId2 = ChallengeEntity(
            name = "1st Coach+",
            id = 2,
            action = "Coach+",
            description = "Try to brush your teeth with Coach+",
            greetingMessage = "Congratulations! You've done your first brushing session with Coach+.",
            smilesReward = 5,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_Coach%2B_1st.png",
            internalCategory = null
        )

        val challengeId3 = ChallengeEntity(
            name = "1st Pirate",
            id = 3,
            action = "Pirate",
            description = "Try to brush your teeth with the Go Pirate! game",
            greetingMessage = "Congratulations! You've completed your first brushing session with the Go Pirate! game",
            smilesReward = 5,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_GoP.png",
            internalCategory = null
        )

        val challengeId4 = ChallengeEntity(
            name = "1st Rabbids",
            id = 4,
            action = "Rabbids",
            description = "Try to brush your teeth with the Rabbids game",
            greetingMessage = "Congratulations! You've done your first brushing session with the Rabbids game.",
            smilesReward = 5,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Badge_Rab.png",
            internalCategory = null
        )

        assertEquals(challengeId2, discoverConnectCategory.challenges.first { it.id == 2L })
        assertEquals(challengeId3, discoverConnectCategory.challenges.first { it.id == 3L })
        assertEquals(challengeId4, discoverConnectCategory.challenges.first { it.id == 4L })
    }
}
