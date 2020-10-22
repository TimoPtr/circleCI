/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toApiRequest
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toSynchronizableItem
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import retrofit2.Response

internal class ProfilePersonalChallengeSynchronizableApiTest : BaseUnitTest() {

    private lateinit var synchronizableApi: ProfilePersonalChallengeSynchronizableApi
    private val personalChallengeApi = mock<PersonalChallengeApi>()
    private val personalChallengeDao = mock<PersonalChallengeDao>()

    override fun setup() {
        super.setup()

        TrustedClock.utcClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

        synchronizableApi = ProfilePersonalChallengeSynchronizableApi(
            personalChallengeApi,
            personalChallengeDao
        )
    }

    @Test
    fun `get() returns completed personal challenge object if request code is 204`() {
        val kolibreeId = 123L

        val date = TrustedClock.getNowZonedDateTime().minusDays(7)
        val response = Response.success<PersonalChallengeResponse>(204, null)
        whenever(personalChallengeApi.getChallenge(kolibreeId))
            .thenReturn(Single.just(response))

        val backendId = 345L
        val localId = 17L
        val entity = PersonalChallengeEntity(
            id = localId,
            backendId = backendId,
            profileId = kolibreeId,
            objectiveType = "coverage",
            difficultyLevel = "easy",
            duration = 7,
            durationUnit = "day",
            creationDate = date,
            updateDate = date,
            completionDate = null,
            progress = 15
        )

        whenever(personalChallengeDao.getChallengeForProfile(kolibreeId))
            .thenReturn(entity)

        val result = synchronizableApi.get(kolibreeId)
        val completedEntity = entity.copy(
            completionDate = TrustedClock.getNowZonedDateTime()
        )
        val expectedResult = ProfilePersonalChallengeSynchronizableItem(
            backendId = backendId,
            kolibreeId = kolibreeId,
            challenge = completedEntity.toV1Challenge()
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun `get() returns RemoteSynchronizableItem object if request is successful`() {
        TrustedClock.utcClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val kolibreeId = 123L

        val response = PersonalChallengeResponse(
            id = 98,
            createdAt = TrustedClock.getNowZonedDateTime(),
            objective = "coverage",
            level = "easy",
            duration = 7,
            durationUnit = "day",
            progress = 0,
            completedAt = null
        )

        whenever(personalChallengeApi.getChallenge(kolibreeId))
            .thenReturn(Single.just(Response.success(response)))

        val result = synchronizableApi.get(kolibreeId)

        val expectedResult = response.toSynchronizableItem(kolibreeId)
        assertEquals(expectedResult, result)
    }

    @Test(expected = EmptyBodyException::class)
    fun `get() throws EmptyBodyException if response is successful and body is empty`() {
        val kolibreeId = 123L

        whenever(personalChallengeApi.getChallenge(kolibreeId))
            .thenReturn(Single.just(Response.success<PersonalChallengeResponse>(null)))

        synchronizableApi.get(kolibreeId)
    }

    @Test(expected = ApiError::class)
    fun `get() throws ApiError if response is unsuccessful`() {
        val kolibreeId = 123L

        whenever(personalChallengeApi.getChallenge(kolibreeId))
            .thenReturn(
                Single.just(
                    Response.error(
                        500,
                        ResponseBody.create(null, "")
                    )
                )
            )

        synchronizableApi.get(kolibreeId)
    }

    @Test
    fun `createOrEdit for ProfilePersonalChallengeSynchronizableItem returns appropriate item `() {
        val profileId = 4545L
        val syncItem = createProfilePersonalChallengeSynchronizableItem(profileId)
        whenever(personalChallengeApi.updateChallenge(profileId, syncItem.toApiRequest()))
            .thenReturn(Single.just(Response.success(createPersonalChallengeResponse())))

        val remoteSyncItem = synchronizableApi.createOrEdit(syncItem)

        val expectedResult = createPersonalChallengeResponse().toSynchronizableItem(profileId)

        assertEquals(expectedResult, remoteSyncItem)
    }

    @Test
    fun `createOrEdit for ProfilePersonalChallengeRemoteItem returns appropriate item `() {
        val profileId = 4545L
        val syncItem = createProfilePersonalChallengeSynchronizableItem(profileId)
        whenever(personalChallengeApi.updateChallenge(profileId, syncItem.toApiRequest()))
            .thenReturn(Single.just(Response.success(createPersonalChallengeResponse())))

        val remoteSyncItem = synchronizableApi.createOrEdit(syncItem)

        val expectedResult = createPersonalChallengeResponse().toSynchronizableItem(profileId)

        assertEquals(expectedResult, remoteSyncItem)
    }

    @Test
    fun `createOrEdit for ProfilePersonalChallengeLocalItem returns appropriate item `() {
        val profileId = 4545L
        val syncItem = createProfilePersonalChallengeSynchronizableItem(profileId)
        whenever(personalChallengeApi.updateChallenge(profileId, syncItem.toApiRequest()))
            .thenReturn(Single.just(Response.success(createPersonalChallengeResponse())))

        val remoteSyncItem = synchronizableApi.createOrEdit(syncItem)

        val expectedResult = createPersonalChallengeResponse().toSynchronizableItem(profileId)

        assertEquals(expectedResult, remoteSyncItem)
    }

    private fun createV1PersonalChallenge() = V1PersonalChallenge(
        objectiveType = PersonalChallengeType.STREAK,
        difficultyLevel = PersonalChallengeLevel.HARD,
        period = PersonalChallengePeriod.SEVEN_DAYS,
        creationDate = TrustedClock.getNowZonedDateTime(),
        completionDate = null,
        progress = 10
    )

    private fun createPersonalChallengeResponse() = PersonalChallengeResponse(
        id = 98,
        createdAt = TrustedClock.getNowZonedDateTime(),
        objective = "streak",
        level = "hard",
        duration = 7,
        durationUnit = "day",
        progress = 10,
        completedAt = null
    )

    private fun createProfilePersonalChallengeSynchronizableItem(profileId: Long) =
        ProfilePersonalChallengeSynchronizableItem(
            backendId = 123,
            kolibreeId = profileId,
            challenge = createV1PersonalChallenge()
        )
}
