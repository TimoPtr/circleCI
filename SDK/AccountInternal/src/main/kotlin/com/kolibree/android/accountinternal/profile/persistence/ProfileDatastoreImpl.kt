/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.persistence

import com.kolibree.android.accountinternal.profile.persistence.dao.ProfileDao
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.failearly.FailEarly
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

internal data class ProfileDatastoreImpl
@JvmOverloads
constructor(
    private val profileDao: ProfileDao,
    private val profileDBScheduler: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
) : ProfileDatastore {

    override fun profilesFlowable(): Flowable<List<ProfileInternal>> {
        return profileDao.profilesFlowable()
            .subscribeOn(profileDBScheduler)
            .observeOn(Schedulers.io())
    }

    /**
     * Get all locally stored profile for a given account
     *
     * @return a non-null ArrayList
     */
    override fun getProfiles(): Single<List<ProfileInternal>> {
        return profileDao.getProfiles()
            .subscribeOn(profileDBScheduler)
            .observeOn(Schedulers.io())
            .defaultIfEmpty(emptyList())
            .toSingle()
    }

    /**
     * Get all locally stored profile for a given account
     *
     * @param profileId the profile id to retrieve the profile
     * @return [Single]<[ProfileInternal]> emitting [ProfileInternal] or [NoSuchElementException]
     */
    override fun getProfile(profileId: Long): Single<ProfileInternal> {
        return profileDao.getProfile(profileId)
            .subscribeOn(profileDBScheduler)
            .observeOn(Schedulers.io())
            .toSingle()
    }

    /**
     * Add new Profile locally
     */
    override fun addProfile(profile: ProfileInternal): Long {
        return addProfiles(listOf(profile)).first()
    }

    /**
     * update profile locally
     */
    override fun updateProfile(profile: ProfileInternal) =
        Completable.fromCallable {
            FailEarly.failInConditionMet(
                profile.brushingTime == -1,
                message = "We don't want to save profile with incorrect brushingTime!"
            ) {
                profile.brushingTime = DEFAULT_BRUSHING_GOAL
            }

            profileDao.update(profile)
        }
            .subscribeOn(profileDBScheduler)
            .observeOn(Schedulers.io())

    /**
     * delete profile locally
     */
    override fun deleteProfile(profileId: Long) =
        Completable.fromCallable { profileDao.delete(profileId) }
            .subscribeOn(profileDBScheduler)
            .observeOn(Schedulers.io())

    /**
     * Add new Profiles locally
     */
    override fun addProfiles(profiles: List<ProfileInternal>): List<Long> {
        return profileDao.addProfiles(profiles)
    }

    /**
     * Marks that Profile that needed an update was successfully updated on the backend side.
     */
    override fun markAsUpdated(profile: ProfileInternal): Completable {
        return profileDao.markAsUpdated(profile.id)
            .subscribeOn(profileDBScheduler)
            .observeOn(Schedulers.io())
    }

    /**
     * Delete all entry in the DB
     */
    override fun deleteAll() = Completable.fromCallable { profileDao.deleteAll() }
        .subscribeOn(profileDBScheduler)
        .observeOn(Schedulers.io())
}
