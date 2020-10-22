package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mock
import retrofit2.Call
import retrofit2.Response

internal class ChallengeProgressSynchronizableReadOnlyApiTest : BaseUnitTest() {
    @Mock
    lateinit var rewardsApi: RewardsApi

    private lateinit var challengeProgressApi: ChallengeProgressSynchronizableReadOnlyApi

    override fun setup() {
        super.setup()

        challengeProgressApi = ChallengeProgressSynchronizableReadOnlyApi(rewardsApi)
    }

    @Test(expected = ApiError::class)
    fun `get throws backend error when response is not successful`() {
        val response = Response.error<ChallengeProgressApi>(404, ResponseBody.create(null, "ignored"))

        val profileId = 78L
        val call = mock<Call<ChallengeProgressApi>>()
        whenever(call.execute()).thenReturn(response)
        whenever(rewardsApi.getChallengeProgress(profileId)).thenReturn(call)

        challengeProgressApi.get(profileId)
    }

    @Test
    fun `get maps to ChallengeProgressProfileCatalogInternal when response is successful`() {
        val challengeItem1 = ChallengeProgressApiBuilder.createChallengesItem(
            challengeId = 1,
            completionDetails = null,
            completionTime = null,
            percentage = 76
        )

        val challengeItem2 = ChallengeProgressApiBuilder.createChallengesItem(
            challengeId = 2,
            completionDetails = ChallengeCompletionDetails(3, 4),
            completionTime = TrustedClock.getNowZonedDateTime(),
            percentage = 3
        )

        val body = ChallengeProgressApiBuilder.build(listOf(challengeItem1, challengeItem2))

        val response = Response.success(body)

        val profileId = 78L
        val call = mock<Call<ChallengeProgressApi>>()
        whenever(call.execute()).thenReturn(response)
        whenever(rewardsApi.getChallengeProgress(profileId)).thenReturn(call)

        val expectedChallengeProgressInternal1 = ChallengeProgressEntity(
            challengeId = challengeItem1.challengeId,
            completionDetails = challengeItem1.completionDetails,
            completionTime = challengeItem1.completionTime,
            percentage = challengeItem1.percentage,
            profileId = profileId
        )

        val expectedChallengeProgressInternal2 = ChallengeProgressEntity(
            challengeId = challengeItem2.challengeId,
            completionDetails = challengeItem2.completionDetails,
            completionTime = challengeItem2.completionTime,
            percentage = challengeItem2.percentage,
            profileId = profileId
        )

        assertEquals(
            arrayListOf(expectedChallengeProgressInternal1, expectedChallengeProgressInternal2),
            challengeProgressApi.get(profileId)
        )
    }
}
