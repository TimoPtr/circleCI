/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.toParsedResponseCompletable
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.synchronizator.models.SynchronizableItem
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.UUID
import javax.inject.Inject

@Keep
interface PersonalChallengeV1Repository {

    fun getChallengeForCurrentProfile(): Flowable<V1PersonalChallenge>

    fun getChallengesForCurrentProfile(): Flowable<List<V1PersonalChallenge>>

    fun createChallengeForCurrentProfile(challenge: V1PersonalChallenge): Completable

    fun updateChallengeForCurrentProfile(challenge: V1PersonalChallenge): Completable

    fun deleteChallengeForCurrentProfile(): Completable
}

internal class PersonalChallengeV1RepositoryImpl @Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val api: PersonalChallengeApi,
    private val dao: PersonalChallengeDao,
    private val synchronizator: Synchronizator
) : PersonalChallengeV1Repository, Truncable {

    override fun getChallengeForCurrentProfile(): Flowable<V1PersonalChallenge> =
        currentProfileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .switchMap { profile -> dao.getChallengeForProfileStream(profileId = profile.id) }
            .map { it.toV1Challenge() }

    override fun getChallengesForCurrentProfile(): Flowable<List<V1PersonalChallenge>> =
        currentProfileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .switchMap { profile -> dao.getChallengesForProfileStream(profileId = profile.id) }
            .map { list ->
                list.map { item -> item.toV1Challenge() }
            }

    override fun createChallengeForCurrentProfile(
        challenge: V1PersonalChallenge
    ): Completable =
        currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .flatMap { profile ->
                val synchronizableItem = pendingChallengeForProfile(
                    profileId = profile.id,
                    challenge = challenge
                )

                synchronizator.create(synchronizableItem)
            }
            .flatMapCompletable { synchronizator.synchronizeCompletable() }

    override fun updateChallengeForCurrentProfile(
        challenge: V1PersonalChallenge
    ): Completable =
        currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .flatMap { profile ->
                val existingEntity = dao.getChallengeForProfile(profile.id)

                FailEarly.failInConditionMet(
                    existingEntity == null,
                    "No personal challenge for profile"
                )

                val synchronizableItem = pendingChallengeForProfile(
                    profileId = profile.id,
                    challenge = challenge,
                    uuid = existingEntity?.uuid
                )

                synchronizator.update(synchronizableItem)
            }
            .flatMapCompletable { synchronizator.synchronizeCompletable() }

    override fun deleteChallengeForCurrentProfile(): Completable =
        currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { profile ->
                Completable.concatArray(
                    api.deleteChallenge(profile.id).toParsedResponseCompletable(),
                    Completable.fromAction { dao.delete(profile.id) }
                ).andThen(synchronizator.synchronizeCompletable())
            }

    override fun truncate(): Completable = dao.truncate()

    companion object {

        @VisibleForTesting
        fun pendingChallengeForProfile(
            profileId: Long,
            challenge: V1PersonalChallenge,
            uuid: UUID? = null
        ): SynchronizableItem =
            ProfilePersonalChallengeSynchronizableItem(
                backendId = null, // this will be set when challenge will be set on the backend side
                kolibreeId = profileId,
                challenge = challenge,
                uuid = uuid
            )
    }
}
