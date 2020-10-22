/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.security.InvalidParameterException

/**
 * To be used internally by SDK developers. Not intended for clients
 */
@Keep
interface ProfileOperations {
    /**
     * @return [ProfileWrapper], a helper to facilitate profile actions
     * @throws [InvalidParameterException] if there's no profile with [profileId]
     */
    fun withProfileId(profileId: Long): ProfileWrapper

    @Deprecated("use setActiveProfileCompletable instead")
    fun setActiveProfile(activeProfileId: Long)

    /**
     * Set the active (current) profile
     *
     * @param activeProfileId [Long] profile ID
     * @return [Completable]
     */
    fun setActiveProfileCompletable(activeProfileId: Long): Completable

    fun withCurrentProfile(): ProfileWrapper?

    val currentProfile: Profile?

    /**
     * Returns a flowable that emits the current active profile and any active profile modifications
     *
     *
     * It will never emit the same Profile two times in a row
     *
     * @return a Flowable that emits the current active profile, followed by future active profile
     */
    fun currentProfileFlowable(): Flowable<Profile>

    val ownerProfile: Profile?

    fun createProfile(data: CreateProfileData): Single<Profile>

    @Deprecated("use getProfileListSingle ")
    val profileList: List<Profile>

    val profileListSingle: Single<List<Profile>>

    @Deprecated("use getProfileWithIdSingle ")
    fun getProfileWithId(profileId: Long): Profile?

    fun getProfileWithIdSingle(profileId: Long): Single<Profile>

    fun editProfile(data: EditProfileData, profile: ProfileInternal): Single<Boolean>
}
