/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringify
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyDuration
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyUnit
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.personalchallenge.domain.logic.PersonalChallengeV1RepositoryImpl.Companion.pendingChallengeForProfile
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toSynchronizableItem
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import java.util.UUID
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import retrofit2.Response

class PersonalChallengeV1RepositoryImplTest : BaseUnitTest() {

    private val profile = ProfileBuilder.create().build()

    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val api: PersonalChallengeApi = mock()

    private val dao: PersonalChallengeDao = mock()

    private val synchronizator: Synchronizator = mock()

    private val repository =
        PersonalChallengeV1RepositoryImpl(currentProfileProvider, api, dao, synchronizator)

    @Test
    fun `fetchChallengeForCurrentProfile returns profile's challenge from persistence DB`() {
        val challenge: V1PersonalChallenge = mock()
        val entity: PersonalChallengeEntity = mock()
        whenever(entity.toV1Challenge()).thenReturn(challenge)

        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(Flowable.just(profile))
        whenever(dao.getChallengeForProfileStream(profile.id))
            .thenReturn(Flowable.just(entity))

        val testObserver = repository.getChallengeForCurrentProfile().test()

        testObserver.assertValue(challenge)
        verify(dao).getChallengeForProfileStream(profile.id)
    }

    @Test
    fun `createChallengeForCurrentProfile creates challenge and passes it to the synchronizator`() {
        TrustedClock.setFixedDate()

        val response = PersonalChallengeResponse(
            id = 100,
            objective = PersonalChallengeType.STREAK.stringify(),
            level = PersonalChallengeLevel.EASY.stringify(),
            duration = PersonalChallengePeriod.THIRTY_DAYS.stringifyDuration(),
            durationUnit = PersonalChallengePeriod.THIRTY_DAYS.stringifyUnit(),
            progress = 0,
            createdAt = ZonedDateTime.now(),
            completedAt = null
        )
        val challenge = response.toV1Challenge()
        val requestItem = pendingChallengeForProfile(profile.id, challenge)

        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(profile))
        whenever(synchronizator.create(eq(requestItem)))
            .thenReturn(Single.just(response.toSynchronizableItem(profile.id)))
        whenever(synchronizator.synchronizeCompletable()).thenReturn(Completable.complete())

        val testObserver = repository.createChallengeForCurrentProfile(challenge).test()

        testObserver.assertComplete()
        verify(synchronizator).create(requestItem)
        verify(synchronizator).synchronizeCompletable()
    }

    @Test
    fun `updateChallengeForCurrentProfile creates challenge and passes it to the synchronizator`() {
        TrustedClock.setFixedDate()

        val response = PersonalChallengeResponse(
            id = 100,
            objective = PersonalChallengeType.STREAK.stringify(),
            level = PersonalChallengeLevel.EASY.stringify(),
            duration = PersonalChallengePeriod.THIRTY_DAYS.stringifyDuration(),
            durationUnit = PersonalChallengePeriod.THIRTY_DAYS.stringifyUnit(),
            progress = 0,
            createdAt = ZonedDateTime.now(),
            completedAt = null
        )
        val challenge = response.toV1Challenge()

        val existingChallenge = mock<PersonalChallengeEntity>()
        val expectedUuid = UUID.randomUUID()
        whenever(existingChallenge.uuid).thenReturn(expectedUuid)
        whenever(dao.getChallengeForProfile(profile.id)).thenReturn(existingChallenge)

        val requestItem = pendingChallengeForProfile(
            profileId = profile.id,
            challenge = challenge,
            uuid = expectedUuid
        )

        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(profile))
        whenever(synchronizator.update(eq(requestItem)))
            .thenReturn(Single.just(response.toSynchronizableItem(profile.id)))
        whenever(synchronizator.synchronizeCompletable()).thenReturn(Completable.complete())

        val testObserver = repository.updateChallengeForCurrentProfile(challenge).test()

        testObserver.assertComplete()
        verify(synchronizator).update(requestItem)
        verify(synchronizator).synchronizeCompletable()
    }

    /*
    deleteChallengeForCurrentProfile
     */

    @Test
    fun `deleteChallengeForCurrentProfile deletes the challenge only if the API succeeded and then synchronizes on success`() {
        val synchronizeSubject = CompletableSubject.create()
        whenever(synchronizator.synchronizeCompletable()).thenReturn(synchronizeSubject)
        whenever(currentProfileProvider.currentProfileSingle()).thenReturn(Single.just(profile))
        @Suppress("RemoveExplicitTypeArguments")
        whenever(api.deleteChallenge(profile.id))
            .thenReturn(Single.just(Response.success<Void>(null)))

        var verifiedSynchronizeNotSubscribed = false
        whenever(dao.delete(profile.id))
            .thenAnswer {
                assertFalse(synchronizeSubject.hasObservers())

                verifiedSynchronizeNotSubscribed = true

                Unit
            }

        val observer = repository.deleteChallengeForCurrentProfile().test()

        inOrder(api, dao, synchronizator) {
            verify(api).deleteChallenge(profile.id)
            verify(dao).delete(profile.id)
        }

        assertTrue(verifiedSynchronizeNotSubscribed)

        observer.assertNotComplete()

        synchronizeSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `deleteChallengeForCurrentProfile never deletes the challenge locally or synchronizes if the API call fails`() {
        val synchronizeSubject = CompletableSubject.create()
        whenever(synchronizator.synchronizeCompletable()).thenReturn(synchronizeSubject)
        whenever(currentProfileProvider.currentProfileSingle()).thenReturn(Single.just(profile))
        @Suppress("RemoveExplicitTypeArguments")
        whenever(api.deleteChallenge(eq(profile.id)))
            .thenReturn(
                Single.just(
                    Response.error<Void>(
                        400,
                        ResponseBody.create(MediaType.parse("application/json"), "")
                    )
                )
            )

        repository.deleteChallengeForCurrentProfile().test().assertError(ApiError::class.java)

        verify(api).deleteChallenge(profile.id)

        verify(dao, never()).delete(profile.id)

        assertFalse(synchronizeSubject.hasObservers())
    }
}
