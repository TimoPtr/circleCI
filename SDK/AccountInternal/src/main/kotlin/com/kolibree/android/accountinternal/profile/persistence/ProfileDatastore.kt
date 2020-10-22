/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.persistence

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single

@Keep
interface ProfileDatastore {

    /**
     * Scheduler:
     * [profilesFlowable] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Flowable]<[List]<[ProfileInternal]>> emitting list of [ProfileInternal] or
     * [NoSuchElementException]
     */
    fun profilesFlowable(): Flowable<List<ProfileInternal>>

    /**
     * Scheduler:
     * [getProfiles] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Single]<[List]<[ProfileInternal]>> emitting list of [ProfileInternal] or
     * [NoSuchElementException]
     */
    fun getProfiles(): Single<List<ProfileInternal>>

    /**
     * Scheduler:
     * [getProfile] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Single]<[ProfileInternal]> emitting [ProfileInternal] or [NoSuchElementException]
     */
    fun getProfile(profileId: Long): Single<ProfileInternal>

    fun addProfiles(profiles: List<ProfileInternal>): List<Long>

    fun addProfile(profile: ProfileInternal): Long

    /**
     * Scheduler:
     * [deleteProfile] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Completable]
     */
    fun deleteProfile(profileId: Long): Completable

    /**
     * Scheduler:
     * [updateProfile] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Completable]
     */
    fun updateProfile(profile: ProfileInternal): Completable

    /**
     * Scheduler:
     * [markAsUpdated] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Completable]
     */
    fun markAsUpdated(profile: ProfileInternal): Completable

    /**
     * Scheduler:
     * [deleteAll] operates by default on an internal scheduler. and emits on `io` [Scheduler]
     *
     * @return [Completable]
     */
    fun deleteAll(): Completable
}
