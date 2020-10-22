/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.network.errorResponseToApiError
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toApiRequest
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toSynchronizableItem
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import com.kolibree.android.synchronizator.SynchronizableItemApi
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import javax.inject.Inject

internal class ProfilePersonalChallengeSynchronizableApi @Inject constructor(
    private val personalChallengeApi: PersonalChallengeApi,
    private val personalChallengeDao: PersonalChallengeDao
) : SynchronizableItemApi {

    override fun get(kolibreeId: Long): SynchronizableItem {
        val response = personalChallengeApi.getChallenge(kolibreeId).blockingGet()

        if (response.code() == HTTP_NO_CONTENT) {
            personalChallengeDao.getChallengeForProfile(kolibreeId)?.let {
                return completedPersonalChallengeItem(kolibreeId, it)
            }
        }

        if (response.isSuccessful) {
            val personalChallengeResponse = response.body() ?: throw EmptyBodyException(response)
            return personalChallengeResponse.toSynchronizableItem(kolibreeId)
        } else {
            throw errorResponseToApiError(response)
        }
    }

    private fun completedPersonalChallengeItem(
        kolibreeId: Long,
        challengeEntity: PersonalChallengeEntity
    ): SynchronizableItem {
        val completedChallenge = challengeEntity.copy(
            completionDate = TrustedClock.getNowZonedDateTime()
        )
        return ProfilePersonalChallengeSynchronizableItem(
            backendId = completedChallenge.backendId,
            kolibreeId = kolibreeId,
            challenge = completedChallenge.toV1Challenge()
        )
    }

    override fun createOrEdit(synchronizable: SynchronizableItem): SynchronizableItem =
        when (synchronizable) {
            is ProfilePersonalChallengeSynchronizableItem -> createOrEditInternal(synchronizable)
            else -> throw IllegalArgumentException("Cannot create/edit $synchronizable")
        }

    private fun createOrEditInternal(
        item: ProfilePersonalChallengeSynchronizableItem
    ): SynchronizableItem =
        personalChallengeApi.updateChallenge(
                item.profileId,
                item.toApiRequest()
            ).toParsedResponseSingle()
            .map { response -> response.toSynchronizableItem(item.kolibreeId) }
            .blockingGet()
}
