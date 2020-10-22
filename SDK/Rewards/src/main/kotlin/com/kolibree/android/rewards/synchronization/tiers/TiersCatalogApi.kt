/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.tiers

import androidx.annotation.Keep
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.kolibree.android.commons.gson.LocalDateTypeAdapter
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import org.threeten.bp.LocalDate

/**
 * Representation of tiers catalog
 *
 * See https://confluence.kolibree.com/x/CoC-AQ
 */
@Keep
internal data class TiersCatalogApi(
    @SerializedName("tier_list") val tiers: Map<Int, TierApi>
) : SynchronizableCatalog

@Keep
internal data class TierApi(
    @SerializedName("smiles_reward_per_brushing") val smilesPerBrushing: Int,
    @SerializedName("challenges_needed") val challengesNeeded: Int,
    @SerializedName("picture_url") val pictureUrl: String,
    @SerializedName("rank") val rank: String,
    @SerializedName("creation_date") @JsonAdapter(LocalDateTypeAdapter::class) val creationDate: LocalDate,
    @SerializedName("message") val message: String
)
