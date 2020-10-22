/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.persistence

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.models.ProfileTierEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal interface ProfileTierDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(profileTierEntity: ProfileTierEntity)

    @Query("SELECT profileId, level, smilesPerBrushing, challengesNeeded, pictureUrl, rank FROM tiers t INNER JOIN profile_tier p ON t.level=p.tierLevel WHERE profileId=:profileId")
    fun tierForProfile(profileId: Long): Flowable<List<ProfileTier>>

    @Query("DELETE FROM profile_tier")
    override fun truncate(): Completable
}

/**
 * Class to be used by non-persistence users
 */
@Keep
data class ProfileTier(
    val profileId: Long,
    val level: Int,
    val smilesPerBrushing: Int,
    val challengesNeeded: Int,
    val pictureUrl: String,
    val rank: String
) {
    fun toOptional(): ProfileTierOptional? = ProfileTierOptional(this)
}

/**
 * Optional wrapper for ProfileTier, acting as null pattern
 */
@Keep
data class ProfileTierOptional(
    val value: ProfileTier? = null
)
