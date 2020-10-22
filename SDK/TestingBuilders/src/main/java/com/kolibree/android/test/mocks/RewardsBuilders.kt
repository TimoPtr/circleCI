/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import android.os.Parcel
import com.kolibree.android.rewards.models.Challenge
import com.kolibree.android.rewards.models.Tier
import com.kolibree.android.rewards.persistence.ProfileTier

fun createProfileTier(
    profileId: Long = 111L,
    level: Int = 1,
    smilesPerBrushing: Int = 1,
    pictureUrl: String = ""
): ProfileTier {
    return ProfileTier(
        profileId = profileId,
        level = level,
        rank = "Wood",
        pictureUrl = pictureUrl,
        smilesPerBrushing = smilesPerBrushing,
        challengesNeeded = 10
    )
}

fun createTierEntity(
    level: Int = 1,
    smilesPerBrushing: Int = 1,
    challengesNeeded: Int = 10,
    pictureUrl: String = "",
    rank: String = "AA"
) = object : Tier {
    override val level: Int = level
    override val rank: String = rank
    override val pictureUrl: String = pictureUrl
    override val smilesPerBrushing: Int = smilesPerBrushing
    override val challengesNeeded: Int = challengesNeeded

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        // no-op
    }

    override fun describeContents(): Int = 0
}

fun createChallenge(pictureUrl: String = "") = object : Challenge {
    override val id: Long = 1L
    override val name: String = ""
    override val greetingMessage: String = ""
    override val description: String = ""
    override val pictureUrl: String = pictureUrl
    override val smilesReward: Int = 1
    override val action: String? = null
    override val category: String = ""

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        // no-op
    }

    override fun describeContents(): Int = 0
}
