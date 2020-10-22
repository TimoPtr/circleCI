/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.redeem

import androidx.annotation.Keep
import java.util.Objects

@Keep
data class RedeemData(
    val rewardsId: Long,
    val profileId: Long
) {
    // override equals and hashCode because Id is a Long and it might crash on Android 5

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedeemData

        if (rewardsId != other.rewardsId) return false
        if (profileId != other.profileId) return false

        return true
    }

    override fun hashCode(): Int =
        Objects.hash(rewardsId, profileId)
}
