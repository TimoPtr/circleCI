/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profiletier

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class ProfileTierApi(
    @SerializedName("Tier") val tier: String, // uppercase Tier from server
    @SerializedName("picture_url") val pictureUrl: String,
    @SerializedName("tier_id") val tierId: Int
)
