/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import androidx.annotation.Keep
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.kolibree.android.rewards.synchronization.SmilesHistoryZonedDateTimeTypeAdapter
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import org.threeten.bp.ZonedDateTime

/**
 * Response from endpoint https://confluence.kolibree.com/x/dQjp
 */
@Keep
internal data class ProfileSmilesHistoryApi(
    val smilesProfileHistory: List<SmilesHistoryEventApi>
)

/**
 * Polymorphic representation of a Smiles History event
 */
@Keep
internal data class SmilesHistoryEventApi(
    val message: String,
    val smilesRewards: Int,
    @JsonAdapter(SmilesHistoryZonedDateTimeTypeAdapter::class) val creationTime: ZonedDateTime,
    val eventType: String,
    // for Challenge completed
    val challengeId: Long? = null,
    // for Brushing session
    val brushingId: Long? = null,
    val brushingType: String? = null,
    // for Tier reached
    @SerializedName("tier_id") val tierLevel: Int? = null,
    // for Smiles redeemed
    val rewardsId: Long? = null,
    // for Smiles transfer, Crown completed, Streak completed
    @SerializedName("profile_id") val relatedProfileId: Long? = null
)

@Keep
internal data class ProfileSmilesHistoryApiWithProfileId(
    val profileId: Long,
    val profileSmilesHistoryApi: ProfileSmilesHistoryApi
) : SynchronizableReadOnly
