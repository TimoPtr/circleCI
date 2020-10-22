/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.transfer

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TransferApi(
    val smiles: Int,
    val result: String,
    @SerializedName("from_profile")
    val fromProfileId: Long,
    @SerializedName("to_profile")
    val toProfileId: Long
)
